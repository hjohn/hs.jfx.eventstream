package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.util.Sink;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InvalidationStreamTest {
  private final StringProperty property = new SimpleStringProperty();

  @Test
  void shouldNotEmitEventsWhenUnsubscribed() {
    Sink<Object> invalidations = new Sink<>();

    Subscription subscription = Invalidations.of(property)
      .subscribe(invalidations::add);

    assertTrue(invalidations.isEmpty());

    property.set("A");
    property.set("B");
    property.set("C");

    assertEquals(1, invalidations.drain().size());  // only one event, as invalidations only trigger after validation

    property.set("D");

    assertTrue(invalidations.isEmpty());  // nothing as wasn't validated yet

    property.get();  // validates property
    property.set("E");

    assertEquals(1, invalidations.drain().size());  // expect one invalidation again

    subscription.unsubscribe();

    property.get();  // validates property
    property.set("B");

    assertTrue(invalidations.isEmpty());
  }

  @Nested
  class IntermediateOperations {

    @Nested
    class Replace {
      private final Sink<String> strings = new Sink<>();

      @Test
      void shouldConvertValues() {
        Invalidations.of(property)
          .replace(() -> (property.get() == null || property.get().length() == 0) ? "" : "" + (int)property.get().charAt(0))
          .subscribe(strings::add);

        assertTrue(strings.isEmpty());  // nothing on subscribe

        property.set("A");

        assertEquals(List.of("65"), strings.drain());
      }

      @Test
      void shouldRejectNullSupplier() {
        assertThrows(NullPointerException.class, () -> Invalidations.of(property).replace(null));
      }
    }
  }
}
