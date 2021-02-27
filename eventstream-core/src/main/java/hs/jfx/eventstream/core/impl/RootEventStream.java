package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.Subscription;

public class RootEventStream<T> extends BaseEventStream<T, T> {
  private static final EventStream<?> EMPTY = new RootEventStream<>(emitter -> Subscription.EMPTY);

  public static <T> RootEventStream<T> of(Subscriber<T> subscriber) {
    return new RootEventStream<>(emitter -> subscriber.subscribe(v -> {
      if(v != null) {
        emitter.emit(v);
      }
    }));
  }

  private RootEventStream(Subscriber<T> subscriber) {
    super(null, subscriber);
  }

  @SuppressWarnings("unchecked")
  public static <T> EventStream<T> empty() {
    return (EventStream<T>)EMPTY;
  }
}
