package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.util.Sink;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BaseObservableStreamTest {
  private final BaseObservableStream<String> stream = new BaseObservableStream<>() {
    @Override
    protected Subscription observeInputs() {
      observeInputsCalls++;

      return () -> unsubscribeCalls++;
    }

    @Override
    protected void sendInitialEvent(Consumer<? super String> observer) {
      sendInitialEventCalls++;
    }
  };

  private int observeInputsCalls;
  private int unsubscribeCalls;
  private int sendInitialEventCalls;

  @Nested
  class WhenObserverAdded {
    private final Sink<String> strings = new Sink<>();
    private final Consumer<String> observer = strings::add;

    {
      stream.addObserver(observer);
    }

    @Test
    void shouldCallSendInitialEvent() {
      assertEquals(1, sendInitialEventCalls);
    }

    @Test
    void shouldEmitValues() {
      stream.emit("X");

      assertEquals(List.of("X"), strings.drain());
    }

    @Nested
    class AndSameObserverIsAddedAgain {
      {
        stream.addObserver(observer);
      }

      @Test
      void shouldCallSendInitialEventAgain() {
        assertEquals(2, sendInitialEventCalls);
      }

      @Test
      void shouldEmitEachValueTwice() {
        stream.emit("X");

        assertEquals(List.of("X", "X"), strings.drain());
      }

      @Nested
      class AndOneObserverIsRemoved {
        {
          stream.removeObserver(observer);
        }

        @Test
        void shouldNotCallSendInitialEventAgain() {
          assertEquals(2, sendInitialEventCalls);  // still 2 from the 2 addObserver calls
        }

        @Test
        void shouldEmitEachValueOnce() {
          stream.emit("X");

          assertEquals(List.of("X"), strings.drain());
        }
      }
    }

    @Nested
    class AndObserverIsRemoved {
      {
        stream.removeObserver(observer);
      }

      @Test
      void shouldHaveCalledSendInitialEventOnceStill() {
        assertEquals(1, sendInitialEventCalls);
      }

      @Test
      void shouldCallUnsubscribeOnce() {
        assertEquals(1, unsubscribeCalls);
      }

      @Test
      void shouldNoLongerEmitValues() {
        stream.emit("X");

        assertEquals(List.of(), strings.drain());
      }

      @Nested
      class AndWhenSameObserverIsRemovedAgain {
        {
          stream.removeObserver(observer);
        }

        @Test
        void shouldHaveCalledSendInitialEventOnceStill() {
          assertEquals(1, sendInitialEventCalls);
        }

        @Test
        void shouldNotCallUnsubscribeAgain() {
          assertEquals(1, unsubscribeCalls);
        }
      }
    }
  }

  @Nested
  class RemoveObserver {
    @Test
    void shouldRejectRemovingNull() {
      assertThrows(NullPointerException.class, () -> stream.removeObserver(null));
    }
  }

  @Nested
  class AddObserver {
    @Test
    void shouldRejectNullObserver() {
      assertThrows(NullPointerException.class, () -> stream.addObserver(null));
      assertEquals(0, observeInputsCalls);
    }
  }
}
