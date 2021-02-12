package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.ChangeAction;
import hs.jfx.eventstream.domain.Emitter;
import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;

import java.util.Objects;
import java.util.function.Predicate;

public class FilterStream<T> extends BaseChangeStream<T, T> {
  public FilterStream(ObservableStream<T> source, Predicate<? super T> predicate) {
    super(Objects.requireNonNull(source), new ChangeAction<>() {
      @Override
      public Subscription observeInputs(ObservableStream<T> source, Emitter<T> emitter) {
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