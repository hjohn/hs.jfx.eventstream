package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.util.Sink;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangesTest {
  private final StringProperty property = new SimpleStringProperty("A");

  @Nested
  class WhenEmptyCalledReturnsChangeStreamWhich {
    private final ChangeStream<String> stream = Changes.empty();

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturnsSubscriptionWhich {
      private final Sink<String> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }
    }
  }

  @Nested
  class WhenCallingOfReturnsChangeStreamWhich {
    private final ChangeStream<String> stream = Changes.of(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturnsSubscriptionWhich {
      private final Sink<String> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveChanges() {
        property.set("B");

        assertEquals(List.of("B"), sink.drain());
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
  class WhenCallingDiffReturnsChangeStreamWhich {
    private final ChangeStream<Change<String>> stream = Changes.diff(property);

    @Test
    void shouldNotBeNull() {
      assertNotNull(stream);
    }

    @Nested
    class WhenSubscribedReturnsSubscriptionWhich {
      private final Sink<Change<String>> sink = new Sink<>();
      private final Subscription subscription = stream.subscribe(sink::add);

      @Test
      void shouldNotBeNull() {
        assertNotNull(subscription);
      }

      @Test
      void shouldReceiveChanges() {
        property.set("B");

        assertEquals(List.of(Change.of("A", "B")), sink.drain());
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
