package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.core.impl.RootChangeStream;

import javafx.beans.value.ObservableValue;

/**
 * Constructs {@link ChangeStream}s.
 */
public interface Changes {

  /**
   * Constructs a {@link ChangeStream}, with values of type <code>T</code>, from a given {@link ObservableValue}.
   * The returned stream will emit every change of the observable.
   *
   * @param <T> the type of values the stream emits
   * @param observable an {@link ObservableValue} used as source for the stream, cannot be null
   * @return a {@link ChangeStream} which uses the given {@link ObservableValue} as source, never null
   */
  static <T> ChangeStream<T> of(ObservableValue<T> observable) {
    return RootChangeStream.of(observable);
  }

  /**
   * Constructs an {@link ChangeStream}, with values of type <code>T</code>, using the given
   * {@link Subscriber} to subscribe to a source. The returned stream will emit all
   * values supplied by the subscription.
   *
   * @param <T> the type of values emitted by this stream
   * @param subscriber a {@link Subscriber} supplying the subscription for the stream, cannot be null
   * @return a {@link ChangeStream} which uses the given {@link Subscriber} to subscribe to a source, never null
   */
  static <T> ChangeStream<T> of(Subscriber<T> subscriber) {
    return RootChangeStream.of(subscriber);
  }

  /**
   * Constructs an empty {@link ChangeStream} which never emits anything.
   *
   * @param <T> the type of values the stream emits
   * @return a {@link ChangeStream} which never emits anything, never null
   */
  static <T> ChangeStream<T> empty() {
    return RootChangeStream.empty();
  }
}
