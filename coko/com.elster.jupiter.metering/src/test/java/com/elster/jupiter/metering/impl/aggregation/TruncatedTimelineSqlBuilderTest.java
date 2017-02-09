/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.DayMonthTime;
import com.elster.jupiter.util.units.Dimension;

import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TruncatedTimelineSqlBuilder} and related factory {@link TruncatedTimelineSqlBuilderFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TruncatedTimelineSqlBuilderTest {

    @Mock
    private ServerMeteringService meteringService;

    @Test
    public void test15MinutesEnergyToDayLevel() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        TruncatedTimelineSqlBuilder builder = TruncatedTimelineSqlBuilderFactory.truncate(kWh15Mins())
                .to(IntervalLength.DAY1)
                .using(sqlBuilder, this.meteringService);

        // Business method
        builder.append("LOCALDATE");

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualToIgnoringCase("TRUNC(LOCALDATE, 'DDD')");
    }

    @Test
    public void test15MinutesGasToDayLevel() {
        GasDayOptions gasDayOptions = mock(GasDayOptions.class);
        when(gasDayOptions.getYearStart()).thenReturn(DayMonthTime.from(MonthDay.of(Month.OCTOBER, 1), LocalTime.of(17, 0)));
        when(this.meteringService.getGasDayOptions()).thenReturn(Optional.of(gasDayOptions));
        SqlBuilder sqlBuilder = new SqlBuilder();
        TruncatedTimelineSqlBuilder builder = TruncatedTimelineSqlBuilderFactory.truncate(gaskWh15Mins())
                .to(IntervalLength.DAY1)
                .using(sqlBuilder, this.meteringService);

        // Business method
        builder.append("LOCALDATE");

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualToIgnoringCase("(TRUNC(LOCALDATE - INTERVAL '17' HOUR, 'DDD') + INTERVAL '17' HOUR)");
    }

    @Test
    public void test15MinutesGasDoesNotNPEWhenGasDayStartNotConfigured() {
        when(this.meteringService.getGasDayOptions()).thenReturn(Optional.empty());
        SqlBuilder sqlBuilder = new SqlBuilder();
        TruncatedTimelineSqlBuilder builder = TruncatedTimelineSqlBuilderFactory.truncate(gaskWh15Mins())
                .to(IntervalLength.DAY1)
                .using(sqlBuilder, this.meteringService);

        // Business method
        builder.append("LOCALDATE");

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualToIgnoringCase("(TRUNC(LOCALDATE - INTERVAL '0' HOUR, 'DDD') + INTERVAL '0' HOUR)");
    }

    @Test
    public void test15MinutesGasToYearLevel() {
        GasDayOptions gasDayOptions = mock(GasDayOptions.class);
        when(gasDayOptions.getYearStart()).thenReturn(DayMonthTime.from(MonthDay.of(Month.MAY, 1), LocalTime.of(17, 0)));
        when(this.meteringService.getGasDayOptions()).thenReturn(Optional.of(gasDayOptions));
        SqlBuilder sqlBuilder = new SqlBuilder();
        TruncatedTimelineSqlBuilder builder = TruncatedTimelineSqlBuilderFactory.truncate(gaskWh15Mins())
                .to(IntervalLength.YEAR1)
                .using(sqlBuilder, this.meteringService);

        // Business method
        builder.append("LOCALDATE");

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualToIgnoringCase("((TRUNC((LOCALDATE - INTERVAL '17' HOUR) - INTERVAL '5' MONTH, 'IYYY') + INTERVAL '17' HOUR) + INTERVAL '4' MONTH)");
    }

    private static VirtualReadingType kWh15Mins() {
        return VirtualReadingType.from(IntervalLength.MINUTE15, Dimension.ENERGY, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
    }

    private static VirtualReadingType gaskWh15Mins() {
        return VirtualReadingType.from(IntervalLength.MINUTE15, Dimension.ENERGY, Accumulation.DELTADELTA, Commodity.NATURALGAS);
    }

}