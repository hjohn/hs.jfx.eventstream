package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscription;

import java.util.Objects;
import java.util.function.Predicate;

public class FilterStream<T> extends BaseChangeStream<T, T> {
  public FilterStream(ObservableStream<T> source, Predicate<? super T> predicate) {
    super(new Subscriber<>(Objects.requireNonNull(source)) {
      @Override
      public Subscription observeInputs(Emitter<T> emitter) {
        return source.subscribe(v -> {
          if(v == null || predicate.test(v)) {
            emitter.emit(v);
          }
        });
      }
    });

    Objects.requireNonNull(predicate);
  }
}