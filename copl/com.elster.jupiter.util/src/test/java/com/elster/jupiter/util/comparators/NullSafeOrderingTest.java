package com.elster.jupiter.util.comparators;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullSafeOrderingTest {

    @Test
    public void testNullIsSmallestBothNull() {
        assertThat(NullSafeOrdering.NULL_IS_SMALLEST.get().compare(null, null)).isEqualTo(0);
    }

    @Test
    public void testNullIsGreatestBothNull() {
        assertThat(NullSafeOrdering.NULL_IS_GREATEST.get().compare(null, null)).isEqualTo(0);
    }

    @Test
    public void testNullIsSmallestFirstIsNull() {
        assertThat(NullSafeOrdering.NULL_IS_SMALLEST.<String>get().compare(null, "A")).isLessThan(0);
    }

    @Test
    public void testNullIsGreatestFirstIsNull() {
        assertThat(NullSafeOrdering.NULL_IS_GREATEST.<String>get().compare(null, "A")).isGreaterThan(0);
    }

    @Test
    public void testNullIsSmallestSecondIsNull() {
        assertThat(NullSafeOrdering.NULL_IS_SMALLEST.<String>get().compare("A", null)).isGreaterThan(0);
    }

    @Test
    public void testNullIsGreatestSecondIsNull() {
        assertThat(NullSafeOrdering.NULL_IS_GREATEST.<String>get().compare("A", null)).isLessThan(0);
    }

    @Test
    public void testNullIsSmallestKeepsNaturalOrdering() {
        assertThat(NullSafeOrdering.NULL_IS_SMALLEST.<String>get().compare("A", "B")).isLessThan(0);
    }

    @Test
    public void testNullIsGreatestKeepsNaturalOrdering() {
        assertThat(NullSafeOrdering.NULL_IS_GREATEST.<String>get().compare("A", "B")).isLessThan(0);
    }

}