package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.ChangeStream;
import hs.jfx.eventstream.domain.ObservableStream;
import hs.jfx.eventstream.domain.Subscription;
import hs.jfx.eventstream.domain.ValueStream;

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
        mappedSubscription.unsubscribe();
        mappedSubscription = map(t).subscribe(emitter::emit);
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