/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import com.elster.jupiter.util.Pair;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static com.elster.jupiter.util.Checks.is;
import static org.assertj.core.api.Assertions.assertThat;

public class ZipperTest {

    private List<String> strings = ImmutableList.of("B", "C", "D");
    private List<Integer> integers = ImmutableList.of(1, 3, 5);

    private Zipper.Matcher<String, Integer> matcher1 = (s, integer) -> !is(s).empty() && (s.charAt(0) - 'A' + 1) == integer;

    private Zipper.Matcher<Integer, String> matcher2 = (integer, s) -> !is(s).empty() && (s.charAt(0) - 'A' + 1) == integer;

    @Test
    public void testZipStringInteger() throws Exception {
        Zipper<String, Integer> zipper = new Zipper<>(matcher1);

        List<Pair<String, Integer>> zip = zipper.zip(strings, integers);

        assertThat(zip).isEqualTo(ImmutableList.of(
                Pair.of("B", null),
                Pair.of("C", 3),
                Pair.of("D", null),
                Pair.of(null, 1),
                Pair.of(null, 5)
        ));
    }

    @Test
    public void testZipIntegerString() throws Exception {
        Zipper<Integer, String> zipper = new Zipper<>(matcher2);

        List<Pair<Integer, String>> zip = zipper.zip(integers, strings);

        assertThat(zip).isEqualTo(ImmutableList.of(
                Pair.of(1, null),
                Pair.of(3, "C"),
                Pair.of(5, null),
                Pair.of(null, "B"),
                Pair.of(null, "D")
        ));
    }

    @Test
    public void testZipTrue() {
        Zipper<String, Integer> zipper = new Zipper<>((a, b) -> true);

        List<Pair<String, Integer>> zip = zipper.zip(strings, integers);

        assertThat(zip).isEqualTo(ImmutableList.of(
                Pair.of("B", 1),
                Pair.of("C", 3),
                Pair.of("D", 5)
        ));
    }

}