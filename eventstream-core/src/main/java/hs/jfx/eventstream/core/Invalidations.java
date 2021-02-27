package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.InvalidationStream;
import hs.jfx.eventstream.core.impl.BaseInvalidationStream;

import java.util.Arrays;
import java.util.List;

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
      List<Observable> copy = List.copyOf(Arrays.asList(observables));

      for(Observable observable : copy) {
        observable.addListener(listener);
      }

      return () -> {
        for(Observable observable : copy) {
          observable.removeListener(listener);
        }
      };
    });
  }
}
