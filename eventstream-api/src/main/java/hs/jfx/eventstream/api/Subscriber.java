package hs.jfx.eventstream.api;

/**
 * A subscriber for creating {@link Subscription}s for streams which
 * emit values of type T.
 *
 * @param <T> the type of value
 */
public interface Subscriber<T> {

  /**
   * Subscribes to a source using the given {@link Emitter} to
   * emit values.
   *
   * @param emitter an {@link Emitter} used to emit values, cannot be null
   * @return a {@link Subscription} with which the subscription can be cancelled, never null
   */
  Subscription subscribe(Emitter<T> emitter);
}
