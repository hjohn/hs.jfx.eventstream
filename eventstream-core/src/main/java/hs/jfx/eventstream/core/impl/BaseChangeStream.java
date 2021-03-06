package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.ValueStream;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.value.ObservableValue;

/**
 * Base class for change streams.
 *
 * @param <S> type of values emitted by the source stream
 * @param <T> type of values emitted by this stream
 */
public class BaseChangeStream<S, T> extends BaseObservableStream<T> implements ChangeStream<T> {

  public BaseChangeStream(Subscriber<T> subscriber) {
    super(subscriber);
  }

  @Override
  public ChangeStream<T> filter(Predicate<? super T> predicate) {
    return FilterStreams.change(this, predicate);
  }

  @Override
  public EventStream<T> filterNull() {
    return FilterNullStreams.event(this);
  }

  @Override
  public ValueStream<T> withDefaultGet(Supplier<? extends T> defaultValueSupplier) {
    return DefaultStreams.value(this, defaultValueSupplier);
  }

  @Override
  public <U> ChangeStream<U> map(Function<? super T, ? extends U> mapper) {
    return MapStreams.change(this, mapper, () -> null);
  }

  @Override
  public <U> ChangeStream<U> flatMap(Function<? super T, ? extends ChangeStream<? extends U>> mapper) {
    return FlatMapStreams.change(this, mapper, () -> null);
  }

  @Override
  public ChangeStream<T> peek(Consumer<? super T> sideEffect) {
    return PeekStreams.change(this, sideEffect);
  }

  @Override
  public ChangeStream<T> orElseGet(Supplier<? extends T> valueSupplier) {
    return MapStreams.change(this, Function.identity(), valueSupplier);
  }

  @Override
  public ChangeStream<T> conditionOn(ObservableValue<Boolean> condition) {
    return RootValueStream.of(condition)
      .flatMapToChange(c -> c ? this : null);  // no need to deal with c being null
  }
}
