package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.ValueStream;
import hs.jfx.eventstream.core.util.StreamUtil;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;

/**
 * Base class for value streams.
 *
 * @param <S> type of values emitted by the source stream
 * @param <T> type of values emitted by this stream
 */
public class BaseValueStream<S, T> extends BaseObservableStream<S, T> implements ValueStream<T> {

  public BaseValueStream(ObservableStream<S> source, Subscriber<T> subscriber, Operator<S, T> operator) {
    super(source, subscriber, operator);
  }

  @Override
  public ChangeStream<T> filter(Predicate<? super T> predicate) {
    return FilterStreams.change(this, predicate);
  }

  @Override
  public <U> ValueStream<U> map(Function<? super T, ? extends U> mapper) {
    return MapStreams.value(this, mapper, StreamUtil.nullSupplier());
  }

  @Override
  public Binding<T> toBinding() {
    return new ValueStreamBinding<>(this);
  }

  @Override
  public <U> ValueStream<U> flatMap(Function<? super T, ? extends ValueStream<? extends U>> mapper) {
    return FlatMapStreams.value(this, mapper, () -> RootValueStream.constant(null));
  }

  @Override
  public <U> ValueStream<U> bind(Function<? super T, ObservableValue<? extends U>> mapper) {
    Objects.requireNonNull(mapper);

    return FlatMapStreams.value(this, v -> RootValueStream.of(mapper.apply(v)), () -> RootValueStream.constant(null));
  }

  @Override
  public <U> ChangeStream<U> flatMapToChange(Function<? super T, ? extends ChangeStream<? extends U>> mapper) {
    return FlatMapStreams.change(this, mapper, RootChangeStream::empty);
  }

  @Override
  public ValueStream<T> peek(Consumer<? super T> sideEffect) {
    return PeekStreams.value(this, sideEffect);
  }

  @Override
  public ValueStream<T> or(Supplier<? extends ValueStream<? extends T>> supplier) {
    return FlatMapStreams.value(this, v -> this, supplier);
  }

  @Override
  public ValueStream<T> orElseGet(Supplier<? extends T> valueSupplier) {
    return MapStreams.value(this, Function.identity(), valueSupplier);
  }

  @Override
  public ValueStream<T> conditionOn(ObservableValue<Boolean> condition) {

    /*
     * Conditional streams return an empty value stream when the condition does not hold.
     * This is intended behavior. A custom FlatMapStream is created here because the normal
     * flatmapping behavior would return a constant null stream instead of an empty one.
     */

    return FlatMapStreams.value(
      RootValueStream.of(condition),
      c -> c ? this : empty(),
      () -> empty()
    );
  }

  /**
   * Returns a {@link ValueStream} which never emits anything, which goes against
   * the general contract of a value stream. This is only used for {@link #conditionOn(ObservableValue)}
   * which documents this behavior.
   *
   * @param <T> the type of values the stream emits
   * @return a {@link ValueStream} which never emits anything, never null
   */
  @SuppressWarnings("unchecked")
  private static <T> ValueStream<T> empty() {
    return (ValueStream<T>)RootValueStream.EMPTY;
  }

  @Override
  public OptionalValue<T> getCurrentValue() {
    return determineCurrentValue();
  }
}
