package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.InvalidationStream;
import hs.jfx.eventstream.core.impl.BaseInvalidationStream;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 * Constructs {@link InvalidationStream}s.
 */
public interface Invalidations {

  /**
   * Creates a stream that emits an impulse on every invalidation of the given observables.
   *
   * @param observables zero or more observables which serve as an invalidation source for the new stream
   * @return a stream that emits an impulse for each invalidation of the given {@code Observable}s for every subscriber, never null
   */
  static InvalidationStream of(Observable... observables) {
    return new BaseInvalidationStream(null, emitter -> {
      InvalidationListener listener = obs -> emitter.emit(null);

      for(Observable observable : observables) {
        observable.addListener(listener);
      }

      return () -> {
        for(Observable observable : observables) {
          observable.removeListener(listener);
        }
      };
    });
  }
}
