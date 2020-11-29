package hs.jfx.eventstream;

import hs.jfx.eventstream.impl.RootChangeStream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Changes {
  private static final ChangeStream<?> EMPTY = new RootChangeStream<>(emitter -> Subscription.EMPTY);

  public static <T> ChangeStream<T> of(ObservableValue<T> observable) {
    return new RootChangeStream<>(emitter -> {
      ChangeListener<T> listener = (obs, old, current) -> emitter.emit(current);

      observable.addListener(listener);

      return () -> observable.removeListener(listener);
    });
  }

  public static <T> ChangeStream<Change<T>> diff(ObservableValue<T> observable) {
    return new RootChangeStream<>(emitter -> {
      ChangeListener<T> listener = (obs, old, current) -> emitter.emit(new Change<>(old, current));

      observable.addListener(listener);

      return () -> observable.removeListener(listener);
    });
  }

  @SuppressWarnings("unchecked")
  public static <T> ChangeStream<T> empty() {
    return (ChangeStream<T>)EMPTY;
  }
}
