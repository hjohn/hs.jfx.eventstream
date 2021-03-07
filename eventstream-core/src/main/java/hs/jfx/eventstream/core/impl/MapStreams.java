package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.ValueStream;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class MapStreams {

  public static <S, T> EventStream<T> event(ObservableStream<S> source, Function<? super S, ? extends T> mapper) {
    Operator<S, T> operator = nullRejectingOperator(Objects.requireNonNull(mapper));
    Subscriber<T> subscriber = subscriber(source, operator);

    return new BaseEventStream<>(subscriber);
  }

  public static <S, T> ChangeStream<T> change(ObservableStream<S> source, Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
    Operator<S, T> operator = nullSafeOperator(Objects.requireNonNull(mapper), Objects.requireNonNull(nullReplacement));
    Subscriber<T> subscriber = subscriber(source, operator);

    return new BaseChangeStream<>(subscriber);
  }

  public static <S, T> ValueStream<T> value(ObservableStream<S> source, Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
    Operator<S, T> operator = nullSafeOperator(Objects.requireNonNull(mapper), Objects.requireNonNull(nullReplacement));
    Subscriber<T> subscriber = subscriber(source, operator);

    return new BaseValueStream<>(subscriber, source, operator);
  }

  private static <S, T> Operator<S, T> nullSafeOperator(Function<? super S, ? extends T> mapper, Supplier<? extends T> nullReplacement) {
    return value -> OptionalValue.of(value == null ? nullReplacement.get() : mapper.apply(value));
  }

  private static <S, T> Operator<S, T> nullRejectingOperator(Function<? super S, ? extends T> mapper) {
    return value -> OptionalValue.ofNullable(mapper.apply(value));
  }

  private static <S, T> Subscriber<T> subscriber(ObservableStream<S> source, Operator<S, T> operator) {
    return emitter -> source.subscribe(value -> operator.operate(value).ifPresent(emitter::emit));
  }
}