package com.energyict.mdc.master.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.masterdata.rest.impl.ReadingTypeFilterUtil;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


public class ReadingTypeFilterUtilTest extends MasterDataApplicationJerseyTest{

    private static final String fullAliasName1 = "full alias name 1";
    private static final String fullAliasName2 = "full alias name 2";

    private List<ReadingType> readingTypes;

    @Before
    public void setupReadingTypesList(){
        ReadingType rt1 = mockReadingType();
        ReadingType rt2 = mockReadingType();
        when(rt1.getFullAliasName()).thenReturn(fullAliasName1);
        when(rt2.getFullAliasName()).thenReturn(fullAliasName2);
        this.readingTypes = Arrays.asList(rt1, rt2);
    }

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

    @Test
    public void whenValidMRIDRegexThenReturnValidUniqueMRID(){
        String regex = "0\\.0\\.0.\\.0\\.1\\.1\\.(12|37)\\.0\\.0\\.0\\.0\\.0\\.[0-9]+\\.[0-9]+\\.0\\.-?[0-9]+\\.[0-9]+\\.[0-9]+";
        String response = ReadingTypeFilterUtil.extractUniqueFromRegex(regex);
        assertThat(response).isEqualTo("0.0.0.0.1.1.0.0.0.0.0.0.0.0.0.0.0.0");
    }

    @Test
    public void whenSingleValueRegexThenReturnSameValue(){
        String regex = "5";
        String response = ReadingTypeFilterUtil.extractUniqueFromRegex(regex);
        assertThat(response).isEqualTo(regex);
    }

    @Test
    public void whenGroupValueRegexThenReturnDefaultValue(){
        String regex = "(1|2|3)";
        String response = ReadingTypeFilterUtil.extractUniqueFromRegex(regex);
        assertThat(response).isEqualTo(ReadingTypeFilterUtil.DEFAULT_VALUE);
    }

    @Test
    public void whenAnyNumberRegexThenReturnDefaultValue(){
        String regex = "-?[0-9]+";
        String response = ReadingTypeFilterUtil.extractUniqueFromRegex(regex);
        assertThat(response).isEqualTo(ReadingTypeFilterUtil.DEFAULT_VALUE);
    }

    @Test
    public void whenEmptySearchTextThenReturnSameList() {
        List<ReadingType> response = ReadingTypeFilterUtil.getFilteredList("", readingTypes);
        assertThat(response).isEqualTo(readingTypes);
    }

    @Test
    public void whenSearchTextMatchesAllReadingTypesThenReturnSameList() {
        List<ReadingType> response = ReadingTypeFilterUtil.getFilteredList("full name", readingTypes);
        assertThat(response).isEqualTo(readingTypes);
    }

    @Test
    public void whenSearchTextMatchesAllReadingTypesAndHasCapitalLettersThenReturnTheSameList(){
        List<ReadingType> response = ReadingTypeFilterUtil.getFilteredList("FULL name", readingTypes);
        assertThat(response).isEqualTo(readingTypes);
    }

    @Test
    public void whenSearchTextMatchesOneReadingTypeThenReturnThatReadingType() {
        List<ReadingType> response = ReadingTypeFilterUtil.getFilteredList("1", readingTypes);
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getFullAliasName()).isEqualTo(fullAliasName1);
    }

    @Test
    public void whenSearchTextDoesntMatchAnyReadingTypeThenReturnEmptyList() {
        List<ReadingType> response = ReadingTypeFilterUtil.getFilteredList("3", readingTypes);
        assertThat(response).isEmpty();
    }

}
