package hs.jfx.eventstream.core.impl;

public interface Emitter<T> {
  void emit(T value);
}
