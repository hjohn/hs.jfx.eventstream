package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.Subscriber;

public class RootEventStream<T> extends BaseEventStream<T, T> {

  public static <T> RootEventStream<T> of(Subscriber<T> subscriber) {
    return new RootEventStream<>(emitter -> subscriber.subscribe(v -> {
      if(v != null) {
        emitter.emit(v);
      }
    }));
  }

  private RootEventStream(Subscriber<T> subscriber) {
    super(subscriber);
  }
}
