package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.impl.Subscriber;
import hs.jfx.eventstream.core.impl.BaseChangeStream;
import hs.jfx.eventstream.core.impl.Emitter;

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
    super(new Subscriber<>() {
      @Override
      public Subscription observeInputs(Emitter<T> emitter) {
        return Subscription.EMPTY;
      }
    });
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
