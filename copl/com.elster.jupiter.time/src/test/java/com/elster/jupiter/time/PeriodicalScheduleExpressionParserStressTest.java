package com.elster.jupiter.time;

import org.junit.Test;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

public class PeriodicalScheduleExpressionParserStressTest {

    private PeriodicalScheduleExpressionParser parser = PeriodicalScheduleExpressionParser.INSTANCE;

    @Test
    public void testObviousMismatch() {
        assertThat(parser.parse("Not a good string")).isAbsent();
    }

    @Test
    public void testEmptyString() {
        assertThat(parser.parse("")).isAbsent();
    }

    @Test
    public void testEmptyBrackets() {
        assertThat(parser.parse("P[]")).isAbsent();
    }

    @Test
    public void testDayMixup1() {
        assertThat(parser.parse("P[4,WEEK,0,0,0,1]")).isAbsent();
    }

    @Test
    public void testDayMixup2() {
        assertThat(parser.parse("P[4,WEEK,0,0,0,LAST]")).isAbsent();
    }

    @Test
    public void testDayMixup3() {
        assertThat(parser.parse("P[2,MONTH,0,0,0,TUESDAY]")).isAbsent();
    }


}