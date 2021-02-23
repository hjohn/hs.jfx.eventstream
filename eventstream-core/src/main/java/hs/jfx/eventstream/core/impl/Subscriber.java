package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.OptionalValue;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.api.ValueStream;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Internal helper class for creating the {@link Subscription} and providing the
 * initial value for new subscriptions.
 *
 * @param <S> type of values emitted by the source stream
 * @param <T> type of values emitted by this stream
 */
public abstract class Subscriber<S, T> {
  private final ObservableStream<S> source;
  private final Supplier<T> defaultValueSupplier;

  /**
   * Creates an instance suitable for streams which may have to supply
   * initial values to new subscriptions. The defaultValueSupplier is
   * used to supply the values.
   *
   * @param defaultValueSupplier a {@link Supplier} which can supply initial values for new subscriptions
   */
  protected Subscriber(Supplier<T> defaultValueSupplier) {
    this.source = null;
    this.defaultValueSupplier = defaultValueSupplier;
  }

  /**
   * Creates an instance suitable for streams which may have to supply
   * initial values to new subscriptions. The given source is
   * used to supply the values.
   *
   * @param source a source which can supply initial values for new subscriptions
   */
  protected Subscriber(ObservableStream<S> source) {
    this.source = source;
    this.defaultValueSupplier = null;
  }

  /**
   * Creates an instance suitable for streams which do not have to supply
   * initial values for new subscriptions.
   */
  protected Subscriber() {
    this.source = null;
    this.defaultValueSupplier = null;
  }

  protected ObservableStream<S> getSource() {
    return source;
  }

  /**
   * Start observing the source(s). This method is called when the number of observers
   * goes from 0 to 1. The returned subscription is cancelled when the number of
   * observers goes down to 0.
   *
   * @return subscription used to stop observing source(s)
   */
  protected abstract Subscription observeInputs(Emitter<T> emitter);

  /**
   * Used by {@link #getCurrentValue()} to determine the value to provide upon
   * subscription. Implement this for instances which have a {@code source} and
   * will be part of a {@link ValueStream#}.<p>
   *
   * The value should be derived in the exact same way as the stream would if
   * a value is emitted through it.
   *
   * @param value a source value
   * @return an optional resulting value
   */
  protected OptionalValue<T> operate(S value) {
    throw new UnsupportedOperationException("Must be implemented when source is a ValueStream");
  }

  protected final OptionalValue<T> getCurrentValue() {
    if(defaultValueSupplier == null) {
      return source == null ? OptionalValue.empty() : ((ValueStream<S>)source).getCurrentValue().flatMap(this::operate);
    }

    return OptionalValue.of(defaultValueSupplier.get());
  }

  protected final void sendInitialEvent(Consumer<? super T> observer) {
    getCurrentValue().ifPresent(observer::accept);
  }
}
