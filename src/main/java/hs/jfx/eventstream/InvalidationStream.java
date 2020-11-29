package hs.jfx.eventstream;

import hs.jfx.eventstream.impl.DefaultStream;
import hs.jfx.eventstream.impl.MapStream;
import hs.jfx.eventstream.impl.TransactionalStream;

import java.util.Objects;
import java.util.function.Supplier;

public interface InvalidationStream extends Observable<Void> {

  default <T> ChangeStream<T> replace(Supplier<? extends T> supplier) {
    Objects.requireNonNull(supplier);

    return new MapStream.Change<>(this, v -> supplier.get(), supplier);
  }

  // Add note here that null is avoided in other streams so this may not do what you expected when mapping later
  default ValueStream<Void> withDefault() {
    return new DefaultStream<>(this, () -> null);
  }

  default InvalidationStream transactional() {
    return new TransactionalStream.Invalidation(this);
  }
}
