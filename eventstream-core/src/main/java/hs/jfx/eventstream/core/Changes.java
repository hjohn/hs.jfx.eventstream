package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.core.impl.RootChangeStream;

import javafx.beans.value.ChangeListener;
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
   * Constructs a {@link ChangeStream}, with values of type <code>Change&lt;T&gt;</code>, from a given {@link ObservableValue}.
   * The returned stream will emit every change of the observable.
   *
   * @param <T> the type of {@link Change} the stream emits
   * @param observable an {@link ObservableValue} used as source for the stream, cannot be null
   * @return a {@link ChangeStream} which uses the given {@link ObservableValue} as source, never null
   */
  static <T> ChangeStream<Change<T>> diff(ObservableValue<T> observable) {
    return RootChangeStream.of(emitter -> {
      ChangeListener<T> listener = (obs, old, current) -> emitter.emit(Change.of(old, current));

      observable.addListener(listener);

      return () -> observable.removeListener(listener);
    });
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
