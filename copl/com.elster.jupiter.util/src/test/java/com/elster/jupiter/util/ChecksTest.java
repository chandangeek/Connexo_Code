package com.elster.jupiter.util;

import org.junit.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class ChecksTest {

    @Test
    public void testNullEqualsNull() {
        assertThat(Checks.is((Instant) null).equalTo(null)).isTrue();
    }

    @Test
    public void testNullDoesntEqualActualObject() {
        assertThat(Checks.is((Instant) null).equalTo(Instant.EPOCH)).isFalse();
    }

    @Test
    public void testNormalEqualityIsMaintained() {
        assertThat(Checks.is(Instant.EPOCH).equalTo(Instant.ofEpochMilli(0))).isTrue();
    }

    @Test
    public void testNormalInequalityIsMaintained() {
        assertThat(Checks.is(Instant.EPOCH).equalTo(Instant.ofEpochMilli(1))).isFalse();
    }

    @Test
    public void testNullEqualsNullForStrings() {
        assertThat(Checks.is((String) null).equalTo(null)).isTrue();
    }

    @Test
    public void testNullDoesntEqualActualObjectForStrings() {
        assertThat(Checks.is((String) null).equalTo("")).isFalse();
    }

    @Test
    public void testNormalEqualityIsMaintainedForStrings() {
        assertThat(Checks.is("abc").equalTo("abc")).isTrue();
    }

    @Test
    public void testNormalInequalityIsMaintainedForStrings() {
        assertThat(Checks.is("abc").equalTo("abd")).isFalse();
    }

    @Test
    public void testEmptyOrOnlyWhiteSpaceTriviallyFalse() {
        assertThat(Checks.is("Fidelity").emptyOrOnlyWhiteSpace()).isFalse();
    }

    @Test
    public void testEmptyOrOnlyWhiteSpaceOnNullIsTrue() {
        assertThat(Checks.is((String) null).emptyOrOnlyWhiteSpace()).isTrue();
    }

    @Test
    public void testEmptyOrOnlyWhiteSpaceOnZeroLengthStringIsTrue() {
        assertThat(Checks.is("").emptyOrOnlyWhiteSpace()).isTrue();
    }

    @Test
    public void testEmptyOrOnlyWhiteSpaceOnAllWhiteSpaceStringIsTrue() {
        String allWhitespaceCharacters = " \t\n\u000B\f\r\u001C\u001D\u001E\u001F";
        assertThat(Checks.is(allWhitespaceCharacters).emptyOrOnlyWhiteSpace()).isTrue();
    }

    @Test
    public void testOnlyWhiteSpaceOnNullIsFalse() {
        assertThat(Checks.is((String) null).onlyWhiteSpace()).isFalse();
    }

    @Test
    public void testOnlyWhiteSpaceOnAllWhiteSpaceStringIsTrue() {
        String allWhitespaceCharacters = " \t\n\u000B\f\r\u001C\u001D\u001E\u001F";
        assertThat(Checks.is(allWhitespaceCharacters).onlyWhiteSpace()).isTrue();
    }

    @Test
    public void testInTriviallyFalse() {
        assertThat(Checks.is("T").in("A", "B", "C")).isFalse();
    }

    @Test
    public void testInFalseWhenEmpty() {
        assertThat(Checks.is("T").in()).isFalse();
    }

    @Test
    public void testInTriviallyTrue() {
        assertThat(Checks.is("T").in("A", "B", "T", "C")).isTrue();
    }

    @Test
    public void testInTrueForNull() {
        assertThat(Checks.is((Object) null).in("A", null, "T", "C")).isTrue();
    }

}
