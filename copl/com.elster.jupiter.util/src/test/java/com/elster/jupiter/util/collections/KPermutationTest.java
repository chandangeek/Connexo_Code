/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KPermutationTest extends EqualsContractTest {

    private static final List<Character> ALPHABET = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');

    private KPermutation instanceA;

    @Test
    public void testPerformTrivial() throws Exception {

        KPermutation variation = new KPermutation(1, 4, 13, 8, 18, 14, 13);

        assertThat(variation.perform(ALPHABET)).isEqualTo(Arrays.asList('b', 'e', 'n', 'i', 's', 'o', 'n'));

    }

    @Test
    public void testPerformFillsNullsForIllegalIndices() throws Exception {

        KPermutation variation = new KPermutation(1, 4, 13, 8, 29, 14, 13);

        assertThat(variation.perform(ALPHABET)).isEqualTo(Arrays.asList('b', 'e', 'n', 'i', null, 'o', 'n'));

    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowNegativeIndices() throws Exception {

        KPermutation variation = new KPermutation(1, 4, 13, 8, -1, 14, 13);

    }

    @Test
    public void testEmptyVariation() {
        KPermutation variation = new KPermutation();

        assertThat(variation.perform(ALPHABET)).isEmpty();
    }

    @Test
    public void testIsPermutation() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f');

        KPermutation variation = new KPermutation(3, 4, 1, 0, 2, 5);

        assertThat(variation.isPermutation(characters)).isTrue();
    }

    @Test
    public void testIsTriviallyAPermutation() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f');

        KPermutation variation = new KPermutation(3, 4, 1, 0, 2, 5);

        assertThat(variation.isPermutation(characters)).isTrue();
    }

    @Test
    public void testIsNotAPermutationBySize() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f');

        KPermutation variation = new KPermutation(3, 4, 1, 0, 2);

        assertThat(variation.isPermutation(characters)).isFalse();
    }

    @Test
    public void testIsNotAPermutationByRepetition() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f');

        KPermutation variation = new KPermutation(3, 3, 1, 0, 1, 5);

        assertThat(variation.isPermutation(characters)).isFalse();
    }

    @Test
    public void testKPermutationOf() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f');
        KPermutation kpermutation = new KPermutation(4, 3, 0, 2);
        List<Character> kpermutated = kpermutation.perform(characters);

        assertThat(KPermutation.of(characters, kpermutated)).isEqualTo(kpermutation);

    }

    @Test
    public void testKPermutationOfLongArray() {
        long[] numbers = new long[] {0, 1, 2, 3, 4, 5};
        long[] kpermutated = new long[] {4, 3, 0, 2};

        assertThat(KPermutation.of(numbers, kpermutated)).isEqualTo(new KPermutation(4, 3, 0, 2));

    }

    @Test
    public void testIsNeutralObviouslyFalse() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f');
        KPermutation variation = new KPermutation(3, 4, 1, 0, 2, 5);

        assertThat(variation.isNeutral(characters)).isFalse();
    }

    @Test
    public void testIsNeutralObviouslyTrue() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f');
        KPermutation variation = new KPermutation(0, 1, 2, 3, 4, 5);

        assertThat(variation.isNeutral(characters)).isTrue();
    }

    @Test
    public void testIsNeutralSubtlyFalse() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g');
        KPermutation variation = new KPermutation(0, 1, 2, 3, 4, 5);

        assertThat(variation.isNeutral(characters)).isFalse();
    }


    @Test(expected = IllegalArgumentException.class)
    public void testKPermutationOfThatWontEverWork() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f');
        KPermutation kpermutation = new KPermutation(4, 3, 0, 2);
        List<Character> kpermutated = kpermutation.perform(characters);

        KPermutation result = KPermutation.of(kpermutated, characters);

    }

    @Test
    public void testKPermutationAndThen() {
        List<Character> characters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f');
        KPermutation kpermutation = new KPermutation(4, 3, 0, 2); //edac
        KPermutation second = new KPermutation(2, 1, 3); //adc

        assertThat(kpermutation.andThen(second)).isEqualTo(new KPermutation(0, 3, 2));

    }


    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new KPermutation(4, 3, 0, 2);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new KPermutation(4, 3, 0, 2);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new KPermutation(4, 3, 0, 2, 1),
                new KPermutation(3, 4, 0, 2),
                new KPermutation(4, 3, 0)
        );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
