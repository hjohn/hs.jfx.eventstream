package hs.jfx.eventstream;

import hs.jfx.eventstream.impl.DefaultStream;
import hs.jfx.eventstream.impl.FilterStream;
import hs.jfx.eventstream.impl.FlatMapStream;
import hs.jfx.eventstream.impl.MapStream;
import hs.jfx.eventstream.impl.PeekStream;
import hs.jfx.eventstream.impl.TransactionalStream;
import hs.jfx.eventstream.util.StreamUtil;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.value.ObservableValue;

/**
 *
 * <pre>
 * Intermediate       Invalidation Change    Value
 * -----------------------------------------------
 * replace                 X   -->   -         -
 * conditionOn             -         X         X
 * map, flatMap            -         X         X
 * or, orElse, orElseGet   -         X         X
 * peek                    -         X         X
 * filter                  -         X   <--   X
 * flatMapToChange         -         -   <--   X
 * withDefault             X   ---   X   -->   -
 *
 * Terminal           Invalidation Change    Value
 * -----------------------------------------------
 * subscribe               X         X         X
 * toBinding               -         -         X
 *
 * Change never should have nulls(?)... but can have them when converted from Value (using filter)
 *
 * @param <T> the type of values the stream emits
 */
public interface ChangeStream<T> extends Observable<T> {

  default ChangeStream<T> filter(Predicate<T> predicate) {
    return new FilterStream<>(this, predicate);
  }

  // TODO offer toBinding here as well, but with the default?
  // TODO better name?  whenEmpty?  whenLazy?  toValueStream?  eager?
  default ValueStream<T> withDefault(T value) {
    return withDefaultGet(() -> value);
  }

  default ValueStream<T> withDefaultGet(Supplier<T> defaultValueSupplier) {
    return new DefaultStream<>(this, defaultValueSupplier);
  }

  default <U> ChangeStream<U> map(Function<? super T, ? extends U> mapper) {
    return new MapStream.Change<>(this, mapper, StreamUtil.nullSupplier());
  }

  // let docs reflect that a flatmap on a change stream means changing of streams, but not emitting current value of destination stream!
  default <U> ChangeStream<U> flatMap(Function<? super T, ? extends ChangeStream<? extends U>> mapper) {
    return new FlatMapStream.Change<>(this, mapper, Changes::empty);
  }

  default ChangeStream<T> peek(Consumer<? super T> sideEffect) {
    return new PeekStream.Change<>(this, sideEffect);
  }

  // In case the value supplied by this stream is null, feed values from supplied stream
  default ChangeStream<T> or(Supplier<? extends ChangeStream<? extends T>> supplier) {
    Objects.requireNonNull(supplier);

    return new FlatMapStream.Change<>(this, v -> this, supplier);
  }

  default ChangeStream<T> orElse(T value) {
    return orElseGet(() -> value);
  }

  default ChangeStream<T> orElseGet(Supplier<T> valueSupplier) {
    return new MapStream.Change<>(this, Function.identity(), valueSupplier);
  }

  default ChangeStream<T> conditionOn(ObservableValue<Boolean> condition) {
    return Values.of(condition).flatMapToChange(c -> c ? this : Changes.empty());
  }

  default ChangeStream<T> transactional() {
    return new TransactionalStream.Change<>(this);
  }

//  @Override
//  default T getCurrentValue() {
//    return BaseStream.nullEvent();
//  }
}
