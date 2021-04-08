package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.impl.BaseChangeStream;

/**
 * A source for a {@link ChangeStream} which emits exactly the values pushed
 * into it.
 *
 * @param <T> the type of values the stream emits
 */
public class ChangeSource<T> extends BaseChangeStream<T, T> {

  /**
   * Constructs a new instance.
   */
  public ChangeSource() {
    super(e -> Subscription.EMPTY);
  }

  /**
   * Emits the given value to subscribers of this stream.
   *
   * @param value a value to emit
   */
  public void push(T value) {
    emit(value);
  }
}
