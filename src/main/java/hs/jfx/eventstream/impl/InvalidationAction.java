package hs.jfx.eventstream.impl;

public interface InvalidationAction extends Action<Void, Void> {
  @Override
  default Void operate(Void value) {
    throw new UnsupportedOperationException();
  }
}
