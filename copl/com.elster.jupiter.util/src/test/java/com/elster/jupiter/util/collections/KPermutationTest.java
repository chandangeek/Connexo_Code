package com.elster.jupiter.util.collections;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class KPermutationTest {

    private static final List<Character> ALPHABET = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');

    @Test
    public void testPerformTrivial() throws Exception {

        KPermutation variation = new KPermutation(1, 4 , 13, 8, 18, 14, 13);

        assertThat(variation.perform(ALPHABET)).isEqualTo(Arrays.asList('b', 'e', 'n', 'i', 's', 'o', 'n'));

    }

    @Test
    public void testPerformFillsNullsForIllegalIndices() throws Exception {

        KPermutation variation = new KPermutation(1, 4 , 13, 8, -1, 14, 13);

        assertThat(variation.perform(ALPHABET)).isEqualTo(Arrays.asList('b', 'e', 'n', 'i', null, 'o', 'n'));

    }

    @Test
    public void testEmptyVariation() {
        KPermutation variation = new KPermutation();

        assertThat(variation.perform(ALPHABET)).isEmpty();
    }

}
