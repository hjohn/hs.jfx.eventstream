package hs.jfx.eventstream.core.util;

import java.util.ArrayList;
import java.util.List;

public class Sink<T> {
  private List<T> values = new ArrayList<>();

  public void add(T value) {
    values.add(value);
  }

  public List<T> drain() {
    List<T> oldValues = values;

    values = new ArrayList<>();

    return oldValues;
  }

  public T single() {
    if(values.size() != 1) {
      throw new IllegalStateException("Expected exactly 1 value, but have: " + values);
    }

    return drain().get(0);
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }
}
