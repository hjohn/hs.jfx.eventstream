package hs.jfx.eventstream.core.impl;

public interface InvalidationAction extends Action<Void, Void> {
  @Override
  default Void operate(Void value) {
    throw new UnsupportedOperationException();
  }
}
