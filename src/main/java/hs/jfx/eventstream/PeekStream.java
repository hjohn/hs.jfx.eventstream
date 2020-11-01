package hs.jfx.eventstream;
import java.util.Objects;
import java.util.function.Consumer;

class PeekStream<T> extends EventStreamBase<T> {
    private final EventStream<T> source;
    private final Consumer<? super T> sideEffect;
    private boolean sideEffectInProgress = false;

    public PeekStream(EventStream<T> source, Consumer<? super T> sideEffect) {
        this.source = Objects.requireNonNull(source);
        this.sideEffect = Objects.requireNonNull(sideEffect);
    }

    @Override
    protected Subscription observeInputs() {
        return source.subscribe(t -> {
            if(sideEffectInProgress) {
                throw new IllegalStateException("Side effect is not allowed to cause recursive event emission");
            }

            sideEffectInProgress = true;
            try {
                sideEffect.accept(t);
            }
            finally {
                sideEffectInProgress = false;
            }

            emit(t);
        });
    }
}