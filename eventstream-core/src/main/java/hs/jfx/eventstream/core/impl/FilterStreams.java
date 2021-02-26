package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscriber;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class FilterStreams {

  public static <T> ChangeStream<T> change(ObservableStream<T> source, Predicate<? super T> predicate) {
    return new BaseChangeStream<>(source, subscriber(source, Objects.requireNonNull(predicate)));
  }

  private static <T> Subscriber<T> subscriber(ObservableStream<T> source, Predicate<? super T> predicate) {
    return emitter -> source.subscribe(v -> {
      if(v == null || predicate.test(v)) {
        emitter.emit(v);
      }
    });
  }
}