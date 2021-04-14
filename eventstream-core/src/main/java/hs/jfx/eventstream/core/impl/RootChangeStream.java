package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.Subscriber;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class RootChangeStream<T> extends BaseChangeStream<T, T> {

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
}
