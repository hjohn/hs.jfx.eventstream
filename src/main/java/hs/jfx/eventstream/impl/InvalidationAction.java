package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.domain.Action;

public interface InvalidationAction extends Action<Void, Void> {
  @Override
  default Void operate(Void value) {
    throw new UnsupportedOperationException();
  }
}
