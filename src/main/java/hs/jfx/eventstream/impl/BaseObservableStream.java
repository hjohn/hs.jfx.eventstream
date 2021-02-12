package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.util.ListHelper;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class BaseObservableStream<T> implements ObservableStream<T> {
  private ListHelper<Consumer<? super T>> observers;
  private Subscription inputSubscription;

  @Override
  public final void addObserver(Consumer<? super T> observer) {
    if(ListHelper.size(observers) == 0) { // TODO why not just check if inputSubscriptoin is null?
      inputSubscription = observeInputs();
    }

    // TODO requireNonNull is too late here, if it is null, then observeInputs has been called already!
    observers = ListHelper.add(observers, Objects.requireNonNull(observer));

    sendInitialEvent(observer);
  }

  @Override
  public final void removeObserver(Consumer<? super T> observer) {
    observers = ListHelper.remove(observers, Objects.requireNonNull(observer));

    if(ListHelper.isEmpty(observers) && inputSubscription != null) {
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
