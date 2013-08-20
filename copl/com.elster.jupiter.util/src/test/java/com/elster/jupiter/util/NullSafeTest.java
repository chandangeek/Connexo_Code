package com.elster.jupiter.util;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class NullSafeTest {

    @Test
    public void testNullSafeHashCodeOnNull() {
        assertThat(NullSafe.of(null).hashCode()).isEqualTo(0);
    }

    @Test
    public void testNullSafeHashCodeOnObject() {
        assertThat(NullSafe.of("string").hashCode()).isEqualTo("string".hashCode());
    }

    @Test
    public void testNullSafeEqualsOfNullOnNull() {
        assertThat(NullSafe.of(null).equals(null)).isTrue();
    }

    @Test
    public void testNullSafeEqualsOfObjectOnNull() {
        assertThat(NullSafe.of("string").equals(null)).isFalse();
    }

    @Test
    public void testNullSafeEqualsOfNullOnObject() {
        assertThat(NullSafe.of(null).equals("string")).isFalse();
    }

    @Test
    public void testNullSafeEqualsOfObjectOnObject() {
        assertThat(NullSafe.of("string").equals("string")).isTrue();
    }

    @Test
    public void testNullSafeToStringOfNull() {
        assertThat(NullSafe.of(null).toString()).isEqualTo("null");
    }

    @Test
    public void testNullSafeToStringOfObject() {
        assertThat(NullSafe.of("string").toString()).isEqualTo("string".toString());
    }

}
