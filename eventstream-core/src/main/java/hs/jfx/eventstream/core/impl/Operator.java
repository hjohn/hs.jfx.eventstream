package hs.jfx.eventstream.core.impl;

import hs.jfx.eventstream.api.OptionalValue;

public interface Operator<S, T> {

  /**
   * Performs a single operation on the given value and returns the result.<p>
   *
   * It's possible that there is no result, in which case the returned {@link OptionalValue}
   * will be empty. Note that {@code null} is a valid non-empty result.
   *
   * @param value a source value
   * @return an {@link OptionalValue}, never null
   */
  OptionalValue<T> operate(S value);
}
