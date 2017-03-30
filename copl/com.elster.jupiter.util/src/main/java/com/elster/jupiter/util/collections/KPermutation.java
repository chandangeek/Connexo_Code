/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;

import aQute.bnd.annotation.ProviderType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * A k-permutation of a List is a reordering of k elements of the n elements of the given list.
 * This class models one such selection and reordering.
 */
@ProviderType
public final class KPermutation {

    private final int[] indices;

    /**
     * Defines a k-permuation by index. While duplicate indices are not checked, and will produce reliable results, such instances are not strictly k-permutations.
     * They are however allowed for convenience.
     *
     * @param indices the selected indices in order.
     */
    public KPermutation(int... indices) {
        IntStream.of(indices).forEach(cantBeNegative());
        this.indices = Arrays.copyOf(indices, indices.length);
    }

    /**
     * Builds the List that is the result of applying this k-permutation on the given List.
     * The algorithm allows for indices that are beyond the range of the given list and will fill nulls for those indices.
     *
     * @param original The original List
     * @param <T> The type of elements in the original List
     * @return a new List instance containing the selected elements in order as determined by this k-permutation.
     */
    public <T> List<T> perform(List<T> original) {
        List<T> variation = new ArrayList<>(indices.length);
        for (int index : indices) {
            variation.add(indexSafeGet(original, index));
        }
        return variation;
    }

    private int[] perform(int[] original) {
        int[] result = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            result[i] = original[indices[i]];
        }
        return result;
    }

    public <T> boolean isPermutation(List<T> original) {
        if (original.size() != indices.length) {
            return false;
        }
        int expectedSum = ((indices.length - 1) * indices.length) / 2;
        return IntStream.of(indices)
                .distinct()
                .sum() == expectedSum;
    }

    public <T> boolean isNeutral(List<T> original) {
        if (original.size() != indices.length) {
            return false;
        }
        return IntStream.range(0, indices.length)
                .allMatch(i -> indices[i] == i);
    }

    public KPermutation andThen(KPermutation second) {
        int[] result = second.perform(indices);
        return new KPermutation(result);
    }

    public static <T> KPermutation of(List<? extends T> source, List<? super T> result) {
        Counter counter = Counters.newLenientCounter();
        int[] indices = result.stream()
                .mapToInt(source::indexOf)
                .peek(cantBeNegative())
                .collect(
                        () -> new int[result.size()],
                        (array, index) -> {
                            array[counter.getValue()] = index;
                            counter.increment();
                        },
                        null
                );
        return new KPermutation(indices);
    }

    public static <T> KPermutation of(long[] source, long[] result) {
        Counter counter = Counters.newLenientCounter();
        int[] indices = Arrays.stream(result)
                .mapToInt(value -> indexOf(source, value))
                .peek(cantBeNegative())
                .collect(
                        () -> new int[result.length],
                        (array, index) -> {
                            array[counter.getValue()] = index;
                            counter.increment();
                        },
                        null
                );
        return new KPermutation(indices);
    }

    private static int indexOf(long[] array, long value) {
        return IntStream.rangeClosed(0, array.length)
                .filter(i -> value == array[i])
                .findFirst()
                .orElse(-1);
    }

    private static IntConsumer cantBeNegative() {
        return i -> {
            if (i < 0) {
                throw new IllegalArgumentException();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KPermutation that = (KPermutation) o;

        return Arrays.equals(indices, that.indices);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(indices);
    }

    private <T> T indexSafeGet(List<T> original, int index) {
        if (index < 0 || index >= original.size()) {
            return null;
        }
        return original.get(index);
    }
}
