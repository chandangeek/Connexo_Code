/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import com.elster.jupiter.util.Pair;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

final class ZipWithIndexSpliterator<T> extends Spliterators.AbstractSpliterator<Pair<T, Long>> {

    private final Spliterator<T> spliterator;
    private long index = 0;

    ZipWithIndexSpliterator(Spliterator<T> spliterator) {
        super(spliterator.estimateSize(), spliterator.characteristics());
        this.spliterator = spliterator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Pair<T, Long>> action) {
        Objects.requireNonNull(action);

        HoldingConsumer<T> captor = new HoldingConsumer<>();

        if (!spliterator.tryAdvance(captor)) {
            return false;
        }

        action.accept(Pair.of(captor.getValue(), index++));
        return true;
    }

}
