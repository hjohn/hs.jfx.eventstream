package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.Action;
import hs.jfx.eventstream.domain.Emitter;
import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.domain.ValueStream;

import java.util.function.Function;
import java.util.function.Supplier;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class RootValueStream<T> extends BaseValueStream<T, T> {
  private static final RootValueStream<?> EMPTY = new RootValueStream<>(e -> Subscription.EMPTY, () -> nullEvent());

  private final Supplier<T> defaultValueSupplier;

  public static <T> RootValueStream<T> of(ObservableValue<T> observable) {
    return new RootValueStream<>(e -> subscribe(e, observable), observable::getValue);
  }

  private static <T> Subscription subscribe(Emitter<T> emitter, ObservableValue<T> observable) {
    ChangeListener<T> listener = (obs, old, current) -> emitter.emit(current);

    observable.addListener(listener);

    return () -> observable.removeListener(listener);
  }

  private RootValueStream(Function<Emitter<T>, Subscription> subscriber, Supplier<T> defaultValueSupplier) {
    super(null, new Action<>() {

      @Override
      public Subscription observeInputs(ObservableStream<T> source, Emitter<T> emitter) {
        return subscriber.apply(emitter);
      }

      @Override
      public T operate(T value) {
        throw new UnsupportedOperationException();
      }
    });

    this.defaultValueSupplier = defaultValueSupplier;
  }

  @Override
  public T getCurrentValue() {
    return defaultValueSupplier.get();
  }

  // TODO clarify why this can exist
  @SuppressWarnings("unchecked")
  public static <T> ValueStream<T> empty() {
    return (ValueStream<T>)EMPTY;
  }

  public static <T> ValueStream<T> constant(T value) {
    return new RootValueStream<>(e -> Subscription.EMPTY, () -> value);
  }
}
