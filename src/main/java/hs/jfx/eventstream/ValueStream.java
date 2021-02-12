package hs.jfx.eventstream;

import hs.jfx.eventstream.impl.FilterStream;
import hs.jfx.eventstream.impl.FlatMapStream;
import hs.jfx.eventstream.impl.MapStream;
import hs.jfx.eventstream.impl.PeekStream;
import hs.jfx.eventstream.impl.TransactionalStream;
import hs.jfx.eventstream.impl.ValueStreamBinding;
import hs.jfx.eventstream.util.StreamUtil;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;

/**
 * Streams which always immediately emit their current or last known value upon
 * subscribing.<p>
 *
 * Operations like filter and conditionOn will result in streams which cannot always
 * emit a value and thus these functions return a different type of stream.
 *
 * @param <T> the type of values the stream emits
 */

// Should be described as emitting the most recent value immediately when condition
// allows or on subscription, so it is still possible to get nothing on initial subscription
public interface ValueStream<T> extends ObservableStream<T> {

  default ChangeStream<T> filter(Predicate<? super T> filter) {
    return new FilterStream<>(this, filter);
  }

  default <U> ValueStream<U> map(Function<? super T, ? extends U> mapper) {
    return new MapStream.Value<>(this, mapper, StreamUtil.nullSupplier());
  }

  default Binding<T> toBinding() {
    return new ValueStreamBinding<>(this);
  }

  // When you have a ValueStream, lets say it contains 1 or 2 or 3.
  // And you flatmap this to one of 3 event streams: clicks, double clicks, triple clicks
  // Does this still have a default value on subscribe?
  // No.
  // So question is: should you be allowed to flatmap to a ChangeStream?
  // Can, but it will become a ChangeStream (similar to filter)
  // Method erasure will be same though, so would need 2 names or have a method that converts to ChangeStream (withoutDefault?)
  default <U> ValueStream<U> flatMap(Function<? super T, ? extends ValueStream<? extends U>> mapper) {
    return new FlatMapStream.Value<>(this, mapper, () -> Values.constant((U)null));
  }

  // Convienence function...
  default <U> ValueStream<U> bind(Function<? super T, ObservableValue<? extends U>> mapper) {
    Objects.requireNonNull(mapper);

    return new FlatMapStream.Value<>(this, v -> Values.of(mapper.apply(v)), () -> Values.constant((U)null));
  }

  default <U> ChangeStream<U> flatMapToChange(Function<? super T, ? extends ChangeStream<? extends U>> mapper) {
    return new FlatMapStream.Change<>(this, mapper, Changes::empty);
  }

  default ValueStream<T> peek(Consumer<? super T> sideEffect) {
    return new PeekStream.Value<>(this, sideEffect);
  }

  default ValueStream<T> or(Supplier<? extends ValueStream<? extends T>> supplier) {
    return new FlatMapStream.Value<>(this, v -> this, supplier);
  }

  default ValueStream<T> orElse(T value) {
    return new MapStream.Value<>(this, Function.identity(), () -> value);
  }

  /**
   * Returns a new {@linkplain EventStream} that only observes this
   * {@linkplain EventStream} when {@code condition} is {@code true}.
   * More precisely, the returned {@linkplain EventStream} observes
   * {@code condition} whenever it itself has at least one subscriber and
   * observes {@code this} {@linkplain EventStream} whenever it itself has
   * at least one subscriber <em>and</em> the value of {@code condition} is
   * {@code true}. When {@code condition} is {@code true}, the returned
   * {@linkplain EventStream} emits the same events as this
   * {@linkplain EventStream}. When {@code condition} is {@code false}, the
   * returned {@linkplain EventStream} emits no events.
   * TODO update docs
   * @param condition a condition, cannot be null
   * @return a new event stream which only observes this stream when {@code condition} is {@code true}
   */
  default ValueStream<T> conditionOn(ObservableValue<Boolean> condition) {
    return Values.of(condition).flatMap(c -> c ? this : Values.empty());  // TODO constant???
  }

  default ValueStream<T> transactional() {
    return new TransactionalStream.Value<>(this);
  }

  /**
   * Returns the value a new subscriber will receive immediately upon subscribing to
   * this stream, also known as the current value of the stream.
   *
   * @return a value
   */
  T getCurrentValue();
}
