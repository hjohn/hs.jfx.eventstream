package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.Emitter;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.api.ValueStream;

import java.util.function.Supplier;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class RootValueStream<T> extends BaseValueStream<T, T> {
  static final RootValueStream<?> EMPTY = new RootValueStream<>(e -> Subscription.EMPTY, null);

  public static <T> RootValueStream<T> of(ObservableValue<T> observable) {
    return new RootValueStream<>(e -> subscribe(e, observable), observable::getValue);
  }

  public static <T> RootValueStream<T> of(Subscriber<T> subscriber, Supplier<T> defaultValueSupplier) {
    return new RootValueStream<>(subscriber, defaultValueSupplier);
  }

  private static <T> Subscription subscribe(Emitter<T> emitter, ObservableValue<T> observable) {
    ChangeListener<T> listener = (obs, old, current) -> emitter.emit(current);

    observable.addListener(listener);

    return () -> observable.removeListener(listener);
  }

  private RootValueStream(Subscriber<T> subscriber, Supplier<T> defaultValueSupplier) {
    super(subscriber, null, v -> defaultValueSupplier == null ? OptionalValue.empty() : OptionalValue.of(defaultValueSupplier.get()));
  }

  public static <T> ValueStream<T> constant(T value) {
    return new RootValueStream<>(e -> Subscription.EMPTY, () -> value);
  }
}
