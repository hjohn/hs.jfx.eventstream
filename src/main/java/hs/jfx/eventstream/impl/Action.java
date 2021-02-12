package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.ObservableStream;
import hs.jfx.eventstream.Subscription;

public interface Action<S, T> {
  Subscription observeInputs(ObservableStream<S> source, Emitter<T> emitter);
  T operate(S value);
}
