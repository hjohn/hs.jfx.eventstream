package hs.jfx.eventstream.api;

/**
 * Interface used for emitting a value.
 *
 * @param <T> the type of value emitted
 */
public interface Emitter<T> {

  /**
   * Emits the given value.
   *
   * @param value a value to emit, can be null
   */
  void emit(T value);
}
