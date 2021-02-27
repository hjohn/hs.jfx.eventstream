package hs.jfx.eventstream.experimental.impl;

import hs.jfx.eventstream.api.Emitter;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.core.impl.BaseChangeStream;
import hs.jfx.eventstream.core.impl.BaseEventStream;
import hs.jfx.eventstream.core.impl.BaseValueStream;
import hs.jfx.eventstream.experimental.Transactions;

public abstract class TransactionalStream {

  public static class Event<T> extends BaseEventStream<T, T> {
    public Event(ObservableStream<T> source) {
      super(source, new TransactionalSubscriber<>(source));
    }
  }

  public static class Change<T> extends BaseChangeStream<T, T> {
    public Change(ObservableStream<T> source) {
      super(source, new TransactionalSubscriber<>(source));
    }
  }

  public static class Value<T> extends BaseValueStream<T, T> {
    public Value(ObservableStream<T> source) {
      super(source, new TransactionalSubscriber<>(source), OptionalValue::of);
    }
  }

  private static class TransactionalSubscriber<T> implements Subscriber<T> {
    private final ObservableStream<T> source;

    private T storedEvent;
    private Subscription transactionFinishedSubscription;

    public TransactionalSubscriber(ObservableStream<T> source) {
      this.source = source;
    }

    @Override
    public Subscription subscribe(Emitter<T> emitter) {
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
  }
}