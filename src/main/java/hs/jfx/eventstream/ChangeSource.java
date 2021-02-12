package hs.jfx.eventstream;

import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.impl.BaseChangeStream;
import hs.jfx.eventstream.impl.ChangeAction;
import hs.jfx.eventstream.impl.Emitter;

public class ChangeSource<T> extends BaseChangeStream<T, T> {

  public ChangeSource() {
    super(null, new ChangeAction<>() {
      @Override
      public Subscription observeInputs(ObservableStream<T> source, Emitter<T> emitter) {
        return Subscription.EMPTY;
      }
    });
  }

  public void push(T event) {
    emit(event);
  }
}
