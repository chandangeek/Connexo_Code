package com.elster.jupiter.util.time;

import com.elster.jupiter.devtools.tests.rules.Using;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

public class DefaultDateTimeFormattersTest {

    @Rule
    public TestRule pinguinsUseJavaToo = Using.timeZoneOfMcMurdo();
    @Rule
    public TestRule blighty = Using.locale("en", "UK");


    private ZonedDateTime time;

    @Before
    public void setUp() {
        time = ZonedDateTime.of(2014, 11, 29, 8, 33, 43, 654321897, ZoneId.systemDefault());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testLongDateMediumTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.mediumDate().withLongTime().build();

        assertThat(formatter.format(time)).isEqualTo("Sat, 29 Nov 2014 08:33:43 NZDT");
    }

    @Test
    public void testShortDateNoTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.shortDate().build();

        assertThat(formatter.format(time)).isEqualTo("2014-11-29");
    }

    @Test
    public void testNoDateShortTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.shortTime().build();

        assertThat(formatter.format(time)).isEqualTo("08:33");
    }

    @Test
    public void testNoDateMediumTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.mediumTime().build();

        assertThat(formatter.format(time)).isEqualTo("08:33");
    }

    @Test
    public void testNoDateLongTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.longTime().build();

        assertThat(formatter.format(time)).isEqualTo("08:33:43");
    }

    @Test
    public void testLongDateLongTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.longDate().withLongTime().build();

        assertThat(formatter.format(time)).isEqualTo("Saturday, 29 November 2014 08:33:43 NZDT");
    }

    @Test
    public void testShortDateMediumTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.shortDate().withMediumTime().build();

        assertThat(formatter.format(time)).isEqualTo("2014-11-29 08:33 NZDT");
    }

    @Test
    public void testShortDateShortTime() {
        DateTimeFormatter formatter = DefaultDateTimeFormatters.shortDate().withShortTime().build();

        assertThat(formatter.format(time)).isEqualTo("2014-11-29 08:33 NZDT");
    }


}