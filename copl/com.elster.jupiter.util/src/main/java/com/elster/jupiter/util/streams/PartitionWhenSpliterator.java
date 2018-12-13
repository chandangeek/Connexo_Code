/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Comparator.naturalOrder;

final class PartitionWhenSpliterator<E> extends Spliterators.AbstractSpliterator<List<E>> {
    private final Spliterator<E> spliterator;
    private final BiPredicate<? super E, ? super E> partitionPredicate;
    private HoldingConsumer<E> holder;
    private Comparator<List<E>> comparator;

    public PartitionWhenSpliterator(Spliterator<E> toWrap, BiPredicate<? super E, ? super E> partitionPredicate) {
        super(Long.MAX_VALUE, toWrap.characteristics() & ~SIZED | NONNULL);
        this.spliterator = toWrap;
        this.partitionPredicate = partitionPredicate;
    }

    public static <E> Stream<List<E>> partitionWhen(BiPredicate<? super E, ? super E> partitionPredicate, Stream<E> in) {
        return StreamSupport.stream(new PartitionWhenSpliterator<>(in.spliterator(), partitionPredicate), false);
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<E>> action) {
        final HoldingConsumer<E> tempHolder;
        if (holder == null) {
            tempHolder = new HoldingConsumer<>();
            if (!spliterator.tryAdvance(tempHolder)) {
                return false;
            }
            holder = tempHolder;
        } else {
            tempHolder = holder;
        }
        ArrayList<E> partition = new ArrayList<>();
        boolean didAdvance;
        E lastAdded;
        do {
            lastAdded = tempHolder.getValue();
            partition.add(lastAdded);
        }
        while ((didAdvance = spliterator.tryAdvance(tempHolder)) && !partitionPredicate.test(lastAdded, tempHolder.getValue()));
        if (!didAdvance) {
            holder = null;
        }
        action.accept(partition);
        return true;
    }

    @Override
    public Comparator<? super List<E>> getComparator() {
        final Comparator<List<E>> c = this.comparator;
        return c != null ? c : (this.comparator = comparator());
    }

    private Comparator<List<E>> comparator() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Comparator<? super E> innerComparator =
                Optional.ofNullable(spliterator.getComparator())
                        .orElse((Comparator) naturalOrder());
        return (left, right) -> {
            final int c = innerComparator.compare(left.get(0), right.get(0));
            return c != 0 ? c : innerComparator.compare(
                    left.get(left.size() - 1), right.get(right.size() - 1));
        };
    }
}