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

import javafx.beans.value.ObservableValue;

/**
 * Base class for event streams.
 *
 * @param <T> type of events emitted by this event stream
 */
public class BaseChangeStream<S, T> extends BaseObservableStream<T> implements ChangeStream<T> {
  private final ObservableStream<S> source;
  private final Action<S, T> action;

  public BaseChangeStream(ObservableStream<S> source, Action<S, T> action) {
    this.source = source;
    this.action = action;
  }

  @Override
  protected final Subscription observeInputs() {
    return action.observeInputs(source, this::emit);
  }

  @Override
  protected final void sendInitialEvent(Consumer<? super T> observer) {
    // Change Streams donot send an initial event
  }

  @Override
  public ChangeStream<T> filter(Predicate<T> predicate) {
    return new FilterStream<>(this, predicate);
  }

  @Override
  public ValueStream<T> withDefaultGet(Supplier<T> defaultValueSupplier) {
    return new DefaultStream<>(this, defaultValueSupplier);
  }

  @Override
  public <U> ChangeStream<U> map(Function<? super T, ? extends U> mapper) {
    return new MapStream.Change<>(this, mapper, StreamUtil.nullSupplier());
  }

  @Override
  public <U> ChangeStream<U> flatMap(Function<? super T, ? extends ChangeStream<? extends U>> mapper) {
    return new FlatMapStream.Change<>(this, mapper, RootChangeStream::empty);
  }

  @Override
  public ChangeStream<T> peek(Consumer<? super T> sideEffect) {
    return new PeekStream.Change<>(this, sideEffect);
  }

  @Override
  public ChangeStream<T> or(Supplier<? extends ChangeStream<? extends T>> supplier) {
    Objects.requireNonNull(supplier);

    return new FlatMapStream.Change<>(this, v -> this, supplier);
  }

  @Override
  public ChangeStream<T> orElseGet(Supplier<T> valueSupplier) {
    return new MapStream.Change<>(this, Function.identity(), valueSupplier);
  }

  @Override
  public ChangeStream<T> conditionOn(ObservableValue<Boolean> condition) {
    return RootValueStream.of(condition)
      .flatMapToChange(c -> c ? this : RootChangeStream.empty());  // no need to deal with c being null
  }
}
