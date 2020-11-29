package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.Subscription;
import hs.jfx.eventstream.ValueStream;

import java.util.Objects;

import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.ObservableList;

public class ValueStreamBinding<T> extends ObservableValueBase<T> implements Binding<T> {
    private final Subscription subscription;
    private T value;

    public ValueStreamBinding(ValueStream<T> input) {
        value = Objects.requireNonNull(input).getCurrentValue();
        subscription = input.subscribe(v -> {
            value = v;
            fireValueChangedEvent();
        });
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void dispose() {
        subscription.unsubscribe();
    }

    @Override
    public ObservableList<?> getDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidate() {
        // do nothing
    }

    @Override
    public boolean isValid() {
        return true;
    }
}