package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.Observable;
import hs.jfx.eventstream.Subscription;

import java.util.Objects;
import java.util.function.Supplier;

public class DefaultStream<T> extends BaseValueStream<T, T> {
  private final Supplier<T> defaultValueSupplier;

  public DefaultStream(Observable<T> source, Supplier<T> defaultValueSupplier) {
    super(Objects.requireNonNull(source), new DefaultAction<>());

    this.defaultValueSupplier = Objects.requireNonNull(defaultValueSupplier);
  }

  @Override
  public T getCurrentValue() {
    return defaultValueSupplier.get();
  }

  private static class DefaultAction<T> implements Action<T, T> {

    @Override
    public Subscription observeInputs(Observable<T> source, Emitter<T> emitter) {
      return source.subscribe(emitter::emit);
    }

    @Override
    public T operate(T value) {
      throw new UnsupportedOperationException();
    }
  }
}
