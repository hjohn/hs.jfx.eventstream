package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.core.impl.BaseEventStream;

import java.util.Arrays;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 * Constructs {@link EventStream}s for invalidations.
 */
public interface Invalidations {

  /**
   * Constructs an {@link EventStream} from the given {@link Observable}s which emits an event
   * with the {@link Observable} which was invalidated as value.
   *
   * @param observables zero or more observables which serve as an invalidation source for the new stream, cannot be null
   * @return an {@link EventStream} which emits an event when one of the given observables is invalidated, never null
   */
  static EventStream<Observable> of(Observable... observables) {
    return new BaseEventStream<>(null, emitter -> {
      InvalidationListener listener = obs -> emitter.emit(obs);
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
