package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.util.ListHelper;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base class for observable streams.
 *
 * @param <T> type of values emitted by this stream
 */
public abstract class BaseObservableStream<T> implements ObservableStream<T> {
  private final Subscriber<?, T> subscriber;
  private final boolean sendInitialEvent;

  private ListHelper<Consumer<? super T>> observers;
  private Subscription inputSubscription;

  public BaseObservableStream(Subscriber<?, T> subscriber, boolean sendInitialEvent) {
    this.subscriber = subscriber;
    this.sendInitialEvent = sendInitialEvent;
  }

  @Override
  public final void addObserver(Consumer<? super T> observer) {
    if(observer == null) {
      throw new NullPointerException("observer cannot be null");
    }

    if(inputSubscription == null) {
      inputSubscription = subscriber.observeInputs(this::emit);
    }

    if(sendInitialEvent) {
      subscriber.sendInitialEvent(observer);
    }

    observers = ListHelper.add(observers, observer);
  }

  @Override
  public final void removeObserver(Consumer<? super T> observer) {
    observers = ListHelper.remove(observers, Objects.requireNonNull(observer));

    if(ListHelper.isEmpty(observers) && inputSubscription != null) {  // null check required here as it is possible to unregister another observer when there none
      inputSubscription.unsubscribe();
      inputSubscription = null;
    }
  }

  /**
   * Emits the given value to subscribers of this stream.
   *
   * @param value a value to emit
   */
  protected final void emit(T value) {
    Iterator<Consumer<? super T>> iterator = ListHelper.iterator(observers);

    while(iterator.hasNext()) {
      Consumer<? super T> observer = iterator.next();

      observer.accept(value);
    }
  }

  protected final OptionalValue<T> determineCurrentValue() {
    return subscriber.getCurrentValue();
  }
}
