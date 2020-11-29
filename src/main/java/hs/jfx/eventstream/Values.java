package hs.jfx.eventstream;

import hs.jfx.eventstream.impl.Action;
import hs.jfx.eventstream.impl.BaseValueStream;
import hs.jfx.eventstream.impl.Emitter;

import javafx.beans.value.ObservableValue;

public class Values {

  /**
   * Creates an event stream that emits the current value of the given
   * {@code ObservableValue} immediately for every subscriber and then on
   * every change.
   *
   * @param <T> type of values emitted by the new stream
   * @param observable an observable which serves as a value source for the new event stream
   * @return an event stream that emits the value of the given
   * {@code ObservableValue} immediately for every subscriber and then on
   * every change
   */
  public static <T> ValueStream<T> of(ObservableValue<T> observable) {
    return Changes.of(observable).withDefaultGet(observable::getValue);
  }

  public static <T> ValueStream<T> constant(T value) {
    return Changes.<T>empty().withDefault(value);
  }

  public static <T> ValueStream<T> empty() {
    return new BaseValueStream<>(null, new Action<T, T>() {

      @Override
      public Subscription observeInputs(Observable<T> source, Emitter<T> emitter) {
        return Subscription.EMPTY;
      }

      @Override
      public T operate(T value) {
        throw new UnsupportedOperationException();
      }
    }) {
      @Override
      public T getCurrentValue() {
        return nullEvent();
      }
    };
  }
}
