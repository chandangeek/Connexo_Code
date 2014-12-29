package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import java.time.Duration;
import java.time.Period;
import org.junit.Ignore;
import org.junit.Test;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

/**
 * Created by bvn on 12/29/14.
 */
public class TemporalExpressionInfoTest {

    @Test
    @Ignore // Seconds need not be supported so far
    public void testSecondsConversion() throws Exception {
        Duration duration = Duration.ofSeconds(30);
        TemporalExpressionInfo info = TemporalExpressionInfo.from(duration);
        assertThat(info.every.count).isEqualTo(30);
        assertThat(info.every.timeUnit).isEqualTo(TimeDuration.TimeUnit.SECONDS.getDescription());
        assertThat(info.offset).isNull();
        assertThat(info.lastDay).isFalse();
    }

    @Test
    public void testHoursConversion() throws Exception {
        Duration duration = Duration.ofHours(1);
        TemporalExpressionInfo info = TemporalExpressionInfo.from(duration);
        assertThat(info.every.count).isEqualTo(1);
        assertThat(info.every.timeUnit).isEqualTo(TimeDuration.TimeUnit.HOURS.getDescription());
        assertThat(info.offset).isNull();
        assertThat(info.lastDay).isFalse();
    }

    @Test
    public void testMinutesConversion() throws Exception {
        Duration duration = Duration.ofMinutes(15);
        TemporalExpressionInfo info = TemporalExpressionInfo.from(duration);
        assertThat(info.every.count).isEqualTo(15);
        assertThat(info.every.timeUnit).isEqualTo(TimeDuration.TimeUnit.MINUTES.getDescription());
        assertThat(info.offset).isNull();
        assertThat(info.lastDay).isFalse();
    }

    @Test
    public void testDaysConversion() throws Exception {
        Period period = Period.ofDays(1);
        TemporalExpressionInfo info = TemporalExpressionInfo.from(period);
        assertThat(info.every.count).isEqualTo(1);
        assertThat(info.every.timeUnit).isEqualTo(TimeDuration.TimeUnit.DAYS.getDescription());
        assertThat(info.offset).isNull();
        assertThat(info.lastDay).isFalse();
    }

    @Test
    public void testMonthsConversion() throws Exception {
        Period period = Period.ofMonths(1);
        TemporalExpressionInfo info = TemporalExpressionInfo.from(period);
        assertThat(info.every.count).isEqualTo(1);
        assertThat(info.every.timeUnit).isEqualTo(TimeDuration.TimeUnit.MONTHS.getDescription());
        assertThat(info.offset).isNull();
        assertThat(info.lastDay).isFalse();
    }
}
