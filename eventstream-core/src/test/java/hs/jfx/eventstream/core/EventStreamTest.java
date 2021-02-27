package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.api.ValueStream;
import hs.jfx.eventstream.core.util.Sink;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventStreamTest {
  private final StringProperty property = new SimpleStringProperty();
  private final Sink<String> strings = new Sink<>();

  @Test
  void shouldNeverEmitNull() {
    property.set("A");

    Events.of(property)  // never emits null as it emits Change objects
      .map(Change::getValue)  // could become null here
      .subscribe(strings::add);

    property.set(null);  // try to get it to emit null

    assertTrue(strings.isEmpty());
  }

  @Nested
  class Empty {

    @Test
    void shouldNeverEmitAnything() {
      EventStream<String> stream = Events.empty();

      stream.subscribe(strings::add);

      assertTrue(strings.isEmpty());
    }
  }

  @Nested
  class IntermediateOperations {
    @Nested
    class ConditionOn {

      @Test
      void shouldEmitValuesConditionally() {
        property.set("Bye");

        BooleanProperty visible = new SimpleBooleanProperty(true);
        EventStream<String> stream = Events.of(property)
          .map(Change::getValue)
          .conditionOn(visible);

        stream.subscribe(strings::add);

        assertEquals(List.of(), strings.drain());  // new subscriber gets nothing as this is a ChangeStream

        visible.set(false);
        property.set("Hello");

        assertEquals(List.of(), strings.drain());  // current subscriber gets nothing as condition is false

        stream.subscribe(strings::add);

        assertEquals(List.of(), strings.drain());  // new subscriber gets nothing as this is a ChangeStream

        visible.set(true);
        assertEquals(List.of(), strings.drain());  // even though condition is true now, nothing is emitted as ChangeStreams donot emit upon subscription

        property.set("Hi");

        assertEquals(List.of("Hi", "Hi"), strings.drain());  // both subscribers receive new changes while condition is true

        stream.subscribe(strings::add);

        assertEquals(List.of(), strings.drain());  // new subscriber gets nothing as this is a ChangeStream

        property.set("World");

        assertEquals(List.of("World", "World", "World"), strings.drain());  // all subscribers get current value
      }

      @Test
      public void shouldNotImmediatelyEmitValuesWhenConditionBecomesTrue() {
        property.set("Bye");

        BooleanProperty visible = new SimpleBooleanProperty(false);
        EventStream<String> stream = Events.of(property)
          .map(Change::getValue)
          .conditionOn(visible)
          .map(String::toUpperCase);

        stream.subscribe(strings::add);

        assertEquals(List.of(), strings.drain());

        visible.set(true);

        assertEquals(List.of(), strings.drain());
      }

      @Test
      void shouldTreatNullAsFalse() {
        ObjectProperty<Boolean> visible = new SimpleObjectProperty<>();
        Events.of(property)
          .map(Change::getValue)
          .conditionOn(visible)  // internally, this uses flatMap, which is null safe
          .subscribe(strings::add);

        visible.set(false);
        property.set("Hello");

        assertEquals(List.of(), strings.drain());  // current subscriber gets nothing as condition is false

        visible.set(null);
        property.set("World");

        assertEquals(List.of(), strings.drain());  // current subscriber gets nothing as condition is null

        visible.set(true);
        property.set("Goodbye");

        assertEquals(List.of("Goodbye"), strings.drain());
      }
    }

    @Nested
    class Filter {

      @Test
      void shouldSkipFilteredValues() {
        Events.of(property)
          .map(Change::getValue)
          .filter(s -> s.contains("o"))
          .subscribe(strings::add);

        assertTrue(strings.isEmpty());

        property.set("Hello");

        assertEquals(List.of("Hello"), strings.drain());

        property.set("World");

        assertEquals(List.of("World"), strings.drain());

        property.set("Everything");  // doesn't match filter

        assertTrue(strings.isEmpty());
      }

      @Test
      void shouldSkipNulls() {
        property.set("A");

        Events.of(property)
          .map(Change::getValue)
          .filter(TestUtil::filterFailOnNull)
          .subscribe(strings::add);

        property.set(null);

        assertTrue(strings.isEmpty());
      }

      @Test
      void shouldRejectNullPredicate() {
        assertThrows(NullPointerException.class, () -> Events.of(property).filter(null));
      }
    }

    @Nested
    class FlatMap {
      private final Sink<Boolean> booleans = new Sink<>();

      @Test
      void shouldDelegateToSubstream() {
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

        EventStream<Boolean> stream = Events.of(node.scene)
          .map(Change::getValue)
          .flatMap(s -> s == null ? Events.empty() : Events.of(s.window).map(Change::getValue))
          .flatMap(w -> w == null ? Events.empty() : Events.of(w.showing).map(Change::getValue));

        Subscription subscription = stream
          .subscribe(booleans::add);

        // Tracked: node.scene
        // Expected: nothing, node.scene has not changed yet since tracking started
        assertTrue(booleans.isEmpty());

        scene.window.set(window);

        // Tracked: node.scene
        // Expected: nothing, node.scene has not changed yet since tracking started
        assertTrue(booleans.isEmpty());

        node.scene.set(scene);

        // Tracked: node.scene -> scene.window
        // Expected: nothing, as scene.window hasn't changed yet since tracking started
        assertTrue(booleans.isEmpty());

        window.showing.set(true);

        // Tracked: node.scene -> scene.window
        // Expected: nothing, as scene.window hasn't changed yet since tracking started
        assertTrue(booleans.isEmpty());

        TestWindow window2 = new TestWindow();

        scene.window.set(window2);

        // Tracked: node.scene -> scene.window -> window2.showing
        // Expected: nothing, as window2.showing hasn't changed yet since tracking started
        assertTrue(booleans.isEmpty());

        window2.showing.set(true);

        // Tracked: node.scene -> scene.window -> window2.showing
        // Expected: true, as window2.showing was changed to true since tracking started
        assertEquals(List.of(true), booleans.drain());

        window2.showing.set(false);

        // Tracked: node.scene -> scene.window -> window2.showing
        // Expected: false, as window2.showing was changed to false since tracking started
        assertEquals(List.of(false), booleans.drain());

        scene.window.set(window);

        // Tracked: node.scene -> scene.window -> window.showing
        // Expected: nothing, as window.showing hasn't changed yet since tracking started
        assertTrue(booleans.isEmpty());

        window2.showing.set(true);
        scene.window.set(window2);

        // Tracked: node.scene -> scene.window -> window2.showing
        // Expected: nothing, as window2.showing hasn't changed yet since tracking started
        assertTrue(booleans.isEmpty());

        node.scene.set(null);

        // Tracked: node.scene
        // Expected: nothing, as node.scene is null
        assertTrue(booleans.isEmpty());

        window.showing.set(false);

        // changing an old dependency does not influence anything
        assertEquals(List.of(), booleans.drain());

        subscription.unsubscribe();

        window2.showing.set(false);

        // no change, the consumer was unsubcribed
        assertEquals(List.of(), booleans.drain());
      }

      @Test
      void shouldSkipNulls() {
        property.set("A");

        Events.of(property)
          .map(Change::getValue)
          .flatMap(TestUtil::eventFlatMapFailOnNull)
          .subscribe(strings::add);

        property.set(null);

        assertTrue(strings.isEmpty());

        property.set("B");

        assertTrue(strings.isEmpty());
      }

      @Test
      void shouldRejectNullFunction() {
        assertThrows(NullPointerException.class, () -> Events.of(property).flatMap(null));
      }

      @Test
      void shouldDoNothingWhenFlatMappingToNull() {
        Events.of(property)
          .flatMap(v -> (EventStream<String>)null)
          .subscribe(strings::add);

        assertTrue(strings.isEmpty());  // expect nothing upon subscription

        property.set("B");  // would trigger a NPE (which is only logged) if flatmapping code didn't handle this case specifically

        assertTrue(strings.isEmpty());  // expect nothing, perfectly okay for a change stream
      }
    }

    @Nested
    class Map {

      @Test
      void shouldConvertValues() {
        Events.of(property)
          .map(Change::getValue)
          .map(s -> "" + (int)s.charAt(0))
          .subscribe(strings::add);

        property.set("A");

        assertEquals(List.of("65"), strings.drain());
      }

      @Test
      void shouldSkipNulls() {
        property.set("A");

        Events.of(property)
          .map(Change::getValue)
          .map(TestUtil::mapFailOnNull)
          .subscribe(strings::add);

        property.set(null);

        assertEquals(List.of(), strings.drain());
      }

      @Test
      void shouldRejectNullFunction() {
        assertThrows(NullPointerException.class, () -> Events.of(property).map(null));
      }
    }

    @Nested
    class Peek {

      @Test
      void shouldConsumeStreamValues() {
        Sink<String> peekedValues = new Sink<>();

        EventStream<String> eventStream = Events.of(property)
          .map(Change::getValue)
          .peek(peekedValues::add);

        property.set("Hello");
        property.set("World");

        assertEquals(List.of(), peekedValues.drain());  // no subscribers, so stream is not active

        Subscription subscription = eventStream.subscribe(strings::add);

        assertTrue(peekedValues.isEmpty());  // nothing emitted on subscribe

        property.set("!");

        assertEquals(List.of("!"), peekedValues.drain());  // value change is picked up by peek function

        subscription.unsubscribe();

        assertEquals(List.of(), peekedValues.drain());  // no change

        property.set("Goodbye");
        property.set("Forever");

        assertEquals(List.of(), peekedValues.drain());  // no change as stream is not in use

        eventStream.subscribe(strings::add);

        assertTrue(peekedValues.isEmpty());  // nothing emitted on subscribe
      }

      @Test
      void shouldNotAllowRecursiveEmission() {
        property.set("Goodbye");

        EventStream<String> eventStream = Events.of(property).map(Change::getValue);

        Consumer<? super String> sideEffect = s -> {
          if("Hello".equals(s)) {
            property.set("World");
          }
        };

        eventStream.peek(sideEffect)
          .subscribe(strings::add);

        assertTrue(strings.isEmpty());  // nothing emitted on subscribe

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
      void shouldSkipNullValues() {
        Sink<String> peekedValues = new Sink<>();

        property.set("A");

        Events.of(property).map(Change::getValue).peek(peekedValues::add).subscribe(strings::add);

        property.set(null);

        assertEquals(List.of(), peekedValues.drain());
        assertEquals(List.of(), strings.drain());
      }

      @Test
      void shouldRejectNullConsumer() {
        assertThrows(NullPointerException.class, () -> Events.of(property).peek(null));
      }
    }

    @Nested
    class WithDefaultGet {

      @Test
      void shouldSupplyDefaultToNewSubscribers() {
        ValueStream<String> eventStream = Events.of(property)
          .map(Change::getValue)
          .withDefaultGet(() -> "(null)");

        Subscription subscription = eventStream.subscribe(strings::add);

        assertEquals(List.of("(null)"), strings.drain());

        property.set("Hello");

        assertEquals(List.of("Hello"), strings.drain());  // current value is emitted

        subscription.unsubscribe();

        assertTrue(strings.isEmpty());  // no change after unsubcribing

        subscription = eventStream.subscribe(strings::add);

        assertEquals(List.of("(null)"), strings.drain());  // default event is emitted again when resubscribed

        eventStream.subscribe(x -> {});

        assertTrue(strings.isEmpty());  // no change when an independent subcriber is added
      }

      @Test
      void shouldRejectNullPredicate() {
        assertThrows(NullPointerException.class, () -> Events.of(property).withDefaultGet(null));
      }
    }

    @Nested
    class WithDefault {

      @Test
      void shouldSupplyDefaultToNewSubscribers() {
        ValueStream<String> eventStream = Events.of(property)
          .map(Change::getValue)
          .withDefault("(null)");

        Subscription subscription = eventStream.subscribe(strings::add);

        assertEquals(List.of("(null)"), strings.drain());

        property.set("Hello");

        assertEquals(List.of("Hello"), strings.drain());  // current value is emitted

        subscription.unsubscribe();

        assertTrue(strings.isEmpty());  // no change after unsubcribing

        subscription = eventStream.subscribe(strings::add);

        assertEquals(List.of("(null)"), strings.drain());  // default event is emitted again when resubscribed

        eventStream.subscribe(x -> {});

        assertTrue(strings.isEmpty());  // no change when an independent subcriber is added
      }
    }
  }

  @Nested
  class TerminalOperations {
    @Nested
    class Subscribe {
      @Test
      void shouldReceiveValue() {
        EventSource<String> source = new EventSource<>();

        source.subscribe(strings::add);
        source.push("A");

        assertEquals("A", strings.single());
      }

      @Test
      void shouldRejectNull() {
        EventSource<String> source = new EventSource<>();

        source.subscribe(strings::add);

        assertThrows(NullPointerException.class, () -> source.push(null));
      }
    }
  }
}
