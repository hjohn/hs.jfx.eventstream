package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;

public interface Action<S, T> {
  Subscription observeInputs(ObservableStream<S> source, Emitter<T> emitter);

  /**
   * Used by {@code ValueStream} to determine the value to provide upon
   * subscription.
   *
   * @param value a source value
   * @return the resulting value
   */
  T operate(S value);

  public static <T> Action<T, T> nothing() {
    return new Action<>() {
      @Override
      public Subscription observeInputs(ObservableStream<T> source, Emitter<T> emitter) {
        return Subscription.EMPTY;
      }

      @Override
      public T operate(T value) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
