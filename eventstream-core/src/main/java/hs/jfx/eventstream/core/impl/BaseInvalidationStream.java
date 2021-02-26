package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.InvalidationStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.ValueStream;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Base class for invalidation streams.
 */
public class BaseInvalidationStream extends BaseObservableStream<Void, Void> implements InvalidationStream {

  public BaseInvalidationStream(ObservableStream<Void> source, Subscriber<Void> subscriber) {
    super(source, subscriber, null);
  }

  @Override
  public <T> ChangeStream<T> replace(Supplier<? extends T> supplier) {
    Objects.requireNonNull(supplier);

    return MapStreams.change(this, v -> supplier.get(), supplier);
  }

  @Override
  public ValueStream<Void> withDefault() {
    return DefaultStreams.value(this, () -> null);
  }
}
