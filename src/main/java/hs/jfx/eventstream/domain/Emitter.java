package hs.jfx.eventstream.domain;

public interface Emitter<T> {
  void emit(T value);
}
