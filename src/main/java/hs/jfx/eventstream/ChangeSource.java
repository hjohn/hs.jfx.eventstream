package hs.jfx.eventstream;

import hs.jfx.eventstream.domain.ChangeStream;
import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.impl.BaseChangeStream;
import hs.jfx.eventstream.impl.ChangeAction;
import hs.jfx.eventstream.impl.Emitter;

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
    super(null, new ChangeAction<>() {
      @Override
      public Subscription observeInputs(ObservableStream<T> source, Emitter<T> emitter) {
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
