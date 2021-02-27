package hs.jfx.eventstream.api;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.value.ObservableValue;

/**
 * A sequence of values supporting aggregate operations. The values
 * emitted from this stream are always a direct result from a change
 * occuring in the source of this stream.<p>
 *
 * Subscribers will receive changes only when they occur. If no changes
 * occur it is therefore possible that a subscription will never receive
 * a change.<p>
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
public interface ChangeStream<T> extends ObservableStream<T> {

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
   * Returns a {@link ChangeStream} which emits the same values as this stream and,
   * each time this stream emits a value, calls the given {@code sideEffect}
   * consumer with the value.<p>
   *
   * Note that this function is not null safe and the value supplied can be {@code null}
   * if the stream emits it.
   *
   * @return a {@link ChangeStream} which emits the same values as this stream and calls the given {@code sideEffect}
   *         consumer with each value, never null
   */
  ChangeStream<T> peek(Consumer<? super T> sideEffect);

  /**
   * Returns a {@link ChangeStream}, using this stream as its source,
   * which emits values from this stream but with <code>null</code>s
   * replaced with the given value.
   *
   * @param value a value to emit instead of <code>null</code>, can be <code>null</code>
   * @return a {@link ChangeStream} with <code>null</code>s replaced with the given value, never null
   */
  default ChangeStream<T> orElse(T value) {
    return orElseGet(() -> value);
  }

  /**
   * Returns a {@link ChangeStream}, using this stream as its source,
   * which emits the same values as its source but with <code>null</code>s
   * replaced with the value supplied by the given {@link Supplier}.
   *
   * @param valueSupplier a {@link Supplier} which supplies the value to emit instead of <code>null</code>
   * @return a {@link ChangeStream} with <code>null</code>s replaced with the value supplied by the given {@link Supplier}, never null
   */
  ChangeStream<T> orElseGet(Supplier<? extends T> valueSupplier);

  /**
   * Returns a {@link ChangeStream}, using this stream as its source,
   * which only observes this stream when {@code condition} is {@code true}.
   * If the condition is {@code null} this is considered to be {@code false}.<p>
   *
   * Although similar to {@link #filter(Predicate)}, the condition is not
   * based on the actual values emitted by the source stream, and as such
   * the subscription to the source stream can be temporarily suspended when the
   * condition evaluates to false.<p>
   *
   * @param condition a boolean {@link ObservableValue}, cannot be null
   * @return a {@link ChangeStream} which only observes its source stream when {@code condition} is {@code true}, never null
   */
  ChangeStream<T> conditionOn(ObservableValue<Boolean> condition);

  /**
   * Returns a {@link ChangeStream}, using this stream as its source,
   * which emits values converted by the given mapper function.<p>
   *
   * This function is null safe and the mapper will not be called when the stream
   * emits {@code null}.
   *
   * @param <U> the type of values the new stream emits
   * @param mapper a {@link Function} which converts a source value to a new value to emit, cannot be null
   * @return a {@link ChangeStream} which emits values converted by the given mapper function, never null
   */
  <U> ChangeStream<U> map(Function<? super T, ? extends U> mapper);

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
   *       Source :--A---A--B--B--A--A--BA--B---------&gt;
   * Changes of A :-3-4-7---8--7----4-----34---5--56--&gt;
   * Changes of B :-1---6---6----5---8----9---2---5---&gt;
   *     Tracking :--AAAAAAABBBBBBAAAAAABAAABBBBBBBBB-&gt;
   *        Emits :---4-7---6----5--4-----34--2---5---&gt;
   * </pre>
   *
   * This function is null safe and the mapper will not be called when the stream
   * emits {@code null}.
   *
   * @param mapper a {@link Function} which returns an alternative stream for each value this stream emits, cannot be null
   * @return a {@link ChangeStream} which obtains a new stream supplied by mapper and emits its values instead, never null
   */
  <U> ChangeStream<U> flatMap(Function<? super T, ? extends ChangeStream<? extends U>> mapper);

  /**
   * Returns a {@link ValueStream}, using this stream as its source,
   * which emits the given value as its default value for new subscribers.
   *
   * @param value a value to emit as default value for new subscribers
   * @return a {@link ValueStream} which emits the given value as its default value, never null
   */
  default ValueStream<T> withDefault(T value) {
    return withDefaultGet(() -> value);
  }

  /**
   * Returns a {@link ValueStream}, using this stream as its source,
   * which emits the value generated by the given defaultValueSupplier
   * as its default value for new subscribers.
   *
   * @param defaultValueSupplier a {@link Supplier} which supplies the value to emit as default value for new subscribers
   * @return a {@link ValueStream} which uses the given defaultValueSupplier to provide its default value, never null
   */
  ValueStream<T> withDefaultGet(Supplier<? extends T> defaultValueSupplier);

  /**
   * Returns an {@link EventStream}, using this stream as its source,
   * which emits the same values as this stream but skips {@code null}s.<p>
   *
   * @return an {@link EventStream}  which emits the same values as this stream but skips {@code null}s, never null
   */
  EventStream<T> filterNull();
}
