package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.ObservableStream;
import hs.jfx.eventstream.api.Subscription;
import hs.jfx.eventstream.api.ValueStream;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class FlatMapStream {

  public static class Change<T, U> extends BaseChangeStream<T, U> {
    public Change(ObservableStream<T> source, Function<? super T, ? extends ChangeStream<? extends U>> mapper, Supplier<? extends ChangeStream<? extends U>> nullReplacement) {
      super(source, new FlatMapAction<>(mapper, nullReplacement));
    }
  }

  public static class Value<T, U> extends BaseValueStream<T, U> {
    public Value(ObservableStream<T> source, Function<? super T, ? extends ValueStream<? extends U>> mapper, Supplier<? extends ValueStream<? extends U>> nullReplacement) {
      super(source, new FlatMapAction<>(mapper, nullReplacement));
    }
  }

  public static class FlatMapAction<T, U> implements Action<T, U> {
    private final Function<? super T, ? extends ObservableStream<? extends U>> mapper;
    private final Supplier<? extends ObservableStream<? extends U>> nullReplacement;

    private Subscription mappedSubscription = Subscription.EMPTY;

    public FlatMapAction(Function<? super T, ? extends ObservableStream<? extends U>> mapper, Supplier<? extends ObservableStream<? extends U>> nullReplacement) {
      this.mapper = Objects.requireNonNull(mapper);
      this.nullReplacement = Objects.requireNonNull(nullReplacement);
    }

    @Override
    public Subscription observeInputs(ObservableStream<T> source, Emitter<U> emitter) {
      Subscription s = source.subscribe(t -> {
        ObservableStream<? extends U> newStream = map(t);

        /*
         * When the flatmapping results in null, an empty stream is tracked (or rather
         * no subscription is made at all). This means effectively that the resulting
         * stream will emit nothing until the source triggers a flatmapping to a
         * different stream.
         *
         * For ValueStreams this can be a bit unexpected, as no value will be emitted.
         * However, the alternative (throwing an exception) does not work well because
         * JavaFX fireValueChangeEvent code will necessarily catch and log this as
         * there is no way to properly let this bubble up to where the stream was
         * created.
         */

        mappedSubscription.unsubscribe();
        mappedSubscription = newStream == null ? Subscription.EMPTY : newStream.subscribe(emitter::emit);
      });

      return () -> {
        s.unsubscribe();
        mappedSubscription.unsubscribe();
        mappedSubscription = Subscription.EMPTY;
      };
    }

    @Override
    public U operate(T value) {
      @SuppressWarnings("unchecked") // cast is save as operate is only called for ValueStreams
      BaseValueStream<T, U> mappedStream = (BaseValueStream<T, U>)map(value);

      return mappedStream.getCurrentValue();
    }

    private ObservableStream<? extends U> map(T input) {
      return input == null ? nullReplacement.get() : mapper.apply(input);
    }
  }
}