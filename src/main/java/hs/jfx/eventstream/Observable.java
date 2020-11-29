package hs.jfx.eventstream;

import java.util.function.Consumer;

public interface Observable<T> {

  /**
   * Add an observer to this event stream.
   *
   * @param observer an observer to add to this event stream, cannot be null
   */
  void addObserver(Consumer<? super T> observer);

  /**
   * Removes an observer from this event stream.
   *
   * @param observer an observer to remove from this event stream, cannot be null
   */
  void removeObserver(Consumer<? super T> observer);

  /**
   * Start observing this event stream and returns a {@link Subscription} which
   * can be used to stop observing the stream.
   *
   * @param subscriber a consumer to add to this event stream, cannot be null
   * @return a {@link Subscription} that can be used to stop observing this event stream
   */
  default Subscription subscribe(Consumer<? super T> subscriber) {
      addObserver(subscriber);

      return () -> removeObserver(subscriber);
  }
}
