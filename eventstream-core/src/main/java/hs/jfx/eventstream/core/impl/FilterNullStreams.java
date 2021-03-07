package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscriber;

public abstract class FilterNullStreams {

  public static <T> EventStream<T> event(ObservableStream<T> source) {
    return new BaseEventStream<>(subscriber(source));
  }

  private static <T> Subscriber<T> subscriber(ObservableStream<T> source) {
    return emitter -> source.subscribe(v -> {
      if(v != null) {
        emitter.emit(v);
      }
    });
  }
}