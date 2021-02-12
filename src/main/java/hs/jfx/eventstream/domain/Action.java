package hs.jfx.eventstream.domain;

public interface Action<S, T> {
  Subscription observeInputs(ObservableStream<S> source, Emitter<T> emitter);
  T operate(S value);

  public static <T> Action<T, T> nothing() {
    return new Action<>() {
      @Override
      public Subscription observeInputs(ObservableStream<T> source, Emitter<T> emitter) {
        return Subscription.EMPTY;
      }

      @Override
      public T operate(T value) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
