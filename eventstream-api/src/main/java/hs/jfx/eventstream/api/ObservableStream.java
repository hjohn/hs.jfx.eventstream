package hs.jfx.eventstream.api;

import java.util.function.Consumer;

/**
 * A sequence of values which be oserved or subscribed to. The values
 * emitted from this stream are always a direct result from a change
 * occuring in the source of this stream.<p>
 *
 * Observable streams are lazy, which means they only observe their
 * source when it has observers of its own. When there are none,
 * this stream stops observing its source immediately.
 *
 * @param <T> the type of values the stream emits
 */
public interface ObservableStream<T> {

  /**
   * Add an observer to this stream. It is allowed to add the same observer
   * twice.<p>
   *
   * The observer is called for every value which is emitted by the stream,
   * unless the value is filtered, regardless of the previous value emitted.
   *
   * @param observer an observer to add to this stream, cannot be null
   */
  void addObserver(Consumer<? super T> observer);

  /**
   * Removes an observer from this stream. If the observer was not registered
   * with this stream this call does nothing. If the same observer was registered
   * twice, two calls will be needed to remove both.
   *
   * @param observer an observer to remove from this stream, cannot be null
   */
  void removeObserver(Consumer<? super T> observer);

  /**
   * Start observing this stream and returns a {@link Subscription} which
   * can be used to stop observing the stream. It is allowed to add the same
   * subscriber twice.<p>
   *
   * The subscriber is called for every value which is emitted by the stream,
   * unless the value is filtered, regardless of the previous value emitted.
   *
   * @param subscriber a consumer to add to this stream, cannot be null
   * @return a {@link Subscription} that can be used to stop observing this stream, never null
   */
  default Subscription subscribe(Consumer<? super T> subscriber) {
      addObserver(subscriber);

      return () -> removeObserver(subscriber);
  }
}
