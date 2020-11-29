package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.Observable;
import hs.jfx.eventstream.Subscription;

public interface Action<S, T> {
  Subscription observeInputs(Observable<S> source, Emitter<T> emitter);
  T operate(S value);
}
