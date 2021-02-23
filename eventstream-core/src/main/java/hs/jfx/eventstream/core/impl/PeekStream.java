package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscription;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class PeekStream {

  public static class Change<T> extends BaseChangeStream<T, T> {
    public Change(ObservableStream<T> source, Consumer<? super T> sideEffect) {
      super(new PeekSubscriber<>(source, sideEffect));
    }
  }

  public static class Value<T> extends BaseValueStream<T, T> {
    public Value(ObservableStream<T> source, Consumer<? super T> sideEffect) {
      super(new PeekSubscriber<>(source, sideEffect));
    }
  }

  private static class PeekSubscriber<T> extends Subscriber<T, T> {
    private final Consumer<? super T> sideEffect;

    private boolean sideEffectInProgress = false;

    public PeekSubscriber(ObservableStream<T> source, Consumer<? super T> sideEffect) {
      super(source);

      this.sideEffect = Objects.requireNonNull(sideEffect);
    }

    @Override
    public Subscription observeInputs(Emitter<T> emitter) {
      return getSource().subscribe(t -> {
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
    public OptionalValue<T> operate(T value) {
      return OptionalValue.of(value);
    }
  }
}