package hs.jfx.eventstream;

import hs.jfx.eventstream.domain.ChangeAction;
import hs.jfx.eventstream.domain.Emitter;
import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.impl.BaseChangeStream;

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
