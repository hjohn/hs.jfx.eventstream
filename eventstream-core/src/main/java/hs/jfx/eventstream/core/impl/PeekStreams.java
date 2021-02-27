package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.Emitter;
import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.api.ValueStream;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class PeekStreams {

  public static <T> EventStream<T> event(ObservableStream<T> source, Consumer<? super T> sideEffect) {
    return new BaseEventStream<>(source, subscriber(source, Objects.requireNonNull(sideEffect)));
  }

  public static <T> ChangeStream<T> change(ObservableStream<T> source, Consumer<? super T> sideEffect) {
    return new BaseChangeStream<>(source, subscriber(source, Objects.requireNonNull(sideEffect)));
  }

  public static <T> ValueStream<T> value(ObservableStream<T> source, Consumer<? super T> sideEffect) {
    return new BaseValueStream<>(source, subscriber(source, Objects.requireNonNull(sideEffect)), OptionalValue::of);
  }

  private static <T> Subscriber<T> subscriber(ObservableStream<T> source, Consumer<? super T> sideEffect) {
    return new Subscriber<>() {
      private boolean sideEffectInProgress = false;

      @Override
      public Subscription subscribe(Emitter<T> emitter) {
        return source.subscribe(t -> {
          if(sideEffectInProgress) {
            throw new IllegalStateException("Side effect is not allowed to cause recursive event emission");
          }

          sideEffectInProgress = true;

          try {
            sideEffect.accept(t);
          }
          finally {
            sideEffectInProgress = false;
          }

          emitter.emit(t);
        });
      }
    };
  }
}