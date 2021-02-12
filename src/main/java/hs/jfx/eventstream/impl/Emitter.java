package hs.jfx.eventstream.impl;

public interface Emitter<T> {
  void emit(T value);
}
