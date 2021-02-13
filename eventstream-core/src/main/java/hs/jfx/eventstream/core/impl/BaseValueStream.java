package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscription;
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

  /**
   * Returns the value this stream supplies to new subscribers. This method
   * must be overriden if the source is NOT an implementation of {@link BaseValueStream}.<p>
   *
   * Note that this method can return a special value {@link #NULL_EVENT} when
   * this stream is not currently attached to its source.
   *
   * @return the value this stream supplies to new subscribers or the special {@link #NULL_EVENT}
   */
  protected T getCurrentValue() {

    /*
     * The source is guaranteed to be a non-null ValueStream because streams
     * allowing a ChangeStream source for a ValueStream must override this method.
     */

    @SuppressWarnings("unchecked")
    S currentValue = ((BaseValueStream<?, S>)source).getCurrentValue();

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
}
