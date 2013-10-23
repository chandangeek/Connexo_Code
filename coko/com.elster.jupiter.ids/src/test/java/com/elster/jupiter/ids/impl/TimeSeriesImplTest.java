package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeSeriesImplTest extends EqualsContractTest {

    private static final long ID = 15L;
    private Vault vault = mock(Vault.class);
    private RecordSpec recordSpec = mock(RecordSpec.class);

    private final TimeSeriesImpl timeSeries = initTimeSeries();

    @Before
    public void setUp() {
        when(vault.isValidDateTime(any(Date.class))).thenReturn(true);
    }

    @After
    public void tearDown() {

    }

    private TimeSeriesImpl initTimeSeries() {
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, TimeZone.getTimeZone("Asia/Calcutta"));
        simulateSaved(series, ID);
        return series;
    }

    @Override
    protected Object getInstanceA() {
        return timeSeries;
    }

    @Override
    protected Object getInstanceEqualToA() {
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, TimeZone.getTimeZone("Asia/Calcutta"));
        simulateSaved(series, ID);
        return series;
    }

    @Override
    protected Object getInstanceNotEqualToA() {
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, TimeZone.getTimeZone("Asia/Calcutta"));
        simulateSaved(series, ID + 1);
        return series;
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
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, timeZone, 10, IntervalLengthUnit.MINUTE, 0);

        Date date = new DateTime(2012, 10, 10, 14, 5, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isFalse();

    }

    @Test
    public void testIsValidDateTimeInvalidMinuteForSubMinuteValue() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, timeZone, 10, IntervalLengthUnit.MINUTE, 0);

        Date date = new DateTime(2012, 10, 10, 14, 20, 0, 1, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isFalse();

    }

    @Test
    public void testIsValidDateTimeValidMinute() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, timeZone, 10, IntervalLengthUnit.MINUTE, 0);

        Date date = new DateTime(2012, 10, 10, 14, 20, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isTrue();
    }

    @Test
    public void testIsValidDateTimeValidDay() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, timeZone, 1, IntervalLengthUnit.DAY, 0);

        Date date = new DateTime(2012, 10, 12, 0, 0, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isTrue();
    }

    @Test
    public void testIsValidDateTimeInvalidDay() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, timeZone, 1, IntervalLengthUnit.DAY, 0);

        Date date = new DateTime(2012, 10, 10, 0, 0, 0, 1, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isFalse();
    }

    @Test
    public void testIsValidDateTimeValidDayWithOffset() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, timeZone, 1, IntervalLengthUnit.DAY, 6);

        Date date = new DateTime(2012, 10, 10, 6, 0, 0, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isTrue();
    }

    @Test
    public void testIsValidDateTimeInvalidDayWithOffset() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, timeZone, 1, IntervalLengthUnit.DAY, 6);

        Date date = new DateTime(2012, 10, 10, 12, 0, 0, 0, DateTimeZone.forTimeZone(timeZone)).toDate();

        assertThat(series.isValidDateTime(date)).isFalse();
    }

    @Test
    public void testTimeZones() {
        Date summer = new DateTime(2013, 7, 1, 12, 0, 0, 0, DateTimeZone.UTC).toDate();
        Date winter = new DateTime(2013, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC).toDate();
        for (String zone : TimeZone.getAvailableIDs()) {
            TimeZone timeZone = TimeZone.getTimeZone(zone);



            System.out.println(zone + " : " + offsetHours((long) timeZone.getRawOffset()) + " : " + offsetHours(timeZone.getOffset(winter.getTime())) + " : " + offsetHours(timeZone.getOffset(summer.getTime())));


        }

    }

    private double offsetHours(long offset) {
        return (offset / (1000d * 60 * 60));
    }


    private void simulateSaved(TimeSeriesImpl impl, long id) {
        field("id").ofType(Long.TYPE).in(impl).set(id);
    }
}
