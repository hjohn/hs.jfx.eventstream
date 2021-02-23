package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscription;

import java.util.Objects;
import java.util.function.Supplier;

public class DefaultStream<T> extends BaseValueStream<T, T> {

  public DefaultStream(ObservableStream<T> source, Supplier<? extends T> defaultValueSupplier) {
    super(new Subscriber<>(Objects.requireNonNull(defaultValueSupplier)) {
      @Override
      protected Subscription observeInputs(Emitter<T> emitter) {
        return source.subscribe(emitter::emit);
      }
    });

    Objects.requireNonNull(source);
  }
}
