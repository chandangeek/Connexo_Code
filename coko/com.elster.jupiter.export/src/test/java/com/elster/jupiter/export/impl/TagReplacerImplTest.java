package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.export.StructureMarker;
import com.google.common.collect.Range;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

public class TagReplacerImplTest {

    private Clock clock = Clock.fixed(ZonedDateTime.of(2015, 6, 4, 14, 20, 55, 115451452, TimeZoneNeutral.getMcMurdo()).toInstant(), TimeZoneNeutral.getMcMurdo());
    private ZonedDateTime from = ZonedDateTime.of(2015, 4, 8, 9, 34, 28, 649616684, TimeZoneNeutral.getMcMurdo());
    private ZonedDateTime to = ZonedDateTime.of(2015, 5, 9, 10, 44, 38, 354057457, TimeZoneNeutral.getMcMurdo());
    private Range<Instant> period = Range.closed(from.toInstant(), to.toInstant());
    private StructureMarker structureMarker = DefaultStructureMarker.createRoot(clock, "root").withPeriod(period);
    private TagReplacerImpl tagReplacer = new TagReplacerImpl(structureMarker, clock, 17);

    @Test
    public void testSeconds() {
        String template = "aFlurryOfTextInter<sec>spersedWithTags";
        String expected = "aFlurryOfTextInter55spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDate() {
        String template = "aFlurryOfTextInter<date>spersedWithTags";
        String expected = "aFlurryOfTextInter20150604spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testTime() {
        String template = "aFlurryOfTextInter<time>spersedWithTags";
        String expected = "aFlurryOfTextInter142055spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testMillisec() {
        String template = "aFlurryOfTextInter<millisec>spersedWithTags";
        String expected = "aFlurryOfTextInter115spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDateYear() {
        String template = "aFlurryOfTextInter<dateyear>spersedWithTags";
        String expected = "aFlurryOfTextInter2015spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDateMonth() {
        String template = "aFlurryOfTextInter<datemonth>spersedWithTags";
        String expected = "aFlurryOfTextInter06spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDateDay() {
        String template = "aFlurryOfTextInter<dateday>spersedWithTags";
        String expected = "aFlurryOfTextInter04spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDatePattern() {
        String template = "aFlurryOfTextInter<dateformat:qqan'text'>spersedWithTags";
        String expectedReplacement = "02PM115451452text";
        String expected = "aFlurryOfTextInter" + expectedReplacement + "spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testIdentifier() {
        String template = "aFl<identifier>urryOfTextInter<identifier>spersedWithTags";
        String expected = "aFlrooturryOfTextInterrootspersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDataDate() {
        String template = "aFlurryOfTextInter<datadate>spersedWithTags";
        String expected = "aFlurryOfTextInter20150408spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDataTime() {
        String template = "aFlurryOfTextInter<datatime>spersedWithTags";
        String expected = "aFlurryOfTextInter093428spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDataEndDate() {
        String template = "aFlurryOfTextInter<dataenddate>spersedWithTags";
        String expected = "aFlurryOfTextInter20150509spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDataEndTime() {
        String template = "aFlurryOfTextInter<dataendtime>spersedWithTags";
        String expected = "aFlurryOfTextInter104438spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDataYearMonth() {
        String template = "aFlurryOfTextInter<datayearandmonth>spersedWithTags";
        String expected = "aFlurryOfTextInter201504spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testSeqNr() {
        String template = "aFlurryOfTextInter<seqnrwithinday>spersedWithTags";
        String expected = "aFlurryOfTextInter17spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testSeqNrNeedingFill() {
        String template = "aFlurryOfTextInter<seqnrwithinday>spersedWithTags";
        String expected = "aFlurryOfTextInter03spersedWithTags";

        assertThat(new TagReplacerImpl(structureMarker, clock, 3).replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testMixedTags() {
        String template = "aFlur<dateday><datemonth><dateyear>ryOfT<millisec>ext<identifier>Inter<sec><date><time>spersedWithTa<dateformat:qqan'text'>gs";
        String expected = "aFlur04062015ryOfT115extrootInter5520150604142055spersedWithTa02PM115451452textgs";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }



}