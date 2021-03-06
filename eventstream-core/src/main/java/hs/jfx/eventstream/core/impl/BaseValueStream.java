package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscriber;
import hs.jfx.eventstream.api.ValueStream;

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
public class BaseValueStream<S, T> extends BaseObservableStream<T> implements ValueStream<T> {
  private final ObservableStream<S> source;
  private final Operator<S, T> operator;

  public BaseValueStream(Subscriber<T> subscriber, ObservableStream<S> source, Operator<S, T> operator) {
    super(subscriber);

    this.source = source;
    this.operator = operator;
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
  public <U> ValueStream<U> map(Function<? super T, ? extends U> mapper) {
    return MapStreams.value(this, mapper, () -> null);
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
  public <U> ChangeStream<U> flatMapToChange(Function<? super T, ? extends ChangeStream<? extends U>> mapper) {
    return FlatMapStreams.change(this, mapper, () -> null);
  }

  public <U> EventStream<U> flatMapToEvent(Function<? super T, ? extends EventStream<? extends U>> mapper) {
    return FlatMapStreams.event(this, mapper);
  }

  @Override
  public ValueStream<T> peek(Consumer<? super T> sideEffect) {
    return PeekStreams.value(this, sideEffect);
  }

  @Override
  public ValueStream<T> or(Supplier<? extends ValueStream<? extends T>> supplier) {

    /*
     * When using a flatmap to implement 'or', the source stream is subscribed
     * twice (once as the source stream and once as the mapped stream). When
     * the source stream emits null, the mapper switches to the alternative
     * stream unsubscribing the mapped source stream subscription. However,
     * this is too late and the mapped subscription will also receive the null,
     * which it passes on downstream along with current value of the alternative
     * stream, effectively resulting in the downstream subscribers receiving two
     * values for one change: the intended value + an (unexpected) null.
     *
     * To prevent this, the source stream is not mapped to directly but instead
     * a derived stream is returned which skips all nulls. Although null is a
     * valid value for subscribers to receive, in this case skipping the nulls
     * is fine as the values from the alternative stream are supposed to be used
     * in that case anyway.
     */

    return FlatMapStreams.value(this, v -> skipNulls(), supplier);
  }

  private ValueStream<T> skipNulls() {
    return new BaseValueStream<>(
      emitter -> subscribe(v -> {
        if(v != null) {
          emitter.emit(v);
        }
      }),
      this,
      OptionalValue::of
    );
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
      c -> c ? this : null,
      () -> null
    );
  }

  @Override
  public OptionalValue<T> getInitialValue() {
    return source == null ? operator.operate(null) : ((ValueStream<S>)source).getInitialValue().flatMap(operator::operate);
  }

  @Override
  protected void newObserverAdded(Consumer<? super T> observer) {
    getInitialValue().ifPresent(observer::accept);
  }
}
