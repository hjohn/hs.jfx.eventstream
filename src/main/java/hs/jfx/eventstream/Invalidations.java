package hs.jfx.eventstream;

import hs.jfx.eventstream.impl.BaseInvalidationStream;
import hs.jfx.eventstream.impl.Emitter;
import hs.jfx.eventstream.impl.InvalidationAction;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class Invalidations {

  /**
   * Creates a stream that emits an impulse on every invalidation of the given observables.
   *
   * @param observables zero or more observables which serve as an invalidation source for the new stream
   * @return a stream that emits an impulse for each invalidation of the given {@code Observable}s for every subscriber
   */
  public static InvalidationStream of(Observable... observables) {
    return new BaseInvalidationStream(null, new InvalidationAction() {
      @Override
      public Subscription observeInputs(hs.jfx.eventstream.ObservableStream<Void> source, Emitter<Void> emitter) {
        InvalidationListener listener = obs -> emitter.emit(null);

        for(Observable observable : observables) {
          observable.addListener(listener);
        }

        return () -> {
          for(Observable observable : observables) {
            observable.removeListener(listener);
          }
        };
      }
    });
  }
}
