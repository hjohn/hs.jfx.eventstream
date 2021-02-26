package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.ValueStream;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class MapStreams {

  public static <S, T> ChangeStream<T> change(ObservableStream<S> source, Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
    Operator<S, T> operator = operator(Objects.requireNonNull(mapper), Objects.requireNonNull(nullReplacement));
    Subscriber<T> subscriber = subscriber(source, operator);

    return new BaseChangeStream<>(source, subscriber);
  }

  public static <S, T> ValueStream<T> value(ObservableStream<S> source, Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
    Operator<S, T> operator = operator(Objects.requireNonNull(mapper), Objects.requireNonNull(nullReplacement));
    Subscriber<T> subscriber = subscriber(source, operator);

    return new BaseValueStream<>(source, subscriber, operator);
  }

  private static <S, T> Operator<S, T> operator(Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
    return value -> OptionalValue.of(value == null ? nullReplacement.get() : mapper.apply(value));
  }

  private static <S, T> Subscriber<T> subscriber(ObservableStream<S> source, Operator<S, T> operator) {
    return emitter -> source.subscribe(value -> emitter.emit(operator.operate(value).get()));
  }
}