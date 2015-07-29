package com.elster.jupiter.util.streams;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

public class ExtraCollectorsTest {

    @Test
    public void testToImmutableListTrivial() throws Exception {
        List<String> strings = Arrays.asList("A", "B", "C", "D");

        ImmutableList<String> immutableList = strings.stream()
                .collect(ExtraCollectors.toImmutableList());

        assertThat(immutableList).isInstanceOf(ImmutableList.class)
                .isEqualTo(strings);
    }

    @Test
    public void testToImmutableListParallel() throws Exception {
        List<String> strings = Arrays.asList("A", "B", "C", "D");

        ImmutableList<String> immutableList = strings.parallelStream()
                .collect(ExtraCollectors.toImmutableList());

        assertThat(immutableList).isInstanceOf(ImmutableList.class)
                .isEqualTo(strings);
    }

    @Test
    public void testEmptyStream() throws Exception {
        List<String> strings = Collections.emptyList();

        ImmutableList<String> immutableList = strings.stream()
                .collect(ExtraCollectors.toImmutableList());

        assertThat(immutableList).isInstanceOf(ImmutableList.class)
                .isEqualTo(strings);
    }

    @Test
    public void testToImmutableListSingleton() throws Exception {
        List<String> strings = Collections.singletonList("A");

        ImmutableList<String> immutableList = strings.stream()
                .collect(ExtraCollectors.toImmutableList());

        assertThat(immutableList).isInstanceOf(ImmutableList.class)
                .isEqualTo(strings);
    }


}