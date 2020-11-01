package hs.jfx.eventstream;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javafx.beans.binding.Binding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class EventStreamTest {

  private StringProperty property = new SimpleStringProperty();
  private List<String> capturedStrings = new ArrayList<>();
  private List<Boolean> capturedBooleans = new ArrayList<>();

  @Test
  public void streamShouldNotProcessValuesWhenUnsubscribed() {
    EventStream<String> eventStream = EventStreams.valuesOf(property)
      .filter(s -> s.contains("l"));  // missing null check would result in NPE if processing happened

    property.set("Hello");
    property.set(null);
    property.set("World");

    // Verify stream still works, and wasn't just gc'd:

    eventStream.subscribe(capturedStrings::add);

    assertEquals(List.of("World"), capturedStrings);
  }

  @Test
  public void valuesStreamShouldEmitCurrentValueOnSubscribe() {
    property.set("Hello");

    EventStreams.valuesOf(property)
      .subscribe(capturedStrings::add);

    assertEquals(List.of("Hello"), capturedStrings);
  }

  @Test
  public void filteredEventStreamShouldSkipFilteredValues() {
    EventStreams.valuesOf(property)
      .filter(s -> s != null && s.contains("l"))
      .subscribe(capturedStrings::add);

    assertTrue(capturedStrings.isEmpty());

    property.set("Hello");

    assertEquals(List.of("Hello"), capturedStrings);

    property.set("World");

    assertEquals(List.of("Hello", "World"), capturedStrings);

    property.set("Everyone");

    assertEquals(List.of("Hello", "World"), capturedStrings);
  }

  @Test
  public void filteredEventStreamShouldRejectNullPredicate() {
    assertThrows(NullPointerException.class, () -> EventStreams.valuesOf(property).filter(null));
  }

  @Test
  public void mappedEventStreamShouldConvertValues() {
    EventStreams.valuesOf(property)
      .map(s -> s == null ? "(null)" : s)
      .subscribe(capturedStrings::add);

    assertEquals(List.of("(null)"), capturedStrings);
  }

  @Test
  public void mappedEventStreamShouldRejectNullFunction() {
    assertThrows(NullPointerException.class, () -> EventStreams.valuesOf(property).map(null));
  }

  @Test
  public void flatMappedEventStreamShouldDelegateToSubstream() {
    class TestWindow {
      BooleanProperty showing = new SimpleBooleanProperty();
    }

    class TestScene {
      ObjectProperty<TestWindow> window = new SimpleObjectProperty<>();
    }

    class TestNode {
      ObjectProperty<TestScene> scene = new SimpleObjectProperty<>();
    }

    TestNode node = new TestNode();
    TestScene scene = new TestScene();
    TestWindow window = new TestWindow();

    Subscription subscription = EventStreams.valuesOf(node.scene)
      .filter(Objects::nonNull)
      .flatMap(s -> EventStreams.valuesOf(s.window))
      .filter(Objects::nonNull)
      .flatMap(w -> EventStreams.valuesOf(w.showing))
      .subscribe(capturedBooleans::add);

    assertEquals(List.of(), capturedBooleans);  // nothing expected, as nulls were filtered out

    scene.window.set(window);

    assertEquals(List.of(), capturedBooleans);  // nothing expected, as node still has no scene

    node.scene.set(scene);

    assertEquals(List.of(false), capturedBooleans);  // node has a scene, scene has a window, and it is not showing

    window.showing.set(true);

    assertEquals(List.of(false, true), capturedBooleans);  // it is showing now

    TestWindow window2 = new TestWindow();

    scene.window.set(window2);

    assertEquals(List.of(false, true, false), capturedBooleans);  // switched to a non-visible window

    scene.window.set(window);

    assertEquals(List.of(false, true, false, true), capturedBooleans);  // switched back to visible window

    window2.showing.set(true);

    scene.window.set(window2);

    assertEquals(List.of(false, true, false, true, true), capturedBooleans);  // stream emits another value, because it has no notion of "current" value

    window.showing.set(false);

    assertEquals(List.of(false, true, false, true, true), capturedBooleans);  // changing an old dependency does not influence anything

    subscription.unsubscribe();

    window2.showing.set(false);

    assertEquals(List.of(false, true, false, true, true), capturedBooleans);  // no change, the consumer was unsubcribed
  }

  @Test
  public void flatMappedEventStreamShouldRejectNullFunction() {
    assertThrows(NullPointerException.class, () -> EventStreams.valuesOf(property).flatMap(null));
  }

  @Test
  public void peekedEventStreamShouldGetValuesThatArePartOfTheStream() {
    List<String> peekedValues = new ArrayList<>();

    EventStream<String> eventStream = EventStreams.valuesOf(property)
      .peek(peekedValues::add);

    property.set("Hello");
    property.set("World");

    assertEquals(List.of(), peekedValues);  // no subscribers, so stream is not active

    Subscription subscription = eventStream.subscribe(capturedStrings::add);

    assertEquals(List.of("World"), peekedValues);  // current value is eagerly emitted

    property.set("!");

    assertEquals(List.of("World", "!"), peekedValues);  // value change is picked up by peek function

    subscription.unsubscribe();

    assertEquals(List.of("World", "!"), peekedValues);  // no change

    property.set("Goodbye");
    property.set("Forever");

    assertEquals(List.of("World", "!"), peekedValues);  // no change as stream is not in use

    eventStream.subscribe(capturedStrings::add);

    assertEquals(List.of("World", "!", "Forever"), peekedValues);  // stream subscribed again, and immediately emits current value
  }

  @Test
  public void peekedEventStreamShouldNotAllowRecursiveEventEmission() {
    property.set("Goodbye");

    EventStream<String> eventStream = EventStreams.valuesOf(property);

    Consumer<? super String> sideEffect = s -> {
      if("Hello".equals(s)) {
        property.set("World");
      }
    };

    eventStream.peek(sideEffect)
      .subscribe(capturedStrings::add);

    assertEquals(List.of("Goodbye"), capturedStrings);

    UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    try {
      AtomicReference<Throwable> exception = new AtomicReference<>();

      Thread.setDefaultUncaughtExceptionHandler((t, e) -> exception.set(e));

      property.set("Hello");  // triggers recursive change by peek function

      assertEquals(IllegalStateException.class, exception.get().getClass());
      assertEquals("Side effect is not allowed to cause recursive event emission", exception.get().getMessage());
    }
    finally {
      Thread.setDefaultUncaughtExceptionHandler(defaultUncaughtExceptionHandler);
    }
  }

  @Test
  public void peekedEventStreamShouldRejectNullPredicate() {
    assertThrows(NullPointerException.class, () -> EventStreams.valuesOf(property).peek(null));
  }

  @Test
  public void eventStreamWithDefaultShouldEmitDefaultWhenNoEventsAreEmittedYet() {
    EventStream<String> eventStream = EventStreams.valuesOf(property)
      .filter(Objects::nonNull)  // this filters the initial event, as it is null
      .withDefaultEvent("(was null)");  // this makes sure there is another initial value

    Subscription subscription = eventStream.subscribe(capturedStrings::add);

    assertEquals(List.of("(was null)"), capturedStrings);

    subscription.unsubscribe();

    assertEquals(List.of("(was null)"), capturedStrings);  // no change after unsubcribing

    subscription = eventStream.subscribe(capturedStrings::add);

    assertEquals(List.of("(was null)", "(was null)"), capturedStrings);  // default event is emitted again when resubscribed

    eventStream.subscribe(x -> {});

    assertEquals(List.of("(was null)", "(was null)"), capturedStrings);  // no change when an independent subcriber is added
  }

  @Test
  public void eventStreamWithDefaultShouldNotEmitDefaultWhenEventsHaveBeenEmitted() {
    property.set("Hello");

    EventStream<String> eventStream = EventStreams.valuesOf(property)
      .withDefaultEvent("(was null)");

    Subscription subscription = eventStream.subscribe(capturedStrings::add);

    assertEquals(List.of("Hello"), capturedStrings);  // current value is emitted

    subscription.unsubscribe();

    assertEquals(List.of("Hello"), capturedStrings);  // no change after unsubcribing

    subscription = eventStream.subscribe(capturedStrings::add);

    assertEquals(List.of("Hello", "Hello"), capturedStrings);  // current value is emitted again when resubscribed
  }

  @Test
  public void eventStreamBindingShouldTrackValuesEmittedInStream() {
    Binding<String> binding = EventStreams.valuesOf(property)
      .filter(Objects::nonNull)  // filter out the null value so the binding initial value must be used
      .toBinding("Empty");

    assertEquals("Empty", binding.getValue());
    assertTrue(binding.isValid());

    property.set("Hello");

    assertEquals("Hello", binding.getValue());

    binding.dispose();

    property.set("World");

    assertEquals("Hello", binding.getValue());  // unchanged as binding was disposed, resulting in it unsubscribing itself
  }
}
