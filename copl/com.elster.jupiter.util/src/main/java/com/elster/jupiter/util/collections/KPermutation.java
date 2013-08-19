package com.elster.jupiter.util.collections;

import java.util.ArrayList;
import java.util.List;

/**
 * A k-permutation of a List is a reordering of k elements of the n elements of the given list.
 * This class models one such selection and reordering.
 */
public class KPermutation {

    int[] indices;

    /**
     * Defines a k-permuation by index. While duplicate indices are not checked, and will produce reliable results, such instances are not strictly k-permutations.
     * They are however allowed for convenience.
     *
     * @param indices the selected indices in order.
     */
    public KPermutation(int... indices) {
        this.indices = indices;
    }

    /**
     * Builds the List that is the result of applying this k-permutation on the given List.
     * The algorithm allows for indices that are beyond the range of the given list and will fill nulls for those indices.
     * @param original
     * @param <T>
     * @return a new List instance containing the selected elements in order as determined by this k-permutation.
     */
    public <T> List<T> perform(List<T> original) {
        List<T> variation = new ArrayList<>(indices.length);
        for (int index : indices) {
            variation.add(indexSafeGet(original, index));
        }
        return variation;
    }

    private <T> T indexSafeGet(List<T> original, int index) {
        if (index < 0 || index >= original.size()) {
            return null;
        }
        return original.get(index);
    }
}
