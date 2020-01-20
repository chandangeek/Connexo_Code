package com.energyict.protocolimplv2.dlms.a2.profile;

import com.energyict.protocolimplv2.dlms.a2.profile.SelectiveEntryFilter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

public class SelectiveEntryFilterTest {

    private TimeZone timeZone = TimeZone.getTimeZone("GMT+2");

    @Test
    public void testReadLastHour15MinutesPast() {
        Calendar fromCalendar = Calendar.getInstance(timeZone);
        fromCalendar.set(Calendar.YEAR, 2019);
        fromCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        fromCalendar.set(Calendar.DAY_OF_MONTH, 10);
        fromCalendar.set(Calendar.HOUR_OF_DAY,7);
        fromCalendar.set(Calendar.MINUTE,0);
        fromCalendar.set(Calendar.SECOND,0);
        fromCalendar.set(Calendar.MILLISECOND,0);
        Calendar toCalendar = Calendar.getInstance(timeZone);
        toCalendar.set(Calendar.YEAR, 2019);
        toCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        toCalendar.set(Calendar.DAY_OF_MONTH, 10);
        toCalendar.set(Calendar.HOUR_OF_DAY,8);
        toCalendar.set(Calendar.MINUTE,15);
        toCalendar.set(Calendar.SECOND,0);
        toCalendar.set(Calendar.MILLISECOND,0);
        Calendar actualCalendar = Calendar.getInstance(timeZone);
        actualCalendar.set(Calendar.YEAR, 2019);
        actualCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        actualCalendar.set(Calendar.DAY_OF_MONTH, 10);
        actualCalendar.set(Calendar.HOUR_OF_DAY,8);
        actualCalendar.set(Calendar.MINUTE,15);
        actualCalendar.set(Calendar.SECOND,0);
        actualCalendar.set(Calendar.MILLISECOND,0);
        SelectiveEntryFilter filter = new SelectiveEntryFilter(fromCalendar, toCalendar, actualCalendar);
        Assert.assertEquals(1, filter.getFromIndex());
        Assert.assertEquals(1, filter.getToIndex());
    }

    @Test
    public void testReadLastHour45MinutesPast() {
        Calendar fromCalendar = Calendar.getInstance(timeZone);
        fromCalendar.set(Calendar.YEAR, 2019);
        fromCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        fromCalendar.set(Calendar.DAY_OF_MONTH, 10);
        fromCalendar.set(Calendar.HOUR_OF_DAY,7);
        fromCalendar.set(Calendar.MINUTE,0);
        fromCalendar.set(Calendar.SECOND,0);
        fromCalendar.set(Calendar.MILLISECOND,0);
        Calendar toCalendar = Calendar.getInstance(timeZone);
        toCalendar.set(Calendar.YEAR, 2019);
        toCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        toCalendar.set(Calendar.DAY_OF_MONTH, 10);
        toCalendar.set(Calendar.HOUR_OF_DAY,8);
        toCalendar.set(Calendar.MINUTE,45);
        toCalendar.set(Calendar.SECOND,0);
        toCalendar.set(Calendar.MILLISECOND,0);
        Calendar actualCalendar = Calendar.getInstance(timeZone);
        actualCalendar.set(Calendar.YEAR, 2019);
        actualCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        actualCalendar.set(Calendar.DAY_OF_MONTH, 10);
        actualCalendar.set(Calendar.HOUR_OF_DAY,8);
        actualCalendar.set(Calendar.MINUTE,15);
        actualCalendar.set(Calendar.SECOND,0);
        actualCalendar.set(Calendar.MILLISECOND,0);
        SelectiveEntryFilter filter = new SelectiveEntryFilter(fromCalendar, toCalendar, actualCalendar);
        Assert.assertEquals(1, filter.getFromIndex());
        Assert.assertEquals(1, filter.getToIndex());
    }

    @Test
    public void testReadNovemberFirstTillNovember15() {
        Calendar fromCalendar = Calendar.getInstance(timeZone);
        fromCalendar.set(Calendar.YEAR, 2019);
        fromCalendar.set(Calendar.MONTH, Calendar.NOVEMBER);
        fromCalendar.set(Calendar.DAY_OF_MONTH, 1);
        fromCalendar.set(Calendar.HOUR_OF_DAY,0);
        fromCalendar.set(Calendar.MINUTE,0);
        fromCalendar.set(Calendar.SECOND,0);
        fromCalendar.set(Calendar.MILLISECOND,0);
        Calendar toCalendar = Calendar.getInstance(timeZone);
        toCalendar.set(Calendar.YEAR, 2019);
        toCalendar.set(Calendar.MONTH, Calendar.NOVEMBER);
        toCalendar.set(Calendar.DAY_OF_MONTH, 15);
        toCalendar.set(Calendar.HOUR_OF_DAY,0);
        toCalendar.set(Calendar.MINUTE,45);
        toCalendar.set(Calendar.SECOND,0);
        toCalendar.set(Calendar.MILLISECOND,0);
        Calendar actualCalendar = Calendar.getInstance(timeZone);
        actualCalendar.set(Calendar.YEAR, 2019);
        actualCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        actualCalendar.set(Calendar.DAY_OF_MONTH, 10);
        actualCalendar.set(Calendar.HOUR_OF_DAY,8);
        actualCalendar.set(Calendar.MINUTE,45);
        actualCalendar.set(Calendar.SECOND,0);
        actualCalendar.set(Calendar.MILLISECOND,0);
        SelectiveEntryFilter filter = new SelectiveEntryFilter(fromCalendar, toCalendar, actualCalendar);
        Assert.assertEquals(609, filter.getFromIndex());
        Assert.assertEquals(944, filter.getToIndex());
    }

}
