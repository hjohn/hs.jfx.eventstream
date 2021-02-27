package hs.jfx.eventstream.api;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;

/**
 * A sequence of values with a notion of a current value supporting
 * aggregate operations. Value streams always immediately emit their
 * current value upon subscribing if attached to a source (see
 * {@link #conditionOn(ObservableValue)}).<p>
 *
 * Subscribers will receive the current value upon subscription immediately
 * (if attached to a source) and then any new values when they occur.<p>
 *
 * Operations offered by this stream which accept a {@link Function}
 * or {@link Predicate} are all null safe and will not be called when the
 * stream emits {@code null}, unless otherwise specified.<p>
 *
 * This is a lazy stream, which means that it only observes its source
 * when it has observers of its own. When there are no subscribers,
 * this stream stop observing its source immediately.
 *
 * @param <T> the type of values the stream emits
 */
public interface ValueStream<T> extends ObservableStream<T> {

  /**
   * Returns a {@link ChangeStream}, using this stream as its source,
   * which only emits values matching the given predicate.<p>
   *
   * This function is null safe and the predicate will not be called when the stream
   * emits {@code null}.
   *
   * @param predicate a {@link Predicate} which values must match to be emitted, cannot be null
   * @return a {@link ChangeStream} which only emits values matching the given predicate, never null
   */
  ChangeStream<T> filter(Predicate<? super T> predicate);

  /**
   * Returns a {@link ValueStream} which emits the same values as this stream and,
   * each time this stream emits a value, calls the given {@code sideEffect}
   * consumer with the value.<p>
   *
   * Note that this function is not null safe and the value supplied can be {@code null}
   * if the stream emits it.
   *
   * @return a {@link ValueStream} which emits the same values as this stream and calls the given {@code sideEffect}
   *         consumer with each value, never null
   */
  ValueStream<T> peek(Consumer<? super T> sideEffect);

  /**
   * Returns a {@link ValueStream}, using this stream as its source,
   * which only observes this stream when {@code condition} is {@code true}.
   * If the condition is {@code null} this is considered to be {@code false}.<p>
   *
   * Although similar to {@link #filter(Predicate)}, the condition is not
   * based on the actual values emitted by the source stream, and as such
   * the subscription to the source stream can be temporarily suspended when the
   * condition evaluates to false.<p>
   *
   * Note that it is intended behavior that this stream does not supply anything
   * to new subscribers when the condition is currently {@code false}.
   *
   * @param condition a boolean {@link ObservableValue}, cannot be null
   * @return a {@link ValueStream} which only observes its source stream when {@code condition} is {@code true}, never null
   */
  ValueStream<T> conditionOn(ObservableValue<Boolean> condition);

  /**
   * Returns a {@link ValueStream}, using this stream as its source,
   * which emits values converted by the given mapper function.<p>
   *
   * This function is null safe and the mapper will not be called when the stream
   * emits {@code null}.
   *
   * @param <U> the type of values the new stream emits
   * @param mapper a {@link Function} which converts a source value to a new value to emit, cannot be null
   * @return a {@link ValueStream} which emits values converted by the given mapper function, never null
   */
  <U> ValueStream<U> map(Function<? super T, ? extends U> mapper);

  /**
   * Returns a {@link ValueStream} which, each time this stream emits a value,
   * obtains a new stream supplied by mapper and emits its values instead. If
   * the mapper function returns {@code null} an empty stream will be used
   * instead -- this can be unexpected for a {@link ValueStream} which normally
   * supplies new subscribers with its current value but won't in this case,
   * it is therefore recommended to avoid flat mapping to {@code null}.<p>
   *
   * Note: ValueStreams supply their current value to new subscribers immediately,
   * so when this stream emits a value, the stream being tracked changes and its
   * current value will immediately be emitted. See also {@link ChangeStream#flatMap(Function)}.<p>
   *
   * An example where the source stream flat maps to value stream A or B,
   * note especially that a change of stream results in an immediate emission of the new
   * stream's current value:
   * <pre>
   *            Time ---&gt;
   *       Source :--A---A--B--B--A--A---A--B---------&gt;
   *  Values of A :-3-4-7---8--7----4-----34---5--56--&gt;
   *  Values of B :-1---6---6----5---8----9---2---5---&gt;
   *     Tracking :--AAAAAAABBBBBBAAAAAAAAAABBBBBBBBB-&gt;
   *        Emits :--34-7---6----57-4-----349-2---5---&gt;
   * </pre>
   *
   * This function is null safe and will not be called when the stream
   * emits {@code null}.
   *
   * @param mapper a {@link Function} which returns an alternative stream for each value this stream emits, cannot be null
   * @return a {@link ValueStream} which obtains a new stream supplied by mapper and emits its values instead, never null
   */
  <U> ValueStream<U> flatMap(Function<? super T, ? extends ValueStream<? extends U>> mapper);

