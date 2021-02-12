package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.Action;
import hs.jfx.eventstream.domain.ChangeStream;
import hs.jfx.eventstream.domain.InvalidationStream;
import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.domain.ValueStream;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base class for event streams.
 *
 * @param <T> type of events emitted by this event stream
 */
public class BaseInvalidationStream extends BaseObservableStream<Void> implements InvalidationStream {
  private final ObservableStream<Void> source;
  private final Action<Void, Void> action;

  public BaseInvalidationStream(ObservableStream<Void> source, Action<Void, Void> action) {
    this.source = source;
    this.action = action;
  }

  @Override
  protected Subscription observeInputs() {
    return action.observeInputs(source, this::emit);
  }

  @Override
  protected final void sendInitialEvent(Consumer<? super Void> observer) {
    // Invalidation Streams donot send an initial event
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

  @Override
  public InvalidationStream transactional() {
    return new TransactionalStream.Invalidation(this);
  }
}
