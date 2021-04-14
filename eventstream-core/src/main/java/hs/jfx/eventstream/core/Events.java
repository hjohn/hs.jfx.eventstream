package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.core.impl.RootEventStream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableValue;

/**
 * Constructs {@link EventStream}s.
 */
public interface Events {

  /**
   * Constructs an {@link EventStream}, with values of type <code>T</code>, from a given {@link ObservableValue}.
   * The returned stream will emit every new value of the observable.
   *
   * @param <T> the type of values
   * @param observable an {@link ObservableValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableValue} as source, never null
   */
  static <T> EventStream<T> of(ObservableValue<T> observable) {
    return RootEventStream.of(emitter -> {
      ChangeListener<T> listener = (obs, old, current) -> emitter.emit(current);

      observable.addListener(listener);

      return () -> observable.removeListener(listener);
    });
  }

  /**
   * Constructs an {@link EventStream}, with values of type <code>T</code>, from a given {@link ObservableIntegerValue}.
   * The returned stream will emit every change of the observable.
   *
   * @param observable an {@link ObservableIntegerValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableValue} as source, never null
   */
  static EventStream<Integer> of(ObservableIntegerValue observable) {
    return withCast(observable, Integer.class);
  }

  /**
   * Constructs an {@link EventStream}, with values of type <code>T</code>, from a given {@link ObservableLongValue}.
   * The returned stream will emit every change of the observable.
   *
   * @param observable an {@link ObservableLongValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableValue} as source, never null
   */
  static EventStream<Long> of(ObservableLongValue observable) {
    return withCast(observable, Long.class);
  }

  /**
   * Constructs an {@link EventStream}, with values of type <code>T</code>, from a given {@link ObservableFloatValue}.
   * The returned stream will emit every change of the observable.
   *
   * @param observable an {@link ObservableFloatValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableValue} as source, never null
   */
  static EventStream<Float> of(ObservableFloatValue observable) {
    return withCast(observable, Float.class);
  }

  /**
   * Constructs an {@link EventStream}, with values of type <code>T</code>, from a given {@link ObservableDoubleValue}.
   * The returned stream will emit every change of the observable.
   *
   * @param observable an {@link ObservableDoubleValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableValue} as source, never null
   */
  static EventStream<Double> of(ObservableDoubleValue observable) {
    return withCast(observable, Double.class);
  }

  /**
   * Constructs an {@link EventStream}, with values of type <code>T</code>, using the given
   * {@link Subscriber} to subscribe to a source. The returned stream will emit all not null
   * values supplied by the subscription.
   *
   * @param <T> the type of values emitted by this stream
   * @param subscriber a {@link Subscriber} supplying the subscription for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link Subscriber} to subscribe to a source, never null
   */
  static <T> EventStream<T> of(Subscriber<T> subscriber) {
    return RootEventStream.of(subscriber);
  }

  private static <S, T> EventStream<T> withCast(ObservableValue<S> observable, Class<T> cls) {
    return RootEventStream.of(emitter -> {
      ChangeListener<S> listener = (obs, old, current) -> emitter.emit(cls.cast(current));

      observable.addListener(listener);

      return () -> observable.removeListener(listener);
    });
  }
}
