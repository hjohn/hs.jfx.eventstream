package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscriber;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class FilterStreams {

  public static <T> EventStream<T> event(ObservableStream<T> source, Predicate<? super T> predicate) {
    return new BaseEventStream<>(subscriber(source, Objects.requireNonNull(predicate)));
  }

  public static <T> ChangeStream<T> change(ObservableStream<T> source, Predicate<? super T> predicate) {
    return new BaseChangeStream<>(nullSafeSubscriber(source, Objects.requireNonNull(predicate)));
  }

  private static <T> Subscriber<T> nullSafeSubscriber(ObservableStream<T> source, Predicate<? super T> predicate) {
    return emitter -> source.subscribe(v -> {
      if(v == null || predicate.test(v)) {
        emitter.emit(v);
      }
    });
  }

  private static <T> Subscriber<T> subscriber(ObservableStream<T> source, Predicate<? super T> predicate) {
    return emitter -> source.subscribe(v -> {
      if(predicate.test(v)) {
        emitter.emit(v);
      }
    });
  }
}