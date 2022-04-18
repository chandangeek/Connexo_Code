package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;
import junit.framework.TestCase;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RescheduleToNextComWindowTest extends TestCase {

    @Mock
    JobExecution scheduledJob;

    @Mock
    private FirmwareService firmwareService;

    @Mock
    FirmwareCampaign firmwareCampaign;

    @Mock
    RescheduleToNextComWindow reschedule;

    @Before
    public void setup(){
       ComWindow comWindow = new ComWindow( PartialTime.fromSeconds(DateTimeConstants.SECONDS_PER_HOUR * 15),
                                            PartialTime.fromSeconds(DateTimeConstants.SECONDS_PER_HOUR * 20));


       when(firmwareCampaign.getComWindow()).thenReturn(comWindow);

        when(reschedule.getComWindowAppliedStartDate(any(), any())).thenCallRealMethod();
    }


    @Test
    public void testRescheduleWithStartingPointInThePast(){
        Instant now = Instant.ofEpochSecond(1641042000);                         // January 1, 2022 13:00:00
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(now.toEpochMilli());

        when(reschedule.getNow()).thenReturn(now);
        when(reschedule.getUtcCalendar()).thenReturn(utcCalendar);


        Instant startPoint = Instant.ofEpochSecond(1641031200); // January 1, 2022 10:00:00

        Instant expected = Instant.ofEpochSecond(1641049200);        // January 1, 2022 13:00:00

        Instant scheduledTime = reschedule.getComWindowAppliedStartDate(firmwareCampaign, startPoint);

        assertEquals(expected, scheduledTime);
    }


    @Test
    public void testRescheduleWithStartingPointInTheFuture(){
        Instant now = Instant.ofEpochSecond(1641074400);                         // January 1, 2022 22:00:00
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(now.toEpochMilli());

        when(reschedule.getNow()).thenReturn(now);
        when(reschedule.getUtcCalendar()).thenReturn(utcCalendar);

        Instant startPoint = Instant.ofEpochSecond(1641031200);      // January 1, 2022 10:00:00

        Instant expected = Instant.ofEpochSecond(1641135600);        // January 2, 2022 15:00:00

        Instant scheduledTime = reschedule.getComWindowAppliedStartDate(firmwareCampaign, startPoint);

        assertEquals(expected, scheduledTime);
    }



    @Test
    public void testRescheduleWithStartingPointInWindowButInPast(){
        Instant now = Instant.ofEpochSecond(1641060000);                         // January 1, 2022 18:00:00
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(now.toEpochMilli());

        when(reschedule.getNow()).thenReturn(now);
        when(reschedule.getUtcCalendar()).thenReturn(utcCalendar);

        Instant startPoint = Instant.ofEpochSecond(1641056400);      // January 1, 2022 17:00:00

        Instant expected = Instant.ofEpochSecond(1641135600);        // January 2, 2022 15:00:00

        Instant scheduledTime = reschedule.getComWindowAppliedStartDate(firmwareCampaign, startPoint);

        assertEquals(expected, scheduledTime);
    }


    @Test
    public void testRescheduleWithStartingPointInWindowButInPastDays(){
        Instant now = Instant.ofEpochSecond(1641060000);                         // January 1, 2022 18:00:00
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(now.toEpochMilli());

        when(reschedule.getNow()).thenReturn(now);
        when(reschedule.getUtcCalendar()).thenReturn(utcCalendar);

        Instant startPoint = Instant.ofEpochSecond(1609516800);      // January 1, 2021 16:00:00

        Instant expected = Instant.ofEpochSecond(1641135600);        // January 2, 2022 15:00:00

        Instant scheduledTime = reschedule.getComWindowAppliedStartDate(firmwareCampaign, startPoint);

        assertEquals(expected, scheduledTime);
    }

    @Test
    public void testRescheduleWithStartingPointOutsideWindowButInPastDays(){
        Instant now = Instant.ofEpochSecond(1641060000);                         // January 1, 2022 18:00:00
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(now.toEpochMilli());

        when(reschedule.getNow()).thenReturn(now);
        when(reschedule.getUtcCalendar()).thenReturn(utcCalendar);

        Instant startPoint = Instant.ofEpochSecond(1609491600);      //  January 1, 2021 9:00:00

        Instant expected = Instant.ofEpochSecond(1641135600);        // January 2, 2022 15:00:00

        Instant scheduledTime = reschedule.getComWindowAppliedStartDate(firmwareCampaign, startPoint);

        assertEquals(expected, scheduledTime);
    }

    @Test
    public void testRescheduleWithStartingPointInWindowButInFuture(){
        Instant now = Instant.ofEpochSecond(1641052800);                         // January 1, 2022 16:00:00
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(now.toEpochMilli());

        when(reschedule.getNow()).thenReturn(now);
        when(reschedule.getUtcCalendar()).thenReturn(utcCalendar);

        Instant startPoint = Instant.ofEpochSecond(1641056400);      // January 1, 2022 17:00:00

        Instant expected = startPoint;

        Instant scheduledTime = reschedule.getComWindowAppliedStartDate(firmwareCampaign, startPoint);

        assertEquals(expected, scheduledTime);
    }
}