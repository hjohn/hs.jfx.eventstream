package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.ValueStream;
import hs.jfx.eventstream.core.impl.RootValueStream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableValue;

/**
 * Constructs {@link ValueStream}s.
 */
public interface Values {

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
  static <T> ValueStream<T> of(ObservableValue<T> observable) {
    return RootValueStream.of(observable);
  }

  /**
   * Constructs an {@link ValueStream}, with values of type <code>Integer</code>, from a given {@link ObservableIntegerValue}.
   * The returned stream will emit the current value of the observable immediately for every subscriber
   * and then on every change.
   *
   * @param observable an {@link ObservableIntegerValue} used as source for the stream, cannot be null
   * @return a {@link ValueStream} which uses the given {@link ObservableIntegerValue} as source, never null
   */
  static ValueStream<Integer> of(ObservableIntegerValue observable) {
    return withCast(observable, Integer.class);
  }

  /**
   * Constructs an {@link ValueStream}, with values of type <code>Long</code>, from a given {@link ObservableLongValue}.
   * The returned stream will emit the current value of the observable immediately for every subscriber
   * and then on every change.
   *
   * @param observable an {@link ObservableLongValue} used as source for the stream, cannot be null
   * @return a {@link ValueStream} which uses the given {@link ObservableLongValue} as source, never null
   */
  static ValueStream<Long> of(ObservableLongValue observable) {
    return withCast(observable, Long.class);
  }

  /**
   * Constructs an {@link ValueStream}, with values of type <code>Float</code>, from a given {@link ObservableFloatValue}.
   * The returned stream will emit the current value of the observable immediately for every subscriber
   * and then on every change.
   *
   * @param observable an {@link ObservableFloatValue} used as source for the stream, cannot be null
   * @return a {@link ValueStream} which uses the given {@link ObservableFloatValue} as source, never null
   */
  static ValueStream<Float> of(ObservableFloatValue observable) {
    return withCast(observable, Float.class);
  }

  /**
   * Constructs an {@link ValueStream}, with values of type <code>Double</code>, from a given {@link ObservableDoubleValue}.
   * The returned stream will emit the current value of the observable immediately for every subscriber
   * and then on every change.
   *
   * @param observable an {@link ObservableDoubleValue} used as source for the stream, cannot be null
   * @return a {@link ValueStream} which uses the given {@link ObservableDoubleValue} as source, never null
   */
  static ValueStream<Double> of(ObservableDoubleValue observable) {
    return withCast(observable, Double.class);
  }

  /**
   * Constructs a {@link ValueStream}, with values of type <code>T</code>,
   * which emits the given value exactly once upon each subscription.
   *
   * @param <T> the type of values the stream emits
   * @param value a value to emit upon each subscription
   * @return a {@link ValueStream} which emits the given value exactly once upon each subscription, never null
   */
  static <T> ValueStream<T> constant(T value) {
    return RootValueStream.constant(value);
  }

  private static <S, T> ValueStream<T> withCast(ObservableValue<S> observable, Class<T> cls) {
    return RootValueStream.of(
      emitter -> {
        ChangeListener<S> listener = (obs, old, current) -> emitter.emit(cls.cast(current));

        observable.addListener(listener);

        return () -> observable.removeListener(listener);
      },
      () -> cls.cast(observable.getValue())
    );
  }
}
