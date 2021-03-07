package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.util.ReplaceCamelCaseDisplayNameGenerator;
import hs.jfx.eventstream.core.util.Sink;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(ReplaceCamelCaseDisplayNameGenerator.class)
public class ChangesTest {

  @Nested
  class WhenOfCalledWith_ObservableValue_Returns_EventStream_Which {
    private final StringProperty property = new SimpleStringProperty("A");
    private final EventStream<Change<String>> stream = Changes.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Change<String>> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveChanges() {
        property.set("B");

        Change<String> change = sink.single();

        assertEquals("A", change.getOldValue());
        assertEquals("B", change.getValue());
      }

      @Nested
      class AfterUnsubscribe {
        {
          subscription.unsubscribe();
        }

        @Test
        void shouldNoLongerReceiveChanges() {
          property.set("B");

          assertTrue(sink.isEmpty());
        }
      }
    }
  }

  @Nested
  class WhenOfCalledWith_ObservableIntegerValue_Returns_EventStream_Which {
    private final IntegerProperty property = new SimpleIntegerProperty();
    private final EventStream<Change<Integer>> stream = Changes.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Change<Integer>> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveChanges() {
        property.set(Integer.MAX_VALUE);

        Change<Integer> change = sink.single();

        assertEquals(0, change.getOldValue());
        assertEquals(Integer.MAX_VALUE, change.getValue());
      }

      @Nested
      class AfterUnsubscribe {
        {
          subscription.unsubscribe();
        }

        @Test
        void shouldNoLongerReceiveChanges() {
          property.set(1);

          assertTrue(sink.isEmpty());
        }
      }
    }
  }

  @Nested
  class WhenOfCalledWith_ObservableLongValue_Returns_EventStream_Which {
    private final LongProperty property = new SimpleLongProperty();
    private final EventStream<Change<Long>> stream = Changes.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Change<Long>> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveChanges() {
        property.set(Long.MAX_VALUE);

        Change<Long> change = sink.single();

        assertEquals(0, change.getOldValue());
        assertEquals(Long.MAX_VALUE, change.getValue());
      }

      @Nested
      class AfterUnsubscribe {
        {
          subscription.unsubscribe();
        }

        @Test
        void shouldNoLongerReceiveChanges() {
          property.set(1);

          assertTrue(sink.isEmpty());
        }
      }
    }
  }

  @Nested
  class WhenOfCalledWith_ObservableFloatValue_Returns_EventStream_Which {
    private final FloatProperty property = new SimpleFloatProperty();
    private final EventStream<Change<Float>> stream = Changes.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Change<Float>> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveChanges() {
        property.set(1.5f);

        Change<Float> change = sink.single();

        assertEquals(0, change.getOldValue());
        assertEquals(1.5f, change.getValue());
      }

      @Nested
      class AfterUnsubscribe {
        {
          subscription.unsubscribe();
        }

        @Test
        void shouldNoLongerReceiveChanges() {
          property.set(1);

          assertTrue(sink.isEmpty());
        }
      }
    }
  }

  @Nested
  class WhenOfCalledWith_ObservableDoubleValue_Returns_EventStream_Which {
    private final DoubleProperty property = new SimpleDoubleProperty();
    private final EventStream<Change<Double>> stream = Changes.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Change<Double>> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveChanges() {
        property.set(1.5);

        Change<Double> change = sink.single();

        assertEquals(0, change.getOldValue());
        assertEquals(1.5, change.getValue());
      }

      @Nested
      class AfterUnsubscribe {
        {
          subscription.unsubscribe();
        }

        @Test
        void shouldNoLongerReceiveChanges() {
          property.set(1);

          assertTrue(sink.isEmpty());
        }
      }
    }
  }
}
