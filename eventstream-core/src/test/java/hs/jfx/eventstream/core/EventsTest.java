package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.util.ReplaceCamelCaseDisplayNameGenerator;
import hs.jfx.eventstream.core.util.Sink;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(ReplaceCamelCaseDisplayNameGenerator.class)
class EventsTest {
  private final StringProperty property = new SimpleStringProperty("A");

  @Nested
  class WhenOfCalledWith_ObservableValue_Returns_EventStream_Which {
    private final EventStream<Change<String>> stream = Events.of(property);

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
  class WhenOfCalledWith_Subscriber_Returns_EventStream_Which {
    private final EventStream<String> stream = Events.of(emitter -> {
      ChangeListener<String> listener = (obs, old, current) -> emitter.emit(current);

      property.addListener(listener);

      return () -> property.removeListener(listener);
    });

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
      void shouldReceiveChanges() {
        property.set("B");

        assertEquals("B", sink.single());
      }

      @Test
      void shouldSkipNullValue() {  // as per contract of event streams
        property.set(null);

        assertTrue(sink.isEmpty());
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
}
