package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.Action;
import hs.jfx.eventstream.domain.Emitter;
import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class MapStream {

  public static class Change<S, T> extends BaseChangeStream<S, T> {
    public Change(ObservableStream<S> source, Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
      super(source, new MapAction<>(mapper, nullReplacement));
    }
  }

  public static class Value<S, T> extends BaseValueStream<S, T> {
    public Value(ObservableStream<S> source, Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
      super(source, new MapAction<>(mapper, nullReplacement));
    }
  }

  private static class MapAction<S, T> implements Action<S, T> {
    private final Function<? super S, ? extends T> mapper;
    private final Supplier<? extends T> nullReplacement;

    public MapAction(Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
      this.mapper = Objects.requireNonNull(mapper);
      this.nullReplacement = Objects.requireNonNull(nullReplacement);
    }

    @Override
    public Subscription observeInputs(ObservableStream<S> source, Emitter<T> emitter) {
      return source.subscribe(value -> emitter.emit(operate(value)));
    }

    @Override
    public T operate(S value) {
      return value == null ? nullReplacement.get() : mapper.apply(value);
    }
  }
}