package hs.jfx.eventstream.core;

/**
 * Represents a change of a value, containing the old value and the current value.
 *
 * @param <T> type of the value that changed
 */
public class Change<T> {
  private final T oldValue;
  private final T currentValue;

  /**
   * Constructs a new instance with the given values.
   *
   * @param <T> the type of values the change contains
   * @param old a value to use as previous value
   * @param current a value to use as current value
   * @return a new {@link Change} instance, never null
   */
  public static <T> Change<T> of(T old, T current) {
    return new Change<>(old, current);
  }

  private Change(T oldValue, T currentValue) {
    this.oldValue = oldValue;
    this.currentValue = currentValue;
  }

  /**
   * Returns the old value.
   *
   * @return the old value
   */
  public T getOldValue() {
    return oldValue;
  }

  /**
   * Returns the current value
   *
   * @return the current value
   */
  public T getValue() {
    return currentValue;
  }
}
