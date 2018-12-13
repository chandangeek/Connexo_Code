/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools;

import com.elster.jupiter.devtools.tests.rules.Using;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeZoneNeutralTestAsMethodRule {

    @Rule
    public TestRule rule = Using.timeZoneOfMcMurdo();

    @Test
    public void testJodaWinterTime() {
        DateTime someTimeInWinter = new DateTime(2014, 1, 1, 0, 0);
        assertThat(DateTimeZone.getDefault().getOffset(someTimeInWinter)).isEqualTo(13 * DateTimeConstants.MILLIS_PER_HOUR);
    }

    @Test
    public void testJodaSummerTime() {
        DateTime someTimeInSummer = new DateTime(2014, 8, 1, 0, 0);
        assertThat(DateTimeZone.getDefault().getOffset(someTimeInSummer)).isEqualTo(12 * DateTimeConstants.MILLIS_PER_HOUR);
    }

    @Test
    public void testWinterTime() {
        Date someTimeInWinter = new DateTime(2014, 1, 1, 0, 0).toDate();
        assertThat(TimeZone.getDefault().getOffset(someTimeInWinter.getTime())).isEqualTo(13 * DateTimeConstants.MILLIS_PER_HOUR);
    }

    @Test
    public void testSummerTime() {
        Date someTimeInSummer = new DateTime(2014, 8, 1, 0, 0).toDate();
        assertThat(TimeZone.getDefault().getOffset(someTimeInSummer.getTime())).isEqualTo(12 * DateTimeConstants.MILLIS_PER_HOUR);
    }

}
