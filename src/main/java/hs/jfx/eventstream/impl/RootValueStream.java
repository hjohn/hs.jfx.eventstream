package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.Observable;
import hs.jfx.eventstream.Subscription;

import java.util.function.Function;

// TODO currently unused
public class RootValueStream<T> extends BaseValueStream<T, T> {

  public RootValueStream(Function<Emitter<T>, Subscription> subscriber) {
    super(null, new Action<>() {

      @Override
      public Subscription observeInputs(Observable<T> source, Emitter<T> emitter) {
        return subscriber.apply(emitter);
      }

      @Override
      public T operate(T value) {
        return value;
      }
    });
  }
}
