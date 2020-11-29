package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.Observable;
import hs.jfx.eventstream.Subscription;
import hs.jfx.eventstream.Transactions;

public abstract class TransactionalStream {

  public static class Invalidation extends BaseInvalidationStream {
    public Invalidation(Observable<Void> source) {
      super(source, new TransactionalAction<>());
    }
  }

  public static class Change<T> extends BaseChangeStream<T, T> {
    public Change(Observable<T> source) {
      super(source, new TransactionalAction<>());
    }
  }

  public static class Value<T> extends BaseValueStream<T, T> {
    public Value(Observable<T> source) {
      super(source, new TransactionalAction<>());
    }
  }

  private static class TransactionalAction<T> implements Action<T, T> {
    private T storedEvent;
    private Subscription transactionFinishedSubscription;

    @Override
    public Subscription observeInputs(Observable<T> source, Emitter<T> emitter) {
      Subscription subscription = source.subscribe(t -> {
        if(!Transactions.inProgress()) {
          emitter.emit(t);
        }
        else {
          if(transactionFinishedSubscription == null) {
            transactionFinishedSubscription = Transactions.register(() -> {
              emitter.emit(storedEvent);
              invalidateTransaction(); // TODO no need to unsubscribe (because tx clears all), but that's not clear here
            });
          }

          storedEvent = t;  // TODO only emits last captured event, rest is lost -- null is a valid event, but it deals with that fine
        }
      });

      return () -> {
        subscription.unsubscribe();

        if(transactionFinishedSubscription != null) {
          transactionFinishedSubscription.unsubscribe();
          invalidateTransaction();
        }
      };
    }

    private void invalidateTransaction() {
      transactionFinishedSubscription = null;
      storedEvent = null;
    }

    @Override
    public T operate(T value) {
      return value;
    }
  }
}