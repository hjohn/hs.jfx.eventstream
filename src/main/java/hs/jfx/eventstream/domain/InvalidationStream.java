package hs.jfx.eventstream.domain;

import java.util.function.Supplier;

public interface InvalidationStream extends ObservableStream<Void> {

  <T> ChangeStream<T> replace(Supplier<? extends T> supplier);

  // Add note here that null is avoided in other streams so this may not do what you expected when mapping later
  ValueStream<Void> withDefault();

  InvalidationStream transactional();
}
