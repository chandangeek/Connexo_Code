/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import org.junit.Test;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

public class PeriodicalScheduleExpressionParserStressTest {

    private PeriodicalScheduleExpressionParser parser = PeriodicalScheduleExpressionParser.INSTANCE;

    @Test
    public void testObviousMismatch() {
        assertThat(parser.parse("Not a good string")).isEmpty();
    }

    @Test
    public void testEmptyString() {
        assertThat(parser.parse("")).isEmpty();
    }

    @Test
    public void testEmptyBrackets() {
        assertThat(parser.parse("P[]")).isEmpty();
    }

    @Test
    public void testDayMixup1() {
        assertThat(parser.parse("P[4,WEEK,0,0,0,1]")).isEmpty();
    }

    @Test
    public void testDayMixup2() {
        assertThat(parser.parse("P[4,WEEK,0,0,0,LAST]")).isEmpty();
    }

    @Test
    public void testDayMixup3() {
        assertThat(parser.parse("P[2,MONTH,0,0,0,TUESDAY]")).isEmpty();
    }


}