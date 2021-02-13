package hs.jfx.eventstream.core.util;

import java.util.function.Supplier;

public class StreamUtil {
  private static final Supplier<?> NULL_SUPPLIER = () -> null;

  @SuppressWarnings("unchecked")
  public static final <T> Supplier<T> nullSupplier() {
    return (Supplier<T>)NULL_SUPPLIER;
  }
}
