package hs.jfx.eventstream;

import hs.jfx.eventstream.domain.ValueStream;
import hs.jfx.eventstream.impl.RootValueStream;

import javafx.beans.value.ObservableValue;

/**
 * Constructs {@link ValueStream}s.
 */
public class Values {

  /**
   * Constructs a {@link ValueStream}, with values of type <code>T</code>,
   * from a given {@link ObservableValue}.  The returned stream will
   * emit the current value of the observable immediately for every subscriber
   * and then on every change.
   *
   * @param <T> the type of values the stream emits
   * @param observable an {@link ObservableValue} used as source for the stream, cannot be null
   * @return a {@link ValueStream} which uses the given {@link ObservableValue} as source, never null
   */
  public static <T> ValueStream<T> of(ObservableValue<T> observable) {
    return RootValueStream.of(observable);
  }

  /**
   * Constructs a {@link ValueStream}, with values of type <code>T</code>,
   * which emits the given value exactly once upon each subscription.
   *
   * @param <T> the type of values the stream emits
   * @param value a value to emit upon each subscription
   * @return a {@link ValueStream} which emits the given value exactly once upon each subscription, never null
   */
  public static <T> ValueStream<T> constant(T value) {
    return RootValueStream.constant(value);
  }
}
