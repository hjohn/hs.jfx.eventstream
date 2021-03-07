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
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class FlatMapStreams {

  public static <S, T> EventStream<T> event(ObservableStream<S> source, Function<? super S, ? extends EventStream<? extends T>> mapper) {
    Function<? super S, ObservableStream<? extends T>> flatMapper = flatMapper(Objects.requireNonNull(mapper), () -> null);
    Subscriber<T> subscriber = subscriber(source, flatMapper);

    return new BaseEventStream<>(source, subscriber);
  }

  public static <S, T> ChangeStream<T> change(ObservableStream<S> source, Function<? super S, ? extends ChangeStream<? extends T>> mapper, Supplier<? extends ChangeStream<? extends T>> nullReplacement) {
    Function<? super S, ObservableStream<? extends T>> flatMapper = flatMapper(Objects.requireNonNull(mapper), Objects.requireNonNull(nullReplacement));
    Subscriber<T> subscriber = subscriber(source, flatMapper);

    return new BaseChangeStream<>(source, subscriber);
  }

  public static <S, T> ValueStream<T> value(ObservableStream<S> source, Function<? super S, ? extends ValueStream<? extends T>> mapper, Supplier<? extends ValueStream<? extends T>> nullReplacement) {
    Function<? super S, ObservableStream<? extends T>> flatMapper = flatMapper(Objects.requireNonNull(mapper), Objects.requireNonNull(nullReplacement));
    Operator<S, T> operator = operator(flatMapper);
    Subscriber<T> subscriber = subscriber(source, flatMapper);

    return new BaseValueStream<>(source, subscriber, operator);
  }

  private static <S, T> Function<? super S, ObservableStream<? extends T>> flatMapper(Function<? super S, ? extends ObservableStream<? extends T>> mapper, Supplier<? extends ObservableStream<? extends T>> nullReplacement) {
    return input -> input == null ? nullReplacement.get() : mapper.apply(input);
  }

  private static <S, T> Operator<S, T> operator(Function<? super S, ObservableStream<? extends T>> flatMapper) {
    return value -> {
      @SuppressWarnings("unchecked") // cast is safe as operate is only called for ValueStreams
      BaseValueStream<S, T> mappedStream = (BaseValueStream<S, T>)flatMapper.apply(value);

      return mappedStream == null ? OptionalValue.empty() : mappedStream.getInitialValue();
    };
  }

  private static <S, T> Subscriber<T> subscriber(ObservableStream<S> source, Function<? super S, ObservableStream<? extends T>> flatMapper) {
    return new Subscriber<>() {
      private Subscription mappedSubscription = Subscription.EMPTY;

      @Override
      public Subscription subscribe(Emitter<T> emitter) {
        return source.subscribe(value -> {
          ObservableStream<? extends T> newStream = flatMapper.apply(value);

          /*
           * When the flatmapping results in null, an empty stream is tracked (or rather
           * no subscription is made at all). This means effectively that the resulting
           * stream will emit nothing until the source triggers a flatmapping to a
           * different stream.
           *
           * For ValueStreams this can be a bit unexpected, as no value will be emitted.
           * However, the alternative (throwing an exception) does not work well because
           * JavaFX fireValueChangeEvent code will necessarily catch and log this as
           * there is no way to properly let this bubble up to where the stream was
           * created.
           */

          mappedSubscription.unsubscribe();
          mappedSubscription = newStream == null ? Subscription.EMPTY : newStream.subscribe(emitter::emit);
        });
      }
    };
  }
}