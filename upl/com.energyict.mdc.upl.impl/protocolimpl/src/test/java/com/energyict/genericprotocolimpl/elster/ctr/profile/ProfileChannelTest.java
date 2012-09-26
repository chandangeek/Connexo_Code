package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.cbo.TimeDuration;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 25-jan-2011
 * Time: 11:21:22
 */
@RunWith(PowerMockRunner.class)
public class ProfileChannelTest {

    private static final Calendar TO = ProtocolTools.createCalendar(2011, 1, 25, 15, 0, 0, 0);

    public static final int DAILY_INTERVAL = 3600 * 24;
    public static final int HOURLY_INTERVAL = 3600;
    public static final int DAILY_VM_INDEX = 5;
    public static final int HOURY_VM_INDEX = 4;

    @Mock
    private Rtu rtu;
    @Mock
    private Channel dailyChannel;
    @Mock
    private Channel hourlyChannel;

    @Before
    public void initializeMocksAndFactories() {
        Calendar lastReading = ProtocolTools.createCalendar(2010, 12, 26, 6, 0, 0, 0);
        when(dailyChannel.getLastReading()).thenReturn(lastReading.getTime());
        when(dailyChannel.getRtu()).thenReturn(this.rtu);
        when(dailyChannel.getInterval()).thenReturn(new TimeDuration(DAILY_INTERVAL));
        when(dailyChannel.getIntervalInSeconds()).thenReturn(DAILY_INTERVAL);
        when(dailyChannel.getLoadProfileIndex()).thenReturn(DAILY_VM_INDEX);

        lastReading = ProtocolTools.createCalendar(2011, 1, 23, 6, 0, 0, 0);
        when(hourlyChannel.getLastReading()).thenReturn(lastReading.getTime());
        when(hourlyChannel.getRtu()).thenReturn(this.rtu);
        when(hourlyChannel.getInterval()).thenReturn(new TimeDuration(HOURLY_INTERVAL));
        when(hourlyChannel.getIntervalInSeconds()).thenReturn(HOURLY_INTERVAL);
        when(hourlyChannel.getLoadProfileIndex()).thenReturn(HOURY_VM_INDEX);

        when(rtu.getDeviceTimeZone()).thenReturn(TimeZone.getDefault());
    }

    @Test
    public void testDailyValues() throws Exception {
        ProfileChannel pc = new ProfileChannel(getGPRSRequestFactory(), dailyChannel, TO);
        ProfileData pd = pc.getProfileData();

        assertNotNull(pd);
        pd.sort();
        assertEquals(1, pd.getNumberOfChannels());
        assertEquals(0, pd.getNumberOfEvents());

        List<IntervalData> intervals = pd.getIntervalDatas();

        assertNotNull(intervals);
        assertEquals(30, intervals.size());
        assertEquals(ProtocolTools.createCalendar(2010, 12, 27, 6, 0, 0, 0).getTimeInMillis(), intervals.get(0).getEndTime().getTime());
        assertEquals(ProtocolTools.createCalendar(2011, 1, 25, 6, 0, 0, 0).getTimeInMillis(), intervals.get(intervals.size() - 1).getEndTime().getTime());
        for (IntervalData interval : intervals) {
            assertNotNull(interval);
            assertNotNull(interval.getIntervalValues());
            assertEquals(1, interval.getIntervalValues().size());
        }
        assertEquals(0, intervals.get(0).get(0).intValue());
        assertEquals(344, intervals.get(intervals.size() - 5).get(0).intValue());
        assertEquals(367, intervals.get(intervals.size() - 4).get(0).intValue());
        assertEquals(13, intervals.get(intervals.size() - 3).get(0).intValue());
        assertEquals(12, intervals.get(intervals.size() - 2).get(0).intValue());
        assertEquals(438, intervals.get(intervals.size() - 1).get(0).intValue());

    }

    @Test
    public void testHourlyValues() throws Exception {
        ProfileChannel pc = new ProfileChannel(getGPRSRequestFactory(), hourlyChannel, TO);
        ProfileData pd = pc.getProfileData();

        assertNotNull(pd);
        pd.sort();
        assertEquals(1, pd.getNumberOfChannels());
        assertEquals(0, pd.getNumberOfEvents());

        List<IntervalData> intervals = pd.getIntervalDatas();
        assertNotNull(intervals);
        assertEquals(48, intervals.size());

        assertEquals(ProtocolTools.createCalendar(2011, 1, 23, 7, 0, 0, 0).getTimeInMillis(), intervals.get(0).getEndTime().getTime());
        assertEquals(ProtocolTools.createCalendar(2011, 1, 25, 6, 0, 0, 0).getTimeInMillis(), intervals.get(intervals.size() - 1).getEndTime().getTime());
        for (IntervalData interval : intervals) {
            assertNotNull(interval);
            assertNotNull(interval.getIntervalValues());
            assertEquals(1, interval.getIntervalValues().size());
        }
        assertEquals(270, intervals.get(0).get(0).intValue());
        assertEquals(272, intervals.get(intervals.size() - 6).get(0).intValue());
        assertEquals(271, intervals.get(intervals.size() - 5).get(0).intValue());
        assertEquals(271, intervals.get(intervals.size() - 4).get(0).intValue());
        assertEquals(270, intervals.get(intervals.size() - 3).get(0).intValue());
        assertEquals(270, intervals.get(intervals.size() - 2).get(0).intValue());
        assertEquals(270, intervals.get(intervals.size() - 1).get(0).intValue());

    }


    private GprsRequestFactory getGPRSRequestFactory() {
        return new DummyRequestFactory();
    }

}
