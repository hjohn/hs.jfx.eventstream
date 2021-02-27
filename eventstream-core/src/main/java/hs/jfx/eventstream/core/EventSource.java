package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.impl.BaseEventStream;

import java.util.Objects;

/**
 * A source for a {@link EventStream} which emits exactly the values pushed
 * into it. Note that event streams donot allow {@code null} values.
 *
 * @param <T> the type of values the stream emits
 */
public class EventSource<T> extends BaseEventStream<T, T> {

  /**
   * Constructs a new instance.
   */
  public EventSource() {
    super(null, e -> Subscription.EMPTY);
  }

  /**
   * Emits the given value to subscribers of this stream.
   *
   * @param value a value to emit, cannot be null
   */
  public void push(T value) {
    emit(Objects.requireNonNull(value));
  }
}
