package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.api.ValueStream;

import java.util.function.Function;
import java.util.function.Supplier;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class RootValueStream<T> extends BaseValueStream<T, T> {
  static final RootValueStream<?> EMPTY = new RootValueStream<>(e -> Subscription.EMPTY, null);

  public static <T> RootValueStream<T> of(ObservableValue<T> observable) {
    return new RootValueStream<>(e -> subscribe(e, observable), observable::getValue);
  }

  private static <T> Subscription subscribe(Emitter<T> emitter, ObservableValue<T> observable) {
    ChangeListener<T> listener = (obs, old, current) -> emitter.emit(current);

    observable.addListener(listener);

    return () -> observable.removeListener(listener);
  }

  private RootValueStream(Function<Emitter<T>, Subscription> subscriber, Supplier<T> defaultValueSupplier) {
    super(new Subscriber<>(defaultValueSupplier) {
      @Override
      public Subscription observeInputs(Emitter<T> emitter) {
        return subscriber.apply(emitter);
      }
    });
  }

  public static <T> ValueStream<T> constant(T value) {
    return new RootValueStream<>(e -> Subscription.EMPTY, () -> value);
  }
}
