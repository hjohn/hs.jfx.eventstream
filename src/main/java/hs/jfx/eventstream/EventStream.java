package hs.jfx.eventstream;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.binding.Binding;

/**
 * Stream of events (values of type T).
 *
 * @param <T> type of values emitted by this stream
 */
public interface EventStream<T> {

    /**
     * Add an observer to this event stream.
     *
     * @param observer an observer to add to this event stream, cannot be null
     */
    void addObserver(Consumer<? super T> observer);

    /**
     * Removes an observer from this event stream.
     *
     * @param observer an observer to remove from this event stream
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

    /**
     * Returns an event stream that immediately emits its event when subscribing
     * to it. If the stream has no event to emit, it defaults to emitting the
     * default event. Once this stream emits an event, the returned stream will
     * emit this stream's most recent event. Useful when one doesn't know whether
     * an EventStream will emit its event immediately but needs it to emit an
     * event immediately. Such a case can arise as shown in the following example:
     * <pre>
     * {@code
     * EventStream<Boolean> controlPresses = EventStreams
     *     .eventsOf(scene, KeyEvent.KEY_PRESSED)
     *     .filter(key -> key.getCode().is(KeyCode.CONTROL))
     *     .map(key -> key.isControlDown());
     *
     * EventSource<?> other;
     * EventStream<Tuple2<Boolean, ?>> combo = EventStreams.combine(controlPresses, other);
     *
     * // This will not run until user presses the control key at least once.
     * combo.subscribe(tuple2 -> System.out.println("Combo emitted an event."));
     *
     * EventStream<Boolean> controlDown = controlPresses.withDefaultEvent(false);
     * EventStream<Tuple2<Boolean, ?>> betterCombo = EventStreams.combine(controlDown, other);
     * betterCombo.subscribe(tuple2 -> System.out.println("Better Combo emitted an event immediately upon program start."));
     * }
     * </pre>
     *
     * @param defaultEvent the event this event stream will emit when subscribing to this stream and this stream does not have an event
     * @return a new event stream with the given default event
     */
    default EventStream<T> withDefaultEvent(T defaultEvent) {
        return new DefaultEventStream<>(this, defaultEvent);
    }

    /**
     * Returns an event stream that emits the same<sup>(*)</sup> events as this
     * stream, but <em>before</em> emitting each event performs the given side
     * effect. This is useful for debugging. The side effect is not allowed to
     * cause recursive event emission from this stream: if it does,
     * {@linkplain IllegalStateException} will be thrown.
     *
     * <p>(*) The returned stream is lazily bound, so it only emits events and
     * performs side effects when it has at least one subscriber.
     *
     * @param an action to perform on emitted events as they are consumed from the stream, cannot be null
     * @return a new event stream
     */
    default EventStream<T> peek(Consumer<? super T> sideEffect) {
        return new PeekStream<>(this, sideEffect);
    }

    /**
     * Returns a new event stream that emits events emitted from this stream
     * that satisfy the given predicate.
     *
     * @param a predicate to apply to emitted event to determine if it should be included, cannot be null
     * @return a new event stream that emits events emitted from this stream
     * that satisfy the given predicate
     */
    default EventStream<T> filter(Predicate<? super T> predicate) {
        return new FilterStream<>(this, predicate);
    }

    /**
     * Returns a new event stream that applies the given function to every
     * value emitted from this stream and emits the result. For example, given
     * <pre>
     *     {@code
     *     EventStream<Integer> A = ...;
     *     EventStream<Integer> B = A.map(intValue -> intValue * 2);
     *     }
     * </pre>
     * <p>Returns B. When A emits an event, the event is mapped by the function (in this case, it multiples
     * A's emitted value by two) and B emits this mapped event.</p>
     * <pre>
     *        Time ---&gt;
     *        A :-3---1--4--5--2--0---3--7---&gt;
     *        B :-6---2--8--10-4--0---6--14--&gt;
     * </pre>
     *
     * @param a function which maps a value emitted from this stream to a new value, cannot be null
     * @return a new event stream that applies the given function to every
     * value emitted from this stream and emits the result
     */
    default <U> EventStream<U> map(Function<? super T, ? extends U> f) {
        return new MappedStream<>(this, f);
    }

    /**
     * Returns a new event stream that, for each event <i>x</i> emitted from
     * this stream, obtains the event stream <i>f(x)</i> and keeps emitting its
     * events until the next event is emitted from this stream.
     * For example, given
     * <pre>
     *     {@code
     *     EventStream<Integer> A = ...;
     *     EventStream<Integer> B = ...;
     *     EventStream<Integer> C = ...;
     *     EventStream<Integer> D = A.flatMap(intValue -> {
     *         intValue < 4
     *             ? B
     *             : C
     *     })
     *     }
     * </pre>
     * <p>Returns D. When A emits an event that is less than 4, D will emit B's events.
     * Otherwise, D will emit C's events. When A emits a new event, the stream whose events
     * D will emit is re-determined.
     * <pre>
     *        Time ---&gt;
     *        A :-3---1--4--5--2--0---3--5---------&gt;
     *        B :--4-7---8--7----4-----34---5--56--&gt;
     *        C :----6---6----5---8----9---2---5---&gt;
     *   Stream :-BBBBBBBCCCCCCBBBBBBBBBBCCCCCCCCC-&gt;
     *        D :--4-7---6----5--4-----34--2---5---&gt;
     * </pre>
     *
     * @param f a function which maps a value emitted from this stream to a new event stream, cannot be null
     * @return a new event stream that, for each event <i>x</i> emitted from
     * this stream, obtains the event stream <i>f(x)</i> and keeps emitting its
     * events until the next event is emitted from this stream
     */
    default <U> EventStream<U> flatMap(Function<? super T, ? extends EventStream<U>> f) {
        return new FlatMapStream<>(this, f);
    }

    /**
     * Returns a binding that holds the most recent event emitted from this
     * stream. The returned binding stays subscribed to this stream until its
     * {@code dispose()} method is called.
     *
     * @param initialValue used as the returned binding's value until this
     * stream emits the first value
     * @return binding reflecting the most recently emitted value, never null
     */
    default Binding<T> toBinding(T initialValue) {
        return new StreamBinding<>(this, initialValue);
    }
}
