package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;

import java.util.Objects;
import java.util.function.Supplier;

public class DefaultStream<T> extends BaseValueStream<T, T> {
  private final Supplier<T> defaultValueSupplier;

  public DefaultStream(ObservableStream<T> source, Supplier<T> defaultValueSupplier) {
    super(Objects.requireNonNull(source), new DefaultAction<>());

    this.defaultValueSupplier = Objects.requireNonNull(defaultValueSupplier);
  }

  @Override
  public T getCurrentValue() {
    return defaultValueSupplier.get();
  }

  private static class DefaultAction<T> implements Action<T, T> {

    @Override
    public Subscription observeInputs(ObservableStream<T> source, Emitter<T> emitter) {
      return source.subscribe(emitter::emit);
    }

    @Override
    public T operate(T value) {
      throw new UnsupportedOperationException();
    }
  }
}
