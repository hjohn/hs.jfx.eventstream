package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.core.impl.RootEventStream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableValue;

/**
 * Constructs streams with changes presented by {@link Change} objects.
 */
public interface Changes {

  /**
   * Constructs an {@link EventStream}, with values of type <code>Change&lt;T&gt;</code>, from a given {@link ObservableValue}.
   * The returned stream will emit every change of the observable as a {@link Change}.
   *
   * @param <T> the type of values in {@link Change}
   * @param observable an {@link ObservableValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableValue} as source, never null
   */
  static <T> EventStream<Change<T>> of(ObservableValue<T> observable) {
    return RootEventStream.of(emitter -> {
      ChangeListener<T> listener = (obs, old, current) -> emitter.emit(Change.of(old, current));

      observable.addListener(listener);

      return () -> observable.removeListener(listener);
    });
  }

  /**
   * Constructs an {@link EventStream}, with values of type <code>Change&lt;Integer&gt;</code>, from a given {@link ObservableIntegerValue}.
   * The returned stream will emit every change of the observable as a {@link Change}.
   *
   * @param observable an {@link ObservableIntegerValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableIntegerValue} as source, never null
   */
  static EventStream<Change<Integer>> of(ObservableIntegerValue observable) {
    return withCast(observable, Integer.class);
  }

  /**
   * Constructs an {@link EventStream}, with values of type <code>Change&lt;Long&gt;</code>, from a given {@link ObservableLongValue}.
   * The returned stream will emit every change of the observable as a {@link Change}.
   *
   * @param observable an {@link ObservableLongValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableLongValue} as source, never null
   */
  static EventStream<Change<Long>> of(ObservableLongValue observable) {
    return withCast(observable, Long.class);
  }

  /**
   * Constructs an {@link EventStream}, with values of type <code>Change&lt;Float&gt;</code>, from a given {@link ObservableFloatValue}.
   * The returned stream will emit every change of the observable as a {@link Change}.
   *
   * @param observable an {@link ObservableFloatValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableFloatValue} as source, never null
   */
  static EventStream<Change<Float>> of(ObservableFloatValue observable) {
    return withCast(observable, Float.class);
  }

  /**
   * Constructs an {@link EventStream}, with values of type <code>Change&lt;Double&gt;</code>, from a given {@link ObservableDoubleValue}.
   * The returned stream will emit every change of the observable as a {@link Change}.
   *
   * @param observable an {@link ObservableDoubleValue} used as source for the stream, cannot be null
   * @return a {@link EventStream} which uses the given {@link ObservableDoubleValue} as source, never null
   */
  static EventStream<Change<Double>> of(ObservableDoubleValue observable) {
    return withCast(observable, Double.class);
  }

  private static <S, T> EventStream<Change<T>> withCast(ObservableValue<S> observable, Class<T> cls) {
    return RootEventStream.of(emitter -> {
      ChangeListener<S> listener = (obs, old, current) -> emitter.emit(Change.of(cls.cast(old), cls.cast(current)));

      observable.addListener(listener);

      return () -> observable.removeListener(listener);
    });
  }
}
