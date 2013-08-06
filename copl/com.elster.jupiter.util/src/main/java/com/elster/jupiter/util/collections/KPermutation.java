package com.elster.jupiter.util.collections;

import java.util.ArrayList;
import java.util.List;

public class KPermutation {

    int[] indices;

    public KPermutation(int... indices) {
        this.indices = indices;
    }

    /**
     * Builds the List that is the result of applying this k-permutation on the given List.
     * The algorithm allows for indices that are beyond the range of the given list and will fill nulls for those indices.
     * @param original
     * @param <T>
     * @return
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
