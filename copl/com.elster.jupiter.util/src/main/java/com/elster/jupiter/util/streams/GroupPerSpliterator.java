package com.elster.jupiter.util.streams;

import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

class GroupPerSpliterator<E> extends Spliterators.AbstractSpliterator<List<E>> {

    private final Spliterator<E> spliterator;
    private final int produceSize;
    private Comparator<List<E>> comparator;

    protected GroupPerSpliterator(Spliterator<E> spliterator, int produceSize) {
        super(size(spliterator, produceSize), characteristics(spliterator, produceSize));
        if (produceSize < 1) {
            throw new IllegalArgumentException();
        }
        this.spliterator = spliterator;
        this.produceSize = produceSize;
    }

    private static <E> long size(Spliterator<E> spliterator, int produceSize) {
        long estimateSize = spliterator.estimateSize();
        if (estimateSize == Long.MAX_VALUE) {
            return estimateSize;
        }
        long size = estimateSize / produceSize;
        if (size * produceSize < estimateSize) {
            size++;
        }
        return size;
    }

    private static <E> int characteristics(Spliterator<E> spliterator, int produceSize) {
        return spliterator.characteristics() | Spliterator.DISTINCT | Spliterator.NONNULL;
    }


    @Override
    public boolean tryAdvance(Consumer<? super List<E>> action) {
        Objects.requireNonNull(action);
        ImmutableList.Builder<E> builder = ImmutableList.builder();

        HoldingConsumer<E> captor = new HoldingConsumer<>();
        for (int i = 0; i < produceSize; i++) {
            if (spliterator.tryAdvance(captor)) {
                builder.add(captor.getValue());
            }
        }
        ImmutableList<E> list = builder.build();
        if (list.isEmpty()) {
            return false;
        }
        action.accept(list);
        return true;
    }

    @Override
    public Comparator<? super List<E>> getComparator() {
        if (!spliterator.hasCharacteristics(Spliterator.SORTED)) {
            throw new IllegalStateException();
        }
        if (this.comparator == null) {
            comparator = initComparator();
        }
        return this.comparator;
    }

    private Comparator<List<E>> initComparator() {
        Comparator<? super E> underlying = spliterator.getComparator();
        underlying = underlying == null ? (Comparator<? super E>) Comparator.naturalOrder() : underlying;
        return Comparator.comparing(list -> list.get(0), underlying);
    }

}
