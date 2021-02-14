package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.util.ListHelper;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class BaseObservableStream<T> implements ObservableStream<T> {
  private ListHelper<Consumer<? super T>> observers;
  private Subscription inputSubscription;

  @Override
  public final void addObserver(Consumer<? super T> observer) {
    if(observer == null) {
      throw new NullPointerException("observer cannot be null");
    }

    if(inputSubscription == null) {
      inputSubscription = observeInputs();
    }

    observers = ListHelper.add(observers, Objects.requireNonNull(observer));

    sendInitialEvent(observer);
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
   * Emits the given value as an event for this event stream.
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

  /**
   * Starts observing this observable's input(s), if any. This method is called when the number of observers goes from 0
   * to 1.
   *
   * @return subscription used to stop observing inputs. The subscription is unsubscribed (i.e. input observation stops)
   *         when the number of observers goes down to 0.
   */
  protected abstract Subscription observeInputs();
  protected abstract void sendInitialEvent(Consumer<? super T> observer);
}
