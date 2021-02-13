package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscription;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class PeekStream {

    public static class Change<T> extends BaseChangeStream<T, T> {
      public Change(ObservableStream<T> source, Consumer<? super T> sideEffect) {
        super(source, new PeekAction<>(sideEffect));
      }
    }

    public static class Value<T> extends BaseValueStream<T, T> {
      public Value(ObservableStream<T> source, Consumer<? super T> sideEffect) {
        super(source, new PeekAction<>(sideEffect));
      }
    }

    private static class PeekAction<T> implements Action<T, T> {
      private final Consumer<? super T> sideEffect;

      private boolean sideEffectInProgress = false;

      public PeekAction(Consumer<? super T> sideEffect) {
        this.sideEffect = Objects.requireNonNull(sideEffect);
      }

      @Override
      public Subscription observeInputs(ObservableStream<T> source, Emitter<T> emitter) {
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

      @Override
      public T operate(T value) {
        return value;
      }
    }
}