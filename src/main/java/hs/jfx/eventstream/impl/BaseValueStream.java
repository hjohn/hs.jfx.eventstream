package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.Action;
import hs.jfx.eventstream.domain.ChangeStream;
import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.domain.ValueStream;
import hs.jfx.eventstream.util.StreamUtil;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;

/**
 * Base class for event streams.
 *
 * @param <T> type of events emitted by this event stream
 */
public class BaseValueStream<S, T> extends BaseObservableStream<T> implements ValueStream<T> {
  private static final Object NULL_EVENT = new Object();

  @SuppressWarnings("unchecked")
  public static <T> T nullEvent() {
    return (T)NULL_EVENT;
  }

  private final ObservableStream<S> source;
  private final Action<S, T> action;

  public BaseValueStream(ObservableStream<S> source, Action<S, T> action) {
    this.source = source;
    this.action = action;
  }

  @Override
  protected final Subscription observeInputs() {
    return action.observeInputs(source, this::emit);
  }

  @Override
  protected void sendInitialEvent(Consumer<? super T> observer) {
    T currentValue = getCurrentValue();

    if(currentValue != NULL_EVENT) {
      observer.accept(currentValue);
    }
  }

  @Override
  public T getCurrentValue() {
    // Source is guaranteed to be a non-null ValueStream because the only way it could be something else is if its
    // parent was a ChangeStream, which is only allowed for DefaultStream and that one overrides getCurrentValue...
    S currentValue = ((ValueStream<S>)source).getCurrentValue();  // TODO could cast to BaseValueStream once all implementations inherit from that

    return currentValue == NULL_EVENT ? nullEvent() : action.operate(currentValue);
  }

  @Override
  public ChangeStream<T> filter(Predicate<? super T> filter) {
    return new FilterStream<>(this, filter);
  }

  @Override
  public <U> ValueStream<U> map(Function<? super T, ? extends U> mapper) {
    return new MapStream.Value<>(this, mapper, StreamUtil.nullSupplier());
  }

  @Override
  public Binding<T> toBinding() {
    return new ValueStreamBinding<>(this);
  }

  @Override
  public <U> ValueStream<U> flatMap(Function<? super T, ? extends ValueStream<? extends U>> mapper) {
    return new FlatMapStream.Value<>(this, mapper, () -> RootValueStream.constant((U)null));  // TODO check if this constant is correct
  }

  // Convienence function...
  @Override
  public <U> ValueStream<U> bind(Function<? super T, ObservableValue<? extends U>> mapper) {
    Objects.requireNonNull(mapper);

    return new FlatMapStream.Value<>(this, v -> RootValueStream.of(mapper.apply(v)), () -> RootValueStream.constant((U)null));// TODO check if this constant is correct
  }

  @Override
  public <U> ChangeStream<U> flatMapToChange(Function<? super T, ? extends ChangeStream<? extends U>> mapper) {
    return new FlatMapStream.Change<>(this, mapper, RootChangeStream::empty);
  }

  @Override
  public ValueStream<T> peek(Consumer<? super T> sideEffect) {
    return new PeekStream.Value<>(this, sideEffect);
  }

  @Override
  public ValueStream<T> or(Supplier<? extends ValueStream<? extends T>> supplier) {
    return new FlatMapStream.Value<>(this, v -> this, supplier);
  }

  @Override
  public ValueStream<T> orElse(T value) {
    return new MapStream.Value<>(this, Function.identity(), () -> value);
  }

  @Override
  public ValueStream<T> conditionOn(ObservableValue<Boolean> condition) {
    return RootValueStream.of(condition).flatMap(c -> c ? this : RootValueStream.empty());  // TODO constant???
  }

  // Experimental
  @Override
  public ValueStream<T> transactional() {
    return new TransactionalStream.Value<>(this);
  }
}
