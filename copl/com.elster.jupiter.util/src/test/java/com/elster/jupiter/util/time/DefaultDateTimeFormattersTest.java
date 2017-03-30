/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class DefaultDateTimeFormattersTest {
    @Rule
    public TestRule pinguinsUseJavaToo = Using.timeZoneOfMcMurdo();
    @Rule
    public TestRule locale = Using.locale("en", "UK");


    private final ZonedDateTime time;
    private final String expectedShortDate;
    private final String expectedLongDate;
    private final String expectedShortTime;
    private final String expectedLongTime;

    public DefaultDateTimeFormattersTest(TestRule locale, ZonedDateTime time, String expectedShortDate, String expectedLongDate, String expectedShortTime, String expectedLongTime) {
        this.locale = locale;
        this.time = time;
        this.expectedShortDate = expectedShortDate;
        this.expectedLongDate = expectedLongDate;
        this.expectedShortTime = expectedShortTime;
        this.expectedLongTime = expectedLongTime;
    }

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        return asList(
                new Object[]{Using.locale("en", "UK"), ZonedDateTime.of(2014, 11, 29, 18, 33, 43, 654321897, TimeZoneNeutral.getMcMurdo()), "29 Nov '14", "Sat 29 Nov '14", "18:33", "18:33:43"},
                new Object[]{Using.locale("en", "US"), ZonedDateTime.of(2014, 11, 29, 18, 33, 43, 654321897, TimeZoneNeutral.getMcMurdo()), "Nov-29-'14", "Sat, Nov-29-'14", "06:33 PM", "06:33:43 PM"},
                new Object[]{Using.locale("en", "UK"), ZonedDateTime.of(2014, 2, 5, 8, 3, 4, 654321897, TimeZoneNeutral.getMcMurdo()), "05 Feb '14", "Wed 05 Feb '14", "08:03", "08:03:04"},
                new Object[]{Using.locale("en", "US"), ZonedDateTime.of(2014, 2, 5, 8, 3, 4, 654321897, TimeZoneNeutral.getMcMurdo()), "Feb-05-'14", "Wed, Feb-05-'14", "08:03 AM", "08:03:04 AM"}
        );
    }


    @Test
    public void testShortDateNoTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.shortDate().build();

        assertThat(formatter.format(time)).isEqualTo(expectedShortDate);
    }

    @Test
    public void testNoDateShortTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.shortTime().build();

        assertThat(formatter.format(time)).isEqualTo(expectedShortTime);
    }

    @Test
    public void testNoDateLongTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.longTime().build();

        assertThat(formatter.format(time)).isEqualTo(expectedLongTime);
    }

    @Test
    public void testLongDateNoTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.longDate().build();

        assertThat(formatter.format(time)).isEqualTo(expectedLongDate);
    }

    @Test
    public void testLongDateLongTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.longDate().withLongTime().build();

        assertThat(formatter.format(time)).isEqualTo(expectedLongDate + ' ' + expectedLongTime);
    }

    @Test
    public void testShortDateShortTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.shortDate().withShortTime().build();

        assertThat(formatter.format(time)).isEqualTo(expectedShortDate + ' ' + expectedShortTime);
    }

    @Test
    public void testLongDateShortTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.longDate().withShortTime().build();

        assertThat(formatter.format(time)).isEqualTo(expectedLongDate + ' ' + expectedShortTime);
    }

    @Test
    public void testShortDateLongTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.longDate().withShortTime().build();

        assertThat(formatter.format(time)).isEqualTo(expectedLongDate + ' ' + expectedShortTime);
    }


}