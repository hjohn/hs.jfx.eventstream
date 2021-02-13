package hs.jfx.eventstream.core.impl;

public interface ChangeAction<S, T> extends Action<S, T> {
  @Override
  default T operate(S value) {
    throw new UnsupportedOperationException();
  }
}
