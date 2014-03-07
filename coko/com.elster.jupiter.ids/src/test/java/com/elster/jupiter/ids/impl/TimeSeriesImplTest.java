package com.elster.jupiter.ids.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;

public class TimeSeriesImplTest extends EqualsContractTest {

    private static final long ID = 15L;
    private VaultImpl vault = mock(VaultImpl.class);
    private RecordSpec recordSpec = mock(RecordSpec.class);
    private DataModel dataModel = mock(DataModel.class);

    private final TimeSeriesImpl timeSeries = initTimeSeries();

    @Before
    public void setUp() {
        when(vault.isValidDateTime(any(Date.class))).thenReturn(true);
    }

    @After
    public void tearDown() {

    }

    private TimeSeriesImpl initTimeSeries() {
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, TimeZone.getTimeZone("Asia/Calcutta"));
        simulateSaved(series, ID);
        return series;
    }

    @Override
    protected Object getInstanceA() {
        return timeSeries;
    }

    @Override
    protected Object getInstanceEqualToA() {
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, TimeZone.getTimeZone("Asia/Calcutta"));
        simulateSaved(series, ID);
        return series;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, TimeZone.getTimeZone("Asia/Calcutta"));
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
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, timeZone, 10, IntervalLengthUnit.MINUTE, 0);

        Date date = new DateTime(2012, 10, 10, 14, 5, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isFalse();

    }

    @Test
    public void testIsValidDateTimeInvalidMinuteForSubMinuteValue() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, timeZone, 10, IntervalLengthUnit.MINUTE, 0);
        Date date = new DateTime(2012, 10, 10, 14, 20, 0, 1, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isFalse();

    }

    @Test
    public void testIsValidDateTimeValidMinute() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, timeZone, 10, IntervalLengthUnit.MINUTE, 0);

        Date date = new DateTime(2012, 10, 10, 14, 20, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isTrue();
    }

    @Test
    public void testIsValidDateTimeValidDay() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, timeZone, 1, IntervalLengthUnit.DAY, 0);

        Date date = new DateTime(2012, 10, 12, 0, 0, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isTrue();
    }

    @Test
    public void testIsValidDateTimeInvalidDay() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, timeZone, 1, IntervalLengthUnit.DAY, 0);

        Date date = new DateTime(2012, 10, 10, 0, 0, 0, 1, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isFalse();
    }

    @Test
    public void testIsValidDateTimeValidDayWithOffset() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, timeZone, 1, IntervalLengthUnit.DAY, 6);

        Date date = new DateTime(2012, 10, 10, 6, 0, 0, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isTrue();
    }

    @Test
    public void testIsValidDateTimeInvalidDayWithOffset() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(dataModel).init(vault, recordSpec, timeZone, 1, IntervalLengthUnit.DAY, 6);

        Date date = new DateTime(2012, 10, 10, 12, 0, 0, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isFalse();
    }

    private void simulateSaved(TimeSeriesImpl impl, long id) {
        field("id").ofType(Long.TYPE).in(impl).set(id);
    }
}
