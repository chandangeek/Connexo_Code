package com.elster.jupiter.util.streams;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

class ConditionedLimitSpliterator<E> extends Spliterators.AbstractSpliterator<E> {

    private final Spliterator<E> spliterator;
    private final Predicate<? super E> predicate;

    ConditionedLimitSpliterator(Spliterator<E> spliterator, Predicate<? super E> predicate) {
        super(spliterator.estimateSize(), 0);
        this.spliterator = spliterator;
        this.predicate = predicate;
    }
    
    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        Objects.requireNonNull(action);

        HoldingConsumer<E> captor = new HoldingConsumer<>();

        if (!spliterator.tryAdvance(captor) || ! predicate.test(captor.getValue())) {
            return false;
        }

        action.accept(captor.getValue());
        return true;
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return (characteristics & getCharacteristics()) != 0;
    }

    private int getCharacteristics() {
        return spliterator.characteristics() & ~Spliterator.SIZED & ~Spliterator.SUBSIZED;
    }

}
