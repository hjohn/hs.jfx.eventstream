package hs.jfx.eventstream.experimental.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.impl.BaseChangeStream;
import hs.jfx.eventstream.core.impl.BaseInvalidationStream;
import hs.jfx.eventstream.core.impl.BaseValueStream;
import hs.jfx.eventstream.core.impl.Emitter;
import hs.jfx.eventstream.core.impl.Subscriber;
import hs.jfx.eventstream.experimental.Transactions;

public abstract class TransactionalStream {

  public static class Invalidation extends BaseInvalidationStream {
    public Invalidation(ObservableStream<Void> source) {
      super(new TransactionalAction<>(source));
    }
  }

  public static class Change<T> extends BaseChangeStream<T, T> {
    public Change(ObservableStream<T> source) {
      super(new TransactionalAction<>(source));
    }
  }

  public static class Value<T> extends BaseValueStream<T, T> {
    public Value(ObservableStream<T> source) {
      super(new TransactionalAction<>(source));
    }
  }

  private static class TransactionalAction<T> extends Subscriber<T, T> {
    private T storedEvent;
    private Subscription transactionFinishedSubscription;

    public TransactionalAction(ObservableStream<T> source) {
      super(source);
    }

    @Override
    public Subscription observeInputs(Emitter<T> emitter) {
      Subscription subscription = getSource().subscribe(t -> {
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
    public OptionalValue<T> operate(T value) {
      return OptionalValue.of(value);
    }
  }
}