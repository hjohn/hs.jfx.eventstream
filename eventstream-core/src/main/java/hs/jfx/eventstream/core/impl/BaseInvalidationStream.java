package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.InvalidationStream;
import hs.jfx.eventstream.api.ValueStream;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Base class for invalidation streams.
 *
 * @param <T> type of values emitted by this stream
 */
public class BaseInvalidationStream extends BaseObservableStream<Void> implements InvalidationStream {

  public BaseInvalidationStream(Subscriber<Void, Void> subscriber) {
    super(subscriber, false);
  }

  @Override
  public <T> ChangeStream<T> replace(Supplier<? extends T> supplier) {
    Objects.requireNonNull(supplier);

    return new MapStream.Change<>(this, v -> supplier.get(), supplier);
  }

  @Override
  public ValueStream<Void> withDefault() {
    return new DefaultStream<>(this, () -> null);
  }
}
