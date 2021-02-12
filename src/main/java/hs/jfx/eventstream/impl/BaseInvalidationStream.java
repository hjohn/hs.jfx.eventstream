package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.InvalidationStream;
import hs.jfx.eventstream.ObservableStream;
import hs.jfx.eventstream.Subscription;

import java.util.function.Consumer;

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
}
