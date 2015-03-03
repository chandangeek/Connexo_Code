package com.elster.jupiter.util.streams;

import java.util.function.Consumer;

final class HoldingConsumer<T> implements Consumer<T> {

    private T value;

    @Override
    public void accept(T value) {
        this.value = value;
    }

    T getValue() {
        return value;
    }
}
