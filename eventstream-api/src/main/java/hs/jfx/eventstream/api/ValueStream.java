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
 * The intermediate operations offered by this stream are null safe.
 * Intermediate operations will not be called when the stream emits
 * <code>null</code> unless otherwise specified or specifically intended
 * to handle <code>null</code>s.<p>
 *
 * This is a lazy stream, which means that it only observes its source
 * when it has observers of its own.  When there are no subscribers,
 * this stream stop observing its source immediately.
 *
 * @param <T> the type of values the stream emits
 */
public interface ValueStream<T> extends ObservableStream<T> {

  ChangeStream<T> filter(Predicate<? super T> filter);

  <U> ValueStream<U> map(Function<? super T, ? extends U> mapper);

  Binding<T> toBinding();

  /**
   * Returns a {@link ValueStream} which, each time this stream emits a value,
   * obtains a new stream supplied by mapper and emits its values instead.<p>
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
   * @param mapper a {@link Function} which returns an alternative stream for each value this stream emits, cannot be null
   * @return a {@link ValueStream} which obtains a new stream supplied by mapper and emits its values instead, never null
   */
  <U> ValueStream<U> flatMap(Function<? super T, ? extends ValueStream<? extends U>> mapper);

  /**
   * Returns a {@link ChangeStream} which, each time this stream emits a value,
   * obtains a new stream supplied by mapper and emits its values instead.<p>
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
   * @param mapper a {@link Function} which returns an alternative stream for each value this stream emits, cannot be null
   * @return a {@link ChangeStream} which obtains a new stream supplied by mapper and emits its values instead, never null
   */
  <U> ChangeStream<U> flatMapToChange(Function<? super T, ? extends ChangeStream<? extends U>> mapper);

  ValueStream<T> peek(Consumer<? super T> sideEffect);

  ValueStream<T> or(Supplier<? extends ValueStream<? extends T>> supplier);

  /**
   * Returns a {@link ValueStream}, using this stream as its source,
   * which emits values from this stream but with <code>null</code>s
   * replaced with the given value.
   *
   * @param value a value to emit instead of <code>null</code>, can be <code>null</code>
   * @return a {@link ValueStream} with <code>null</code>s replaced with the given value, never null
   */
  ValueStream<T> orElse(T value);

  /**
   * Returns a {@link ValueStream}, using this stream as its source,
   * which only observes this stream when {@code condition} is {@code true}.<p>
   *
   * Although similar to {@link #filter(Predicate)}, the condition is not
   * based on the actual values emitted by the source stream, and as such
   * the subscription to the source stream can be temporarily suspended when the
   * condition evaluates to false.<p>
   *
   * @param condition a boolean {@link ObservableValue}, cannot be null
   * @return a {@link ValueStream} which only observes its source stream when {@code condition} is {@code true}, never null
   */
  ValueStream<T> conditionOn(ObservableValue<Boolean> condition);

  // Convienence function...
  <U> ValueStream<U> bind(Function<? super T, ObservableValue<? extends U>> mapper);
}
