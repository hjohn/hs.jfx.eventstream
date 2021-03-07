package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.Subscription;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class RootChangeStream<T> extends BaseChangeStream<T, T> {
  private static final ChangeStream<?> EMPTY = new RootChangeStream<>(emitter -> Subscription.EMPTY);

  public static <T> RootChangeStream<T> of(ObservableValue<T> observable) {
    return RootChangeStream.of(emitter -> {
      ChangeListener<T> listener = (obs, old, current) -> emitter.emit(current);

      observable.addListener(listener);

      return () -> observable.removeListener(listener);
    });
  }

  public static <T> RootChangeStream<T> of(Subscriber<T> subscriber) {
    return new RootChangeStream<>(subscriber);
  }

  private RootChangeStream(Subscriber<T> subscriber) {
    super(subscriber);
  }

  @SuppressWarnings("unchecked")
  public static <T> ChangeStream<T> empty() {
    return (ChangeStream<T>)EMPTY;
  }
}
