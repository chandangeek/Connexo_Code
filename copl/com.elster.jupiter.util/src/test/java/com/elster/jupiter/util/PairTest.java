/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PairTest {
	@Test
	public void testHashCodeNullSafe() {
		Pair.<String, String>of(null, null).hashCode();
	}

	@Test
	public void testToStringNullSafe() {
		Pair.<String, String>of(null, null).toString();
	}

	@Test
	public void testToString() {
		assertThat(Pair.<String, String>of("A", "B").toString()).isEqualTo("(A,B)");
	}

	@Test
	public void testEqualsNullSafe() {
        assertThat(Pair.<String, String>of(null, null).equals(Pair.<Integer, Integer>of(null, null))).isTrue();
	}

	@Test
	public void testEqualsNull() {
        assertThat(Pair.<String, String>of("A", "B").equals(null)).isFalse();
	}

	@Test
	public void testEqualsDifferentClass() {
        assertThat(Pair.<String, String>of("A", "B").equals("(A,B)")).isFalse();
	}

	@Test
	public void testEqualsSame() {
		Pair<String, String> pair = Pair.<String, String>of("A", "B");
        assertThat(pair.equals(pair)).isTrue();
	}

	@Test
	public void testEqualityOneNull() {
        assertThat(Pair.<String, String>of(null, "B").equals(Pair.<String, String>of(null, "B"))).isTrue();
	}

	@Test
	public void testEqualitySecondNull() {
        assertThat(Pair.<String, String>of("B", null).equals(Pair.<String, String>of("B", null))).isTrue();
	}

	@Test
	public void testEquality() {
        assertThat(Pair.<String, String>of("A", "B").equals(Pair.<String, String>of("A", "B"))).isTrue();
	}

	@Test
	public void testEqualityFalse() {
        assertThat(Pair.<String, String>of("A", "B").equals(Pair.<String, String>of("B", "B"))).isFalse();
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
