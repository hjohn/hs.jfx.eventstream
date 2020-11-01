package hs.jfx.eventstream;

import java.util.Objects;
import java.util.function.Function;

class MappedStream<T, U> extends EventStreamBase<U> {
    private final EventStream<T> input;
    private final Function<? super T, ? extends U> f;

    public MappedStream(EventStream<T> input, Function<? super T, ? extends U> f) {
        this.input = Objects.requireNonNull(input);
        this.f = Objects.requireNonNull(f);
    }

    @Override
    protected Subscription observeInputs() {
        return input.subscribe(value -> {
            emit(f.apply(value));
        });
    }
}