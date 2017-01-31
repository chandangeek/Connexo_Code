/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.time.Instant;
import java.util.Optional;

import org.junit.Test;

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

    @Test
    public void emptyEqualsEmpty() {
        assertThat(Checks.is(Optional.empty()).equalTo(Optional.empty())).isTrue();
    }

    @Test
    public void presentNotEqualsEmpty() {
        assertThat(Checks.is(Optional.of("NotEmpty")).equalTo(Optional.empty())).isFalse();
    }

    @Test
    public void emptyNotEqualsPresent() {
        assertThat(Checks.is(Optional.empty()).equalTo(Optional.of("NotEmpty"))).isFalse();
    }

    @Test
    public void nonEqualOptionals() {
        assertThat(Checks.is(Optional.of("One")).equalTo(Optional.of("Two"))).isFalse();
    }

    @Test
    public void equalOptionals() {
        assertThat(Checks.is(Optional.of("One")).equalTo(Optional.of("One"))).isTrue();
    }

    @Test
    public void presentAndEquals() {
        assertThat(Checks.is(Optional.of("One")).presentAndEqualTo(Optional.of("One"))).isTrue();
    }

    @Test
    public void presentAndEqualsForDifferentValues() {
        assertThat(Checks.is(Optional.of("One")).presentAndEqualTo(Optional.of("Two"))).isFalse();
    }

    @Test
    public void presentAndEqualsWithFirstEmpty() {
        assertThat(Checks.is(Optional.empty()).presentAndEqualTo(Optional.of("One"))).isFalse();
    }

    @Test
    public void presentAndEqualsWithSecondEmpty() {
        assertThat(Checks.is(Optional.of("One")).presentAndEqualTo(Optional.empty())).isFalse();
    }

}