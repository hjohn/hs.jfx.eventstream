package hs.jfx.eventstream;

import hs.jfx.eventstream.util.ListHelper;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base class for event streams.
 *
 * @param <T> type of events emitted by this event stream
 */
abstract class EventStreamBase<T> implements EventStream<T> {
    private ListHelper<Consumer<? super T>> observers = null;
    private Subscription inputSubscription = null;

    /**
     * Starts observing this observable's input(s), if any.
     * This method is called when the number of observers goes from 0 to 1.
     * This method is called <em>before</em> {@link #newObserver(Object)}
     * is called for the first observer.
     *
     * @return subscription used to stop observing inputs. The subscription
     * is unsubscribed (i.e. input observation stops) when the number of
     * observers goes down to 0.
     */
    protected abstract Subscription observeInputs();

    /**
     * Emits the given value as an event for this event stream.
     *
     * @param value a value to emit
     */
    protected void emit(T value) {
        Iterator<Consumer<? super T>> iterator = ListHelper.iterator(observers);

        while(iterator.hasNext()) {
          Consumer<? super T> observer = iterator.next();

          observer.accept(value);
        }
    }

    /**
     * Returns the current number of observers of this stream.
     *
     * @return the current number of observers of this stream, never negative
     */
    protected final int getObserverCount() {
        return ListHelper.size(observers);
    }

    /**
     * Called for each new observer.
     * Overriding this method is a convenient way for subclasses
     * to handle this event, for example to publish some initial events.
     *
     * <p>This method is called <em>after</em> the
     * {@link #observeInputs()} method.</p>
     *
     * @param observer the new observer, never null
     */
    protected void newObserver(Consumer<? super T> observer) {
        // default implementation is empty
    }

    @Override
    public final Subscription subscribe(Consumer<? super T> subscriber) {
        return EventStream.super.subscribe(subscriber);
    }

    @Override
    public final void addObserver(Consumer<? super T> observer) {
        observers = ListHelper.add(observers, Objects.requireNonNull(observer));

        if(ListHelper.size(observers) == 1) {
            inputSubscription = observeInputs();
        }

        newObserver(observer);
    }

    @Override
    public final void removeObserver(Consumer<? super T> observer) {
        observers = ListHelper.remove(observers, observer);

        if(ListHelper.isEmpty(observers) && inputSubscription != null) {
            inputSubscription.unsubscribe();
            inputSubscription = null;
        }
    }
}
