package hs.jfx.eventstream;

import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.domain.ValueStream;
import hs.jfx.eventstream.experimental.Transactions;
import hs.jfx.eventstream.util.Sink;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javafx.beans.binding.Binding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValueStreamTest {
  private final StringProperty property = new SimpleStringProperty();
  private final Sink<String> strings = new Sink<>();

  @Test
  void shouldSubscribeLazily() {
    AtomicBoolean processed = new AtomicBoolean();
    ValueStream<String> eventStream = Values.of(property)
      .peek(s -> { processed.set(true); });

    property.set("Hello");

    assertFalse(processed.get());

    eventStream.subscribe(strings::add);

    assertEquals(List.of("Hello"), strings.drain());
    assertTrue(processed.get());
  }

  @Test
  void shouldSupplyCurrentValueToNewSubscribers() {
    property.set("Hello");

    ValueStream<String> stream = Values.of(property);

    stream.subscribe(strings::add);

    assertEquals(List.of("Hello"), strings.drain());

    Sink<String> sink = new Sink<>();

    stream.subscribe(sink::add);

    assertEquals(List.of("Hello"), sink.drain());
  }

  @Nested
  class ConditionOn {

    @Test
    public void shouldEmitValuesConditionally() {
      property.set("Bye");

      BooleanProperty visible = new SimpleBooleanProperty(true);
      ValueStream<String> stream = Values.of(property)
        .conditionOn(visible);

      stream.subscribe(strings::add);

      assertEquals(List.of("Bye"), strings.drain());  // expect current value for new subscriber

      visible.set(false);
      property.set("Hello");

      assertEquals(List.of(), strings.drain());  // current subscriber gets nothing as condition is false

      stream.subscribe(strings::add);

      assertEquals(List.of(), strings.drain());  // new subscriber gets nothing as condition is false

      visible.set(true);
      assertEquals(List.of("Hello", "Hello"), strings.drain());  // when condition becomes true, both subcribers immediately get notified

      property.set("Hi");

      assertEquals(List.of("Hi", "Hi"), strings.drain());  // both subscribers receive new changes while condition is true

      stream.subscribe(strings::add);

      assertEquals(List.of("Hi"), strings.drain());  // expect current value for new subscriber

      property.set("World");

      assertEquals(List.of("World", "World", "World"), strings.drain());  // all subscribers get current value
    }

    @Test
    public void shouldEmitValuesWhenConditionBecomesTrue() {
      property.set("Bye");

      BooleanProperty visible = new SimpleBooleanProperty(false);
      ValueStream<String> stream = Values.of(property)
        .conditionOn(visible)
        .map(String::toUpperCase);

      stream.subscribe(strings::add);

      assertEquals(List.of(), strings.drain());

      visible.set(true);

      assertEquals(List.of("BYE"), strings.drain());
    }
  }

  @Nested
  class Constant {

    @Test
    void shouldReturnSameValueAlways() {
      ValueStream<String> stream = Values.constant("x");

      stream.subscribe(strings::add);

      assertEquals("x", strings.single());

      stream.subscribe(strings::add);

      assertEquals("x", strings.single());
    }
  }

  @Nested
  class FilterOperation {

    @Test
    void shouldSkipFilteredValues() {
      property.set("Forever");  // Would match

      Values.of(property)
        .filter(s -> s.contains("o"))
        .subscribe(strings::add);

      assertTrue(strings.isEmpty());  // Nothing is sent out as ChangeStreams donot emit on subscribe

      property.set("Hello");

      assertEquals(List.of("Hello"), strings.drain());

      property.set("World");

      assertEquals(List.of("World"), strings.drain());

      property.set("Everything");  // doesn't match filter

      assertTrue(strings.isEmpty());
    }

    @Test
    void shouldSkipNulls() {
      Values.of(property)
        .filter(TestUtil::filterFailOnNull)  // if it didn't skip nulls, this filter would hard error
        .subscribe(strings::add);

      assertTrue(strings.isEmpty());
    }

    @Test
    void shouldRejectNullPredicate() {
      assertThrows(NullPointerException.class, () -> Values.of(property).filter(null));
    }
  }

  @Nested
  class FlatMapOperation {
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

      Subscription subscription = Values.of(node.scene)
        .flatMap(s -> Values.of(s.window))
        .flatMap(w -> Values.of(w.showing))
        .orElse(false)
        .subscribe(booleans::add);

      // expect false as scene is null
      assertEquals(List.of(false), booleans.drain());

      scene.window.set(window);

      // nothing expected, as node still has no scene
      assertEquals(List.of(), booleans.drain());

      node.scene.set(scene);

      // node has a scene, scene has a window, and it is not showing, another value is emitted as scene changed
      assertEquals(List.of(false), booleans.drain());

      window.showing.set(true);

      // it is showing now
      assertEquals(List.of(true), booleans.drain());

      TestWindow window2 = new TestWindow();

      scene.window.set(window2);

      // switched to a non-visible window
      assertEquals(List.of(false), booleans.drain());

      scene.window.set(window);

      // switched back to visible window
      assertEquals(List.of(true), booleans.drain());

      window2.showing.set(true);
      scene.window.set(window2);

      // stream emits another value, because it has no notion of "current" value
      assertEquals(List.of(true), booleans.drain());

      node.scene.set(null);

      // condition should revert to false
      assertEquals(List.of(false), booleans.drain());

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
      Values.of(property)
        .flatMap(TestUtil::valueFlatMapFailOnNull)
        .subscribe(strings::add);

      assertNull(strings.single());
    }

    @Test
    void shouldRejectNullFunction() {
      assertThrows(NullPointerException.class, () -> Values.of(property).flatMap(null));
    }
  }

  @Nested
  class MapOperation {

    @Test
    void shouldConvertValues() {
      property.set("A");

      Values.of(property)
        .map(s -> "" + (int)s.charAt(0))
        .subscribe(strings::add);

      assertEquals(List.of("65"), strings.drain());
    }

    @Test
    void shouldSkipNulls() {
      Values.of(property)
        .map(TestUtil::mapFailOnNull)
        .subscribe(strings::add);

      assertNull(strings.single());
    }

    @Test
    void shouldRejectNullFunction() {
      assertThrows(NullPointerException.class, () -> Values.of(property).map(null));
    }
  }

  @Nested
  class OrElseOperation {

    @Test
    void shouldReplaceNulls() {
      Values.of(property)
        .orElse("(null)")
        .subscribe(strings::add);

      assertEquals("(null)", strings.single());

      property.set("A");

      assertEquals("A", strings.single());

      property.set(null);

      assertEquals("(null)", strings.single());
    }
  }

  @Nested
  class PeekOperation {

    @Test
    void shouldConsumeStreamValues() {
      Sink<String> peekedValues = new Sink<>();

      ValueStream<String> eventStream = Values.of(property)
        .peek(peekedValues::add);

      property.set("Hello");
      property.set("World");

      assertEquals(List.of(), peekedValues.drain());  // no subscribers, so stream is not active

      Subscription subscription = eventStream.subscribe(strings::add);

      assertEquals(List.of("World"), peekedValues.drain());  // current value is eagerly emitted

      property.set("!");

      assertEquals(List.of("!"), peekedValues.drain());  // value change is picked up by peek function

      subscription.unsubscribe();

      assertEquals(List.of(), peekedValues.drain());  // no change

      property.set("Goodbye");
      property.set("Forever");

      assertEquals(List.of(), peekedValues.drain());  // no change as stream is not in use

      eventStream.subscribe(strings::add);

      assertEquals(List.of("Forever"), peekedValues.drain());  // stream subscribed again, and immediately emits current value
    }

    @Test
    void shouldNotAllowRecursiveEmission() {
      property.set("Goodbye");

      ValueStream<String> eventStream = Values.of(property);

      Consumer<? super String> sideEffect = s -> {
        if("Hello".equals(s)) {
          property.set("World");
        }
      };

      eventStream.peek(sideEffect)
        .subscribe(strings::add);

      assertEquals(List.of("Goodbye"), strings.drain());

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
    void shouldRejectNullConsumer() {
      assertThrows(NullPointerException.class, () -> Values.of(property).peek(null));
    }
  }

  @Nested
  class ToBinding {

    @Test
    void shouldTrackValuesEmittedInStream() {
      Binding<String> binding = Values.of(property).toBinding();

      assertNull(binding.getValue());
      assertTrue(binding.isValid());

      property.set("Hello");

      assertEquals("Hello", binding.getValue());

      binding.dispose();

      property.set("World");

      assertEquals("Hello", binding.getValue());  // unchanged as binding was disposed, resulting in it unsubscribing itself
    }
  }

  @Nested
  class Transactional {

    @Test
    void shouldDelayEmittingWhenInTransaction() {
      property.set("1");

      Values.of(property)
        .transactional()
        .subscribe(strings::add);

      assertEquals("1", strings.single());

      property.set("2");

      assertEquals("2", strings.single());

      Transactions.doWhile(() -> {
        property.set("3");
        property.set("4");

        assertTrue(strings.isEmpty());  // verify nothing is emitted yet
      });

      assertEquals("4", strings.single());
    }
  }
}
