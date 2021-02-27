package hs.jfx.eventstream.api;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Similar to {@link java.util.Optional}, but considers <code>null</code> a valid value.<p>
 *
 * Used for values where <code>null</code> is a valid value, and where the absence of any
 * value (including <code>null</code>) needs to be signaled separately.
 *
 * @param <T> the type of the value that this can contain
 */
public final class OptionalValue<T> {
  private static final OptionalValue<?> EMPTY = new OptionalValue<>(null, false);

  /**
   * Creates a new {@link OptionalValue} with the given value as its
   * value. Accepts {@code null} as a valid value.
   *
   * @param <T> the type of the value that this can contain
   * @param value a value, can be null
   * @return a new {@link OptionalValue} instance with the given value, never null
   */
  public static <T> OptionalValue<T> of(T value) {
    return new OptionalValue<>(value, true);
  }

  /**
   * Creates a new {@link OptionalValue} with the given value as its
   * value, unless the value was {@code null} in which case an empty
   * {@link OptionalValue} is returned.
   *
   * @param <T> the type of the value that this can contain
   * @param value a value, can be null
   * @return a new {@link OptionalValue} instance, never null
   */
  public static <T> OptionalValue<T> ofNullable(T value) {
    return value == null ? OptionalValue.empty() : new OptionalValue<>(value, true);
  }

  /**
   * Creates a new empty {@link OptionalValue} instance.
   *
   * @param <T> the type of the value that this can contain
   * @return a new empty {@link OptionalValue}, never null
   */
  @SuppressWarnings("unchecked")
  public static <T> OptionalValue<T> empty() {
    return (OptionalValue<T>)EMPTY;
  }

  private final T value;
  private final boolean present;

  private OptionalValue(T value, boolean present) {
    this.value = value;
    this.present = present;
  }

  /**
   * Returns the value contained within this instance, or throws
   * {@link NoSuchElementException} if it is empty.
   *
   * @return a value, can be null
   * @throws NoSuchElementException when empty
   */
  public T get() {
    if(!present) {
      throw new NoSuchElementException("No value present");
    }

    return value;
  }

  /**
   * Supplies the given consumer with the contained value if present
   * otherwise does nothing.
   *
   * @param consumer a {@link Consumer} which is supplied with the contained value if present, cannot be null
   */
  public void ifPresent(Consumer<T> consumer) {
    Objects.requireNonNull(consumer);

    if(present) {
      consumer.accept(value);
    }
  }

  /**
   * Returns {@code true} if a value is present otherwise {@code false}.
   *
   * @return {@code true} if a value is present otherwise {@code false}
   */
  public boolean isPresent() {
    return present;
  }

  /**
   * Returns the contained value if present, otherwise returns the given
   * alternative value.
   *
   * @param alternativeValue a value to return if empty, can be null
   * @return the contained value if present, otherwise returns the given
   *     alternative value, can be null
   */
  public T orElse(T alternativeValue) {
    return present ? value : alternativeValue;
  }

  /**
   * If a value is present, applies the given mapper function to the contained value and returns
   * a new {@link OptionalValue} with the result, otherwise returns an empty {@link OptionalValue}.
   *
   * @param <U> the type of the value returned by the mapper function
   * @param mapper a mapping function, cannot be null
   * @return a new {@link OptionalValue} with the mapped result, otherwise an empty {@link OptionalValue}, never null
   */
  public <U> OptionalValue<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper);

    return present ? OptionalValue.of(mapper.apply(value)) : empty();
  }

  /**
   * If a value is present, applies the given mapper function to the contained value and returns
   * its result, otherwise returns an empty {@link OptionalValue}.
   *
   * @param <U> the type of the value returned by the mapper function
   * @param mapper a mapping function, cannot be null
   * @return the {@link OptionalValue} resulting from the mapper, otherwise an empty {@link OptionalValue}, never null
   */
  public <U> OptionalValue<U> flatMap(Function<? super T, ? extends OptionalValue<? extends U>> mapper) {
    Objects.requireNonNull(mapper);

    if(present) {
      @SuppressWarnings("unchecked")
      OptionalValue<U> other = Objects.requireNonNull((OptionalValue<U>)mapper.apply(value));

      return other;
    }

    return empty();
  }
}
