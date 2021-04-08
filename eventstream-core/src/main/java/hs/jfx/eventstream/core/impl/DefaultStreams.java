package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.ValueStream;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class DefaultStreams {

  public static <T> ValueStream<T> value(ObservableStream<T> source, Supplier<? extends T> defaultValueSupplier) {
    Objects.requireNonNull(defaultValueSupplier);

    return new BaseValueStream<>(e -> source.subscribe(e::emit), null, v -> OptionalValue.of(defaultValueSupplier.get()));
  }
}
