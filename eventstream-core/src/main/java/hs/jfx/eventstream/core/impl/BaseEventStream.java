package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.ValueStream;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.value.ObservableValue;

/**
 * Base class for event streams.
 *
 * @param <S> type of values emitted by the source stream
 * @param <T> type of values emitted by this stream
 */
public class BaseEventStream<S, T> extends BaseObservableStream<S, T> implements EventStream<T> {

  public BaseEventStream(ObservableStream<S> source, Subscriber<T> subscriber) {
    super(source, subscriber, null);
  }

  @Override
  public EventStream<T> filter(Predicate<? super T> predicate) {
    return FilterStreams.event(this, predicate);
  }

  @Override
  public ValueStream<T> withDefaultGet(Supplier<? extends T> defaultValueSupplier) {
    return DefaultStreams.value(this, defaultValueSupplier);
  }

  @Override
  public <U> EventStream<U> map(Function<? super T, ? extends U> mapper) {
    return MapStreams.event(this, mapper);
  }

  @Override
  public <U> EventStream<U> flatMap(Function<? super T, ? extends EventStream<? extends U>> mapper) {
    return FlatMapStreams.event(this, mapper);
  }

  @Override
  public EventStream<T> peek(Consumer<? super T> sideEffect) {
    return PeekStreams.event(this, sideEffect);
  }

  @Override
  public EventStream<T> conditionOn(ObservableValue<Boolean> condition) {
    return RootValueStream.of(condition)
      .flatMapToEvent(c -> c ? this : RootEventStream.empty());
  }
}
