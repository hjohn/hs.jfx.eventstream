package hs.jfx.eventstream;

import hs.jfx.eventstream.domain.ChangeStream;
import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.domain.ValueStream;
import hs.jfx.eventstream.util.Sink;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangeStreamTest {
  private final StringProperty property = new SimpleStringProperty();
  private final Sink<String> strings = new Sink<>();

  @Nested
  class ConditionOn {

    @Test
    void shouldEmitValuesConditionally() {
      property.set("Bye");

      BooleanProperty visible = new SimpleBooleanProperty(true);
      ChangeStream<String> stream = Changes.of(property)
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
  }

  @Nested
  class Empty {

    @Test
    void shouldNeverEmitAnything() {
      ChangeStream<String> stream = Changes.empty();

      stream.subscribe(strings::add);

      assertTrue(strings.isEmpty());
    }
  }

  @Nested
  class FilterOperation {

    @Test
    void shouldSkipFilteredValues() {
      Changes.of(property)
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
      Changes.of(property)
        .filter(TestUtil::filterFailOnNull)
        .subscribe(strings::add);

      assertTrue(strings.isEmpty());
    }

    @Test
    void shouldRejectNullPredicate() {
      assertThrows(NullPointerException.class, () -> Changes.of(property).filter(null));
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

      ChangeStream<Boolean> stream = Changes.of(node.scene)
        .flatMap(s -> Changes.of(s.window))
        .flatMap(w -> Changes.of(w.showing));

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

      Changes.of(property)
        .flatMap(TestUtil::changeFlatMapFailOnNull)
        .subscribe(strings::add);

      property.set(null);

      assertTrue(strings.isEmpty());

      property.set("B");

      assertTrue(strings.isEmpty());
    }

    @Test
    void shouldRejectNullFunction() {
      assertThrows(NullPointerException.class, () -> Changes.of(property).flatMap(null));
    }
  }

  @Nested
  class MapOperation {

    @Test
    void shouldConvertValues() {
      Changes.of(property)
        .map(s -> "" + (int)s.charAt(0))
        .subscribe(strings::add);

      property.set("A");

      assertEquals(List.of("65"), strings.drain());
    }

    @Test
    void shouldSkipNulls() {
      property.set("A");

      Changes.of(property)
        .map(TestUtil::mapFailOnNull)
        .subscribe(strings::add);

      property.set(null);

      assertNull(strings.single());
    }

    @Test
    void shouldRejectNullFunction() {
      assertThrows(NullPointerException.class, () -> Changes.of(property).map(null));
    }
  }

  @Nested
  class OrElseOperation {

    @Test
    void shouldReplaceNulls() {
      Changes.of(property)
        .orElse("(null)")
        .subscribe(strings::add);

      assertTrue(strings.isEmpty());

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

      ChangeStream<String> eventStream = Changes.of(property)
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

      ChangeStream<String> eventStream = Changes.of(property);

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
    void shouldRejectNullConsumer() {
      assertThrows(NullPointerException.class, () -> Changes.of(property).peek(null));
    }
  }

  @Nested
  class WithDefaultGetOperation {

    @Test
    void shouldSupplyDefaultToNewSubscribers() {
      ValueStream<String> eventStream = Changes.of(property)
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
      assertThrows(NullPointerException.class, () -> Changes.of(property).withDefaultGet((Supplier<String>)null));
    }
  }

  @Nested
  class WithDefaultOperation {

    @Test
    void shouldSupplyDefaultToNewSubscribers() {
      ValueStream<String> eventStream = Changes.of(property)
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
