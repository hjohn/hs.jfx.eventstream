package hs.jfx.eventstream.core;

import java.util.Objects;

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
   * @param oldValue a value to use as previous value
   * @param currentValue a value to use as current value
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

  @Override
  public int hashCode() {
    return Objects.hash(currentValue, oldValue);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Change<?> other = (Change<?>)obj;

    return Objects.equals(currentValue, other.currentValue)
      && Objects.equals(oldValue, other.oldValue);
  }
}
