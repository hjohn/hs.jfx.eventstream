package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.ObservableStream;
import hs.jfx.eventstream.Subscription;
import hs.jfx.eventstream.ValueStream;

import java.util.function.Consumer;

/**
 * Base class for event streams.
 *
 * @param <T> type of events emitted by this event stream
 */
public class BaseValueStream<S, T> extends BaseObservableStream<T> implements ValueStream<T> {
  private static final Object NULL_EVENT = new Object();

  @SuppressWarnings("unchecked")
  public static <T> T nullEvent() {
    return (T)NULL_EVENT;
  }

  private final ObservableStream<S> source;
  private final Action<S, T> action;

  public BaseValueStream(ObservableStream<S> source, Action<S, T> action) {
    this.source = source;
    this.action = action;
  }

  @Override
  protected final Subscription observeInputs() {
    return action.observeInputs(source, this::emit);
  }

  @Override
  protected void sendInitialEvent(Consumer<? super T> observer) {
    T currentValue = getCurrentValue();

    if(currentValue != NULL_EVENT) {
      observer.accept(currentValue);
    }
  }

  @Override
  public T getCurrentValue() {
    // Source is guaranteed to be a non-null ValueStream because the only way it could be something else is if its
    // parent was a ChangeStream, which is only allowed for DefaultStream and that one overrides getCurrentValue...
    S currentValue = ((ValueStream<S>)source).getCurrentValue();  // TODO could cast to BaseValueStream once all implementations inherit from that

    return currentValue == NULL_EVENT ? nullEvent() : action.operate(currentValue);
  }
}
