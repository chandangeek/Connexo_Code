/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PairTest {
    @Test
    public void testToStringNullSafe() {
        assertThat(Pair.<String, String>of(null, null).toString()).isEqualTo("(null,null)");
    }

    @Test
    public void testToString() {
        assertThat(Pair.of("A", "B").toString()).isEqualTo("(A,B)");
    }

    @Test
    public void testEqualsAndHashCodeNullSafe() {
        assertThat(Pair.<String, String>of(null, null).equals(Pair.<Integer, Integer>of(null, null))).isTrue();
        assertThat(Pair.<String, String>of(null, null).hashCode()).isEqualTo(Pair.<Integer, Integer>of(null, null).hashCode());
    }

    @Test
    public void testEqualsNull() {
        assertThat(Pair.of("A", "B").equals(null)).isFalse();
        assertThat(Pair.of(null, null).equals(null)).isFalse();
    }

    @Test
    public void testEqualsDifferentClass() {
        assertThat(Pair.of("A", "B").equals("(A,B)")).isFalse();
    }

    @Test
    public void testEqualsSame() {
        Pair<String, String> pair = Pair.of("A", "B");
        assertThat(pair.equals(pair)).isTrue();
        assertThat(pair.hashCode()).isEqualTo(pair.hashCode());
    }

    @Test
    public void testEqualityOneNull() {
        assertThat(Pair.<String, String>of(null, "B").equals(Pair.<String, String>of(null, "B"))).isTrue();
        assertThat(Pair.<String, String>of(null, "B").hashCode()).isEqualTo(Pair.<String, String>of(null, "B").hashCode());
    }

    @Test
    public void testEqualitySecondNull() {
        assertThat(Pair.<String, String>of("B", null).equals(Pair.<String, String>of("B", null))).isTrue();
        assertThat(Pair.<String, String>of("B", null).hashCode()).isEqualTo(Pair.<String, String>of("B", null).hashCode());
    }

    @Test
    public void testEquality() {
        assertThat(Pair.of("A", "B").equals(Pair.of("A", "B"))).isTrue();
        assertThat(Pair.of("A", "B").hashCode()).isEqualTo(Pair.of("A", "B").hashCode());
    }

    @Test
    public void testEqualityFalse() {
        assertThat(Pair.of("A", "B").equals(Pair.of("B", "B"))).isFalse();
    }

    @Test
    public void testGetFirst() {
        assertThat(Pair.of("A", "B").getFirst()).isEqualTo("A");
    }

    @Test
    public void testGetLast() {
        assertThat(Pair.of("A", "B").getLast()).isEqualTo("B");
    }

    @Test
    public void testWithFirst() {
        assertThat(Pair.of("A", "B").withFirst("F")).isEqualTo(Pair.of("F", "B"));
    }

    @Test
    public void testWithLast() {
        assertThat(Pair.of("A", "B").withLast("F")).isEqualTo(Pair.of("A", "F"));
    }

    @Test
    public void testAsMap() {
        Map<String, String> map = Pair.of("A", "B").asMap();
        assertThat(map).hasSize(1)
                .containsKey("A")
                .containsValue("B");
    }

    @Test
    public void testWithFirstWithFunction() {
        Pair<String, Integer> pair = Pair.of("A", 5).withFirst((a, b) -> a.toLowerCase() + b);
        assertThat(pair).isEqualTo(Pair.of("a5", 5));
    }

    @Test
    public void testWithLastWithFunction() {
        Pair<String, String> pair = Pair.of("A", 5).withLast((a, b) -> a.toLowerCase() + b);
        assertThat(pair).isEqualTo(Pair.of("A", "a5"));
    }

    @Test
    public void testFlipped() {
        assertThat(Pair.of("A", 5).flipped()).isEqualTo(Pair.of(5, "A"));
    }
}
