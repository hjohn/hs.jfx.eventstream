package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.util.Sink;

import java.util.List;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvalidationsTest {
  private final StringProperty observable1 = new SimpleStringProperty("A");
  private final StringProperty observable2 = new SimpleStringProperty("1");

  @Nested
  class WhenOfCalledWithObservablesReturnsEventStreamWhich {
    private final EventStream<Observable> stream = Invalidations.of(observable1, observable2);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturnsSubscriptionWhich {
      private final Sink<Observable> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveInvalidations() {
        observable1.set("B");

        assertEquals(List.of(observable1), sink.drain());

        observable2.set("2");

        assertEquals(List.of(observable2), sink.drain());
      }

      @Nested
      class WhenInvalidated {
        {
          observable1.set("B");
          sink.drain();
        }

        @Test
        void shouldNotReceiveInvalidationAgain() {
          observable1.set("C");

          assertTrue(sink.isEmpty());
        }

        @Nested
        class WhenValidated {
          {
            observable1.get();
          }

          @Test
          void shouldReceiveInvalidationsAgain() {
            observable1.set("D");

            assertEquals(List.of(observable1), sink.drain());
          }
        }
      }

      @Nested
      class AfterUnsubscribe {
        {
          subscription.unsubscribe();
        }

        @Test
        void shouldNoLongerReceiveInvalidations() {
          observable1.set("B");

          assertTrue(sink.isEmpty());
        }
      }
    }
  }
}
