package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscription;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class MapStream {

  public static class Change<S, T> extends BaseChangeStream<S, T> {
    public Change(ObservableStream<S> source, Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
      super(new MapSubscriber<>(source, mapper, nullReplacement));
    }
  }

  public static class Value<S, T> extends BaseValueStream<S, T> {
    public Value(ObservableStream<S> source, Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
      super(new MapSubscriber<>(source, mapper, nullReplacement));
    }
  }

  private static class MapSubscriber<S, T> extends Subscriber<S, T> {
    private final Function<? super S, ? extends T> mapper;
    private final Supplier<? extends T> nullReplacement;

    public MapSubscriber(ObservableStream<S> source, Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
      super(source);

      this.mapper = Objects.requireNonNull(mapper);
      this.nullReplacement = Objects.requireNonNull(nullReplacement);
    }

    @Override
    public Subscription observeInputs(Emitter<T> emitter) {
      return getSource().subscribe(value -> emitter.emit(operate(value).get()));
    }

    @Override
    public OptionalValue<T> operate(S value) {
      return OptionalValue.of(value == null ? nullReplacement.get() : mapper.apply(value));
    }
  }
}