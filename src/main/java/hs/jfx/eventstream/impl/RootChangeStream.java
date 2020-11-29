package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.Observable;
import hs.jfx.eventstream.Subscription;

import java.util.function.Function;

public class RootChangeStream<T> extends BaseChangeStream<T, T> {

  public RootChangeStream(Function<Emitter<T>, Subscription> subscriber) {
    super(null, new ChangeAction<>() {
      @Override
      public Subscription observeInputs(Observable<T> source, Emitter<T> emitter) {
        return subscriber.apply(emitter);
      }
    });
  }
}