  /**
   * Returns a {@link ChangeStream} which, each time this stream emits a value,
   * obtains a new stream supplied by mapper and emits its values instead. If
   * the mapper function returns {@code null} an empty stream will be used
   * instead.<p>
   *
   * Note: ChangeStreams donot emit values eagerly, so when this stream
   * emits a value, the stream being tracked changes but a value will
   * only be emitted when that stream emits a value after tracking started.
   * See also {@link ValueStream#flatMap(Function)}.<p>
   *
   * An example where the source stream flat maps to change stream A or B,
   * note especially that a change of stream does not necessarily result in
   * an emission:
   * <pre>
   *            Time ---&gt;
   *       Source :--A---A--B--B--A--A---A--B---------&gt;
   * Changes of A :-3-4-7---8--7----4-----34---5--56--&gt;
   * Changes of B :-1---6---6----5---8----9---2---5---&gt;
   *     Tracking :--AAAAAAABBBBBBAAAAAAAAAABBBBBBBBB-&gt;
   *        Emits :---4-7---6----5--4-----34--2---5---&gt;
   * </pre>
   *
   * This function is null safe and the mapper will not be called when the stream
   * emits {@code null}.
   *
   * @param mapper a {@link Function} which returns an alternative stream for each value this stream emits, cannot be null
   * @return a {@link ChangeStream} which obtains a new stream supplied by mapper and emits its values instead, never null
   */
  <U> ChangeStream<U> flatMapToChange(Function<? super T, ? extends ChangeStream<? extends U>> mapper);

  /**
   * Returns a {@link ValueStream}, using this stream as its source,
   * which emits values from this stream but with <code>null</code>s
   * replaced with the given value.
   *
   * @param value a value to emit instead of <code>null</code>, can be <code>null</code>
   * @return a {@link ValueStream} with <code>null</code>s replaced with the given value, never null
   */
  default ValueStream<T> orElse(T value) {
    return orElseGet(() -> value);
  }

  /**
   * Returns a {@link ValueStream}, using this stream as its source,
   * which emits the same values as its source but with <code>null</code>s
   * replaced with the value supplied by the given {@link Supplier}.
   *
   * @param valueSupplier a {@link Supplier} which supplies the value to emit instead of <code>null</code>
   * @return a {@link ValueStream} with <code>null</code>s replaced with the value supplied by the given {@link Supplier}, never null
   */
  ValueStream<T> orElseGet(Supplier<? extends T> valueSupplier);

  /**
   * Returns a {@link ValueStream}, using this stream as its source,
   * which emits values from this stream unless the value was
   * <code>null</code> in which case it emits values from the supplied stream
   * until a new value is emitted from this stream.
   *
   * @param supplier a {@link Supplier} supplying an alternative stream when this stream emitted <code>null</code>, cannot be null
   * @return a {@link ValueStream} which emits values from this stream unless the value was <code>null</code>
   *         in which case it emits values from the supplied stream, never null
   */
  ValueStream<T> or(Supplier<? extends ValueStream<? extends T>> supplier);

  /**
   * Returns an {@link EventStream}, using this stream as its source,
   * which emits the same values as this stream but skips {@code null}s.<p>
   *
   * @return an {@link EventStream}  which emits the same values as this stream but skips {@code null}s, never null
   */
  EventStream<T> filterNull();

  /**
   * Returns the values of this stream as a {@link Binding}.
   *
   * @return the values of this stream as a {@link Binding}, never null
   */
  Binding<T> toBinding();

  /**
   * Returns an {@link OptionalValue} which contained value this stream will supply
   * to new subscribers. If the {@link OptionalValue} is empty, no value will be
   * supplied to new subscribers.<p>
   *
   * Depending on how the stream is constructed the contained value could be
   * considered the "current" value of the stream (if based on a property). It
   * specifically is not the <b>last</b> value emitted by this stream; if not all
   * ancestors are {@link ValueStream}s then the calculation of the value will
   * start at the last ancestor in the ancestor chain which is still a
   * {@link ValueStream}.<p>
   *
   * Example 1:
   * <pre>Values.of(property).getCurrentValue();</pre>
   * Returns the value of {@code property} directly.<p>
   *
   * Example 2:
   * <pre>
   * Values.of(property)
   *     .filter(f)         // returns a ChangeStream
   *     .withDefault("X")  // returns a ValueStream
   *     .map(v -> v + "Y")
   *     .getCurrentValue();
   * </pre>
   * Returns "XY" because {@code filter} does not result in a {@code ValueStream}. The
   * last ancestor which is a {@code ValueStream} supplies "X", and after mapping this becomes "XY".<p>
   *
   * @return an {@link OptionalValue} which contained value this stream will supply
   *     to new subscribers, never null
   */
  OptionalValue<T> getCurrentValue();
}
