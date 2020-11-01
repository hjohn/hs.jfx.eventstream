package hs.jfx.eventstream;

import java.util.Objects;
import java.util.function.Function;

class FlatMapStream<T, U> extends EventStreamBase<U> {
    private final EventStream<T> source;
    private final Function<? super T, ? extends EventStream<U>> mapper;

    private Subscription mappedSubscription = Subscription.EMPTY;

    public FlatMapStream(EventStream<T> src, Function<? super T, ? extends EventStream<U>> f) {
        this.source = Objects.requireNonNull(src);
        this.mapper = Objects.requireNonNull(f);
    }

    @Override
    protected Subscription observeInputs() {
        Subscription s = source.subscribe(t -> {
            mappedSubscription.unsubscribe();
            mappedSubscription = mapper.apply(t).subscribe(this::emit);
        });

        return () -> {
            s.unsubscribe();
            mappedSubscription.unsubscribe();
            mappedSubscription = Subscription.EMPTY;
        };
    }
}