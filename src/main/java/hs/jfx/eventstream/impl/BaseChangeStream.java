package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.ChangeStream;
import hs.jfx.eventstream.ObservableStream;
import hs.jfx.eventstream.Subscription;

import java.util.function.Consumer;

/**
 * Base class for event streams.
 *
 * @param <T> type of events emitted by this event stream
 */
public class BaseChangeStream<S, T> extends BaseObservableStream<T> implements ChangeStream<T> {
  private final ObservableStream<S> source;
  private final Action<S, T> action;

  public BaseChangeStream(ObservableStream<S> source, Action<S, T> action) {
    this.source = source;
    this.action = action;
  }

  @Override
  protected final Subscription observeInputs() {
    return action.observeInputs(source, this::emit);
  }

  @Override
  protected final void sendInitialEvent(Consumer<? super T> observer) {
    // Change Streams donot send an initial event
  }
}
