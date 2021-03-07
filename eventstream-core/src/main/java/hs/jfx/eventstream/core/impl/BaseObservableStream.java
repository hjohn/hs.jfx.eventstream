package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.api.ValueStream;
import hs.jfx.eventstream.core.util.ListHelper;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base class for observable streams.
 *
 * @param <S> type of values emitted by the source stream (if any)
 * @param <T> type of values emitted by this stream
 */
public abstract class BaseObservableStream<S, T> implements ObservableStream<T> {
  private final ObservableStream<S> source;
  private final Subscriber<T> subscriber;
  private final Operator<S, T> operator;

  private ListHelper<Consumer<? super T>> observers;
  private Subscription inputSubscription;

  public BaseObservableStream(ObservableStream<S> source, Subscriber<T> subscriber, Operator<S, T> operator) {
    this.source = source;
    this.subscriber = subscriber;
    this.operator = operator;
  }

  @Override
  public final void addObserver(Consumer<? super T> observer) {
    if(observer == null) {
      throw new NullPointerException("observer cannot be null");
    }

    if(inputSubscription == null) {
      inputSubscription = subscriber.subscribe(this::emit);
    }

    if(operator != null) {
      sendInitialEvent(observer);
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
    return getCurrentValue();
  }

  private void sendInitialEvent(Consumer<? super T> observer) {
    getCurrentValue().ifPresent(observer::accept);
  }

  private OptionalValue<T> getCurrentValue() {
    return source == null ? operator.operate(null) : ((ValueStream<S>)source).getInitialValue().flatMap(operator::operate);
  }
}
