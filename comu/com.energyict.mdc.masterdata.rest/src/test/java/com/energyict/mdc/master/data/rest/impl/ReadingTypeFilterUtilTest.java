package com.energyict.mdc.master.data.rest.impl;

import com.energyict.mdc.masterdata.rest.impl.ReadingTypeFilterUtil;

import org.junit.Test;


import static org.assertj.core.api.Assertions.assertThat;


public class ReadingTypeFilterUtilTest {

    @Test
    public void testSimplString() {
        assertThat(ReadingTypeFilterUtil.computeRegex("ABC")).isEqualTo("\\QABC\\E");
    }

    @Test
    public void testAsterix() {
        assertThat(ReadingTypeFilterUtil.computeRegex("AB*C")).isEqualTo("\\QAB\\E.*\\QC\\E");
    }

    @Test
    public void testQuestionMarkInString() {
        assertThat(ReadingTypeFilterUtil.computeRegex("AB?C")).isEqualTo("\\QAB\\E.{1}\\QC\\E");
    }

    @Test
    public void testSpaces() {
        assertThat(ReadingTypeFilterUtil.computeRegex("AB C")).isEqualTo("\\QAB C\\E");
    }

    @Test
    public void testBracket() {
        assertThat(ReadingTypeFilterUtil.computeRegex("AB(C")).isEqualTo("\\QAB(C\\E");
    }

    @Test
    public void testEscapedAsterix() {
        assertThat(ReadingTypeFilterUtil.computeRegex("ABC\\*XYZ")).isEqualTo("\\QABC*XYZ\\E");
    }

    @Test
    public void testAsterixAndQuestionMark() {
        assertThat(ReadingTypeFilterUtil.computeRegex("*A?")).isEqualTo("\\Q\\E.*\\QA\\E.{1}\\Q\\E");
    }

    @Test
    public void testAsterixAndEscapedAsterix1() {
        assertThat(ReadingTypeFilterUtil.computeRegex("A\\**")).isEqualTo("\\QA*\\E.*\\Q\\E");
    }

    @Test
    public void testAsterixAndEscapedAsterix2() {
        assertThat(ReadingTypeFilterUtil.computeRegex("A*\\*")).isEqualTo("\\QA\\E.*\\Q*\\E");
    }

    @Test
    public void testMultipleAsterix() {
        assertThat(ReadingTypeFilterUtil.computeRegex("A**")).isEqualTo("\\QA\\E.*\\Q\\E.*\\Q\\E");
    }

    @Test
    public void testQuestionMarkAndEscapedQuestionMark() {
        assertThat(ReadingTypeFilterUtil.computeRegex("A\\??")).isEqualTo("\\QA?\\E.{1}\\Q\\E");
    }

    @Test
    public void testMultipleEscaped() {
        assertThat(ReadingTypeFilterUtil.computeRegex("!!!!")).isEqualTo("\\Q!!!!\\E");
    }

    @Test
    public void testEscapedEndQuote() {
        assertThat(ReadingTypeFilterUtil.computeRegex("A\\EA")).isEqualTo("\\QA\\E\\\\E\\QA\\E");
    }
}
