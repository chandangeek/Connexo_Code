/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class PartitionWhenSpliteratorTest {

    @Test
    public void testPartitionByStartingLetter() {
        Stream<String> stream = Arrays.asList("Ark", "Azure", "Ball", "Bench", "Breach", "Clover", "End", "Evergreen").stream();
        Stream<List<String>> listStream = PartitionWhenSpliterator.partitionWhen((a, b) -> a.charAt(0) != b.charAt(0), stream);
        List<List<String>> lists = listStream.collect(Collectors.toList());

        assertThat(lists).hasSize(4);

        assertThat(lists.get(0)).containsExactly("Ark", "Azure");
        assertThat(lists.get(1)).containsExactly("Ball", "Bench", "Breach");
        assertThat(lists.get(2)).containsExactly("Clover");
        assertThat(lists.get(3)).containsExactly("End", "Evergreen");
    }

    @Test
    public void testPartitionByStartingLetterEmptyStream() {
        Stream<String> stream = Arrays.<String>asList().stream();
        Stream<List<String>> listStream = PartitionWhenSpliterator.partitionWhen((a, b) -> a.charAt(0) != b.charAt(0), stream);
        List<List<String>> lists = listStream.collect(Collectors.toList());

        assertThat(lists).isEmpty();
    }

    @Test
    public void testPartitionByStartingLetterOnlyOnePartition() {
        Stream<String> stream = Arrays.asList("Ark", "Azure").stream();
        Stream<List<String>> listStream = PartitionWhenSpliterator.partitionWhen((a, b) -> a.charAt(0) != b.charAt(0), stream);
        List<List<String>> lists = listStream.collect(Collectors.toList());

        assertThat(lists).hasSize(1);

        assertThat(lists.get(0)).containsExactly("Ark", "Azure");
    }

    @Test
    public void testPartitionByIncreasingOrder() {
        Stream<String> stream = Arrays.asList("Ark", "Azure", "Ball", "Bench", "Almond", "Breach", "Clover", "End", "Evergreen").stream();
        Stream<List<String>> listStream = PartitionWhenSpliterator.partitionWhen((a, b) -> a.compareTo(b) > 0, stream);
        List<List<String>> lists = listStream.collect(Collectors.toList());

        assertThat(lists).hasSize(2);

        assertThat(lists.get(0)).containsExactly("Ark", "Azure", "Ball", "Bench");
        assertThat(lists.get(1)).containsExactly("Almond", "Breach", "Clover", "End", "Evergreen");
    }


}