package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.core.impl.RootEventStream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public interface Events {

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

  /**
   * Constructs an empty {@link EventStream} which never emits anything.
   *
   * @param <T> the type of values the stream emits
   * @return an {@link EventStream} which never emits anything, never null
   */
  static <T> EventStream<T> empty() {
    return RootEventStream.empty();
  }
}
