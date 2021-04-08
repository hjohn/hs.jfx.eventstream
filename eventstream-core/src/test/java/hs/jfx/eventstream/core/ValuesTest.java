package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.api.ValueStream;
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
public class ValuesTest {

  @Nested
  class WhenConstantCalledReturns_ValueStream_Which {
    private final ValueStream<Integer> stream = Values.constant(42);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Integer> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldImmediatelyReceiveCurrentValue() {
        assertEquals(42, sink.single());
      }
    }
  }

  @Nested
  class WhenOfCalledWith_ObservableValue_Returns_ValueStream_Which {
    private final StringProperty property = new SimpleStringProperty("A");
    private final ValueStream<String> stream = Values.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<String> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveInitialValueFirstAndThenAnyChanges() {
        assertEquals("A", sink.single());

        property.set("B");

        assertEquals("B", sink.single());
      }

      @Nested
      class AfterUnsubscribe {
        {
          sink.single();  // clear initial value
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
  class WhenOfCalledWith_ObservableIntegerValue_Returns_ValueStream_Which {
    private final IntegerProperty property = new SimpleIntegerProperty();
    private final ValueStream<Integer> stream = Values.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Integer> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveInitialValueFirstAndThenAnyChanges() {
        assertEquals(0, sink.single());

        property.set(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, sink.single());
      }

      @Nested
      class AfterUnsubscribe {
        {
          sink.single();  // clear initial value
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
  class WhenOfCalledWith_ObservableLongValue_Returns_ValueStream_Which {
    private final LongProperty property = new SimpleLongProperty();
    private final ValueStream<Long> stream = Values.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Long> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveInitialValueFirstAndThenAnyChanges() {
        assertEquals(0, sink.single());

        property.set(Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE, sink.single());
      }

      @Nested
      class AfterUnsubscribe {
        {
          sink.single();  // clear initial value
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
  class WhenOfCalledWith_ObservableFloatValue_Returns_ValueStream_Which {
    private final FloatProperty property = new SimpleFloatProperty();
    private final ValueStream<Float> stream = Values.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Float> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveInitialValueFirstAndThenAnyChanges() {
        assertEquals(0.0f, sink.single());

        property.set(1.5f);

        assertEquals(1.5f, sink.single());
      }

      @Nested
      class AfterUnsubscribe {
        {
          sink.single();  // clear initial value
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
  class WhenOfCalledWith_ObservableDoubleValue_Returns_ValueStream_Which {
    private final DoubleProperty property = new SimpleDoubleProperty();
    private final ValueStream<Double> stream = Values.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturns_Subscription_Which {
      private final Sink<Double> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveInitialValueFirstAndThenAnyChanges() {
        assertEquals(0.0, sink.single());

        property.set(1.5);

        assertEquals(1.5, sink.single());
      }

      @Nested
      class AfterUnsubscribe {
        {
          sink.single();  // clear initial value
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
