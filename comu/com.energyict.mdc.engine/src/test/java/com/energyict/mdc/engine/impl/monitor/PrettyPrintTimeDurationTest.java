package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import org.joda.time.DateTimeConstants;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.monitor.PrettyPrintTimeDuration} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-06 (18:48)
 */
@RunWith(MockitoJUnitRunner.class)
public class PrettyPrintTimeDurationTest {

    private static final int DAYS_IN_WEEK = 7;
    private static final int SECONDS_IN_WEEK = DateTimeConstants.SECONDS_PER_HOUR * DateTimeConstants.HOURS_PER_DAY * DAYS_IN_WEEK;
    private static final int DAYS_IN_MONTH = 30;
    private static final int SECONDS_IN_MONTH = DateTimeConstants.SECONDS_PER_HOUR * DateTimeConstants.HOURS_PER_DAY * DAYS_IN_MONTH;
    private static final int DAYS_IN_YEAR = 365;
    private static final int SECONDS_IN_YEAR = DateTimeConstants.SECONDS_PER_HOUR * DateTimeConstants.HOURS_PER_DAY * DAYS_IN_YEAR;

    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setupThesaurus () {
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.YEAR_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.YEAR_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.YEAR_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.YEAR_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.MONTH_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.MONTH_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.MONTH_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.MONTH_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.DAY_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.DAY_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.DAY_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.DAY_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.HOUR_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.HOUR_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.HOUR_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.HOUR_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.MINUTE_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.MINUTE_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.MINUTE_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.MINUTE_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.SECOND_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.SECOND_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.SECOND_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.SECOND_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.SEPARATOR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.SEPARATOR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.LAST_SEPARATOR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.LAST_SEPARATOR));
    }

    @Test
    public void testZeroSeconds () {
        this.doTest(0, "0 seconds");
    }

    @Test
    public void testOneSeconds () {
        this.doTest(1, "1 second");
    }

    @Test
    public void testSecondsLessThanMinute () {
        this.doTest(DateTimeConstants.SECONDS_PER_MINUTE - 1, "59 seconds");
    }

    @Test
    public void testOneMinute () {
        this.doTest(DateTimeConstants.SECONDS_PER_MINUTE, "1 minute");
    }

    @Test
    public void testMinutesLessThanHour () {
        this.doTest(DateTimeConstants.SECONDS_PER_MINUTE * 5, "5 minutes");
    }

    @Test
    public void testMinutesAndSeconds () {
        this.doTest(DateTimeConstants.SECONDS_PER_MINUTE * 5 + 5, "5 minutes and 5 seconds");
    }

    @Test
    public void testOneHour () {
        this.doTest(DateTimeConstants.SECONDS_PER_HOUR, "1 hour");
    }

    @Test
    public void testMultipleHours () {
        this.doTest(DateTimeConstants.SECONDS_PER_HOUR * 3, "3 hours");
    }

    @Test
    public void testDurationLessThanDay () {
        this.doTest(DateTimeConstants.SECONDS_PER_DAY - (DateTimeConstants.SECONDS_PER_MINUTE - 1), "23 hours, 59 minutes and 1 second");
    }

    @Test
    public void testOneDay () {
        this.doTest(DateTimeConstants.SECONDS_PER_DAY, "1 day");
    }

    @Test
    public void testMultipleDays () {
        this.doTest(DateTimeConstants.SECONDS_PER_DAY * 3, "3 days");
    }

    @Test
    public void testDurationLessThanWeek () {
        this.doTest(SECONDS_IN_WEEK - 1, "6 days, 23 hours, 59 minutes and 59 seconds");
    }

    @Test
    public void testOneMonth () {
        this.doTest(SECONDS_IN_MONTH, "1 month");
    }

    @Test
    public void testMultipleMonths () {
        this.doTest(SECONDS_IN_MONTH * 3, "3 months");
    }

    @Test
    public void testOneYear () {
        this.doTest(SECONDS_IN_YEAR, "1 year");
    }

    @Test
    public void testMultipleYears () {
        this.doTest(SECONDS_IN_YEAR * 3, "3 years");
    }

    @Test
    public void testAllSingularUnits () {
        this.doTest(
                SECONDS_IN_YEAR
              + SECONDS_IN_MONTH
              + DateTimeConstants.SECONDS_PER_DAY
              + DateTimeConstants.SECONDS_PER_HOUR
              + DateTimeConstants.SECONDS_PER_MINUTE
              + 1,
                "1 year, 1 month, 1 day, 1 hour, 1 minute and 1 second");
    }

    @Test
    public void testAllPluralUnits () {
        this.doTest(
                SECONDS_IN_YEAR * 3
              + SECONDS_IN_MONTH * 7
              + DateTimeConstants.SECONDS_PER_DAY * 11
              + DateTimeConstants.SECONDS_PER_HOUR * 13
              + DateTimeConstants.SECONDS_PER_MINUTE * 17
              + 19,
                "3 years, 7 months, 11 days, 13 hours, 17 minutes and 19 seconds");
    }

    @Test
    public void testWithHoursAsLastNonZero () {
        this.doTest(
                SECONDS_IN_YEAR * 3
              + SECONDS_IN_MONTH * 7
              + DateTimeConstants.SECONDS_PER_DAY * 11
              + DateTimeConstants.SECONDS_PER_HOUR * 13,
                "3 years, 7 months, 11 days and 13 hours");
    }

    private void doTest (int seconds, String expectedPrintResult) {
        PrettyPrintTimeDuration timeDuration = new PrettyPrintTimeDuration(new TimeDuration(seconds), this.thesaurus);

        // Business method
        String prettyPrinted = timeDuration.toString();

        // Asserts
        assertThat(prettyPrinted).isEqualTo(expectedPrintResult);
    }

}