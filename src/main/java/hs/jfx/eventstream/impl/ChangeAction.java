package hs.jfx.eventstream.impl;

public interface ChangeAction<S, T> extends Action<S, T> {
  @Override
  default T operate(S value) {
    throw new UnsupportedOperationException();
  }
}
