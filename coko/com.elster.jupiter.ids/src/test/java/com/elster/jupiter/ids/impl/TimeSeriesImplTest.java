/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeSeriesImplTest extends EqualsContractTest {

    private static final long ID = 15L;
    private IVault vault = mock(IVault.class);
    private RecordSpec recordSpec = mock(RecordSpec.class);
    private DataModel dataModel = mock(DataModel.class);
    private IdsService idsService = mock(IdsService.class);

    private final TimeSeriesImpl timeSeries = initTimeSeries();

    @Before
    public void setUp() {
        when(vault.isValidInstant(any())).thenReturn(true);
    }

    @After
    public void tearDown() {

    }

    private TimeSeriesImpl initTimeSeries() {
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, ZoneId.of("Asia/Calcutta"));
        simulateSaved(series, ID);
        return series;
    }

    @Override
    protected Object getInstanceA() {
        return timeSeries;
    }

    @Override
    protected Object getInstanceEqualToA() {
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, ZoneId.of("Asia/Calcutta"));
        simulateSaved(series, ID);
        return series;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, ZoneId.of("Asia/Calcutta"));
        simulateSaved(series, ID + 1);
        return ImmutableList.of(series);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void testIsValidDateTimeInvalidMinute() {
        ZoneId timeZone = ZoneId.of("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, timeZone, Duration.ofMinutes(10), 0);
        Instant instant = ZonedDateTime.of(2012, 10, 10, 14, 5, 0, 0, timeZone).toInstant();
        assertThat(series.isValidInstant(instant)).isFalse();
        assertThat(series.validInstantOnOrAfter(instant)).isEqualTo(instant.plusSeconds(60*5));
    }

    @Test
    public void testIsValidDateTimeInvalidMinuteForSubMinuteValue() {
        ZoneId timeZone = ZoneId.of("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, timeZone, Duration.ofMinutes(10), 0);
        Instant instant = ZonedDateTime.of(2012, 10, 10, 14, 20, 1, 0, timeZone).toInstant();
        assertThat(series.isValidInstant(instant)).isFalse();
        assertThat(series.validInstantOnOrAfter(instant)).isEqualTo(instant.plusSeconds(59 + 9 * 60));
    }

    @Test
    public void testIsValidDateTimeValidMinute() {
    	ZoneId timeZone = ZoneId.of("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, timeZone, Duration.ofMinutes(10), 0);
        Instant instant = ZonedDateTime.of(2012, 10, 10, 14, 20, 0, 0, timeZone).toInstant();
        assertThat(series.isValidInstant(instant)).isTrue();
        assertThat(series.validInstantOnOrAfter(instant)).isEqualTo(instant);
    }

    @Test
    public void testIsValidDateTimeValidDay() {
    	ZoneId timeZone = ZoneId.of("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, timeZone, Period.ofDays(1), 0);
        Instant instant = ZonedDateTime.of(2012, 10, 12, 0, 0, 0, 0, timeZone).toInstant();
        assertThat(series.isValidInstant(instant)).isTrue();
        assertThat(series.validInstantOnOrAfter(instant)).isEqualTo(instant);
    }

    @Test
    public void testIsValidDateTimeInvalidDay() {
    	ZoneId timeZone = ZoneId.of("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, timeZone, Period.ofDays(1), 0);
        Instant instant = ZonedDateTime.of(2012, 10, 10, 0, 0, 1, 0, timeZone).toInstant();
        assertThat(series.isValidInstant(instant)).isFalse();
        assertThat(series.validInstantOnOrAfter(instant))
        	.isEqualTo(ZonedDateTime.of(2012, 10, 11, 0, 0, 0, 0, timeZone).toInstant());
    }

    @Test
    public void testIsValidDateTimeValidDayWithOffset() {
    	ZoneId timeZone = ZoneId.of("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, timeZone, Period.ofDays(1), 6);
        Instant instant = ZonedDateTime.of(2012, 10, 10, 6, 0, 0, 0, timeZone).toInstant();
        assertThat(series.isValidInstant(instant)).isTrue();
        assertThat(series.validInstantOnOrAfter(instant)).isEqualTo(instant);
        Instant instant2 = ZonedDateTime.of(2012 , 10, 12, 6, 0, 0, 0, timeZone).toInstant();
        assertThat(series.toList(Range.openClosed(instant, instant2))).hasSize(2);
    }

    @Test
    public void testIsValidDateTimeInvalidDayWithLaterOffset() {
    	ZoneId timeZone = ZoneId.of("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, timeZone, Period.ofDays(1), 6);
        Instant instant = ZonedDateTime.of(2012 , 10, 10, 12, 0, 0, 0, timeZone).toInstant();
        assertThat(series.isValidInstant(instant)).isFalse();
        assertThat(series.validInstantOnOrAfter(instant))
    		.isEqualTo(ZonedDateTime.of(2012, 10, 11, 6, 0, 0, 0, timeZone).toInstant());
    }

    @Test
    public void testIsValidDateTimeInvalidDayWithEarlierOffset() {
    	ZoneId timeZone = ZoneId.of("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel,idsService).init(vault, recordSpec, timeZone, Period.ofDays(1), 6);
        Instant instant = ZonedDateTime.of(2012 , 10, 10, 3, 0, 0, 0, timeZone).toInstant();
        assertThat(series.isValidInstant(instant)).isFalse();
        assertThat(series.validInstantOnOrAfter(instant))
    		.isEqualTo(ZonedDateTime.of(2012, 10, 10, 6, 0, 0, 0, timeZone).toInstant());
        Instant instant2 = ZonedDateTime.of(2012 , 10, 11, 9, 0, 0, 0, timeZone).toInstant();
        assertThat(series.toList(Range.closed(instant, instant2))).hasSize(2);
    }
    
    private void simulateSaved(TimeSeriesImpl impl, long id) {
        field("id").ofType(Long.TYPE).in(impl).set(id);
    }
}
