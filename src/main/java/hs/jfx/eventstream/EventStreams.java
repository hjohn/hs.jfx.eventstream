package hs.jfx.eventstream;

import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class EventStreams {

    /**
     * Creates an event stream that emits the value of the given
     * {@code ObservableValue} immediately for every subscriber and then on
     * every change.
     *
     * @param <T> type of values emitted by the new stream
     * @param an observable which serves as a value source for the new event stream
     * @return an event stream that emits the value of the given
     * {@code ObservableValue} immediately for every subscriber and then on
     * every change
     */
    public static <T> EventStream<T> valuesOf(ObservableValue<T> observable) {
        return new EventStreamBase<>() {
            @Override
            protected Subscription observeInputs() {
                ChangeListener<T> listener = (obs, old, val) -> emit(val);

                observable.addListener(listener);

                return () -> observable.removeListener(listener);
            }

            @Override
            protected void newObserver(Consumer<? super T> subscriber) {
                subscriber.accept(observable.getValue());
            }
        };
    }
}
