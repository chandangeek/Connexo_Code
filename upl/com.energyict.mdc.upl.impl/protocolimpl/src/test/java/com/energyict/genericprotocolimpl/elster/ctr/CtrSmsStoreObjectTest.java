package com.energyict.genericprotocolimpl.elster.ctr;


import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.profile.DummyChannel;
import com.energyict.genericprotocolimpl.elster.ctr.profile.DummyRtu;
import com.energyict.protocol.*;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 15/11/11
 * Time: 14:43
 */
public class CtrSmsStoreObjectTest {

    public static final int CHANNEL_BACK_LOG = 20;
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("CET");
    public static final int PROFILE_INTERVAL = 3600;

    @Test
    public void testNullLastReading() {
        Calendar channelBackLogDate = getChannelBackLogDate(0);
        ProfileData weekProfileData = createWeekProfileData(Calendar.getInstance());
        Date newLastReading = doDummyStoreAndGetLastReading(weekProfileData);
        assertNotNull(newLastReading);
        assertNotSame(channelBackLogDate.getTime(), newLastReading);
        assertEquals(getLastReading(weekProfileData), newLastReading);
    }

    @Test
    public void testGapAfterBacklogDate() {
        Calendar lastReading = getChannelBackLogDate(2);
        Date newLastReading = doDummyStoreAndGetLastReading(createWeekProfileData(Calendar.getInstance()), lastReading);
        assertNotNull(newLastReading);
        assertEquals(lastReading.getTime(), newLastReading);
    }

    @Test
    public void testGapBeforeBacklogDate() {
        Calendar lastReading = getChannelBackLogDate(-2);
        Calendar channelBackLogDate = getChannelBackLogDate(0);
        Date newLastReading = doDummyStoreAndGetLastReading(createWeekProfileData(Calendar.getInstance()), lastReading);
        assertNotNull(newLastReading);
        assertEquals(channelBackLogDate.getTime(), newLastReading);
    }

    @Test
    public void testNoGapBeforeBacklogDateBeforeBackLog() {
        ProfileData weekProfile = createWeekProfileData(getChannelBackLogDate(-5));
        Date lastReading = getEndTimeBeforeThisProfileData(weekProfile);
        Date newLastReading = doDummyStoreAndGetLastReading(weekProfile, lastReading);
        assertNotNull(newLastReading);
        assertEquals(getLastReading(weekProfile), newLastReading);
    }

    @Test
    public void testNoGapBeforeBacklogDateAfterBackLog() {
        ProfileData weekProfile = createWeekProfileData(getChannelBackLogDate(5));
        Date lastReading = getEndTimeBeforeThisProfileData(weekProfile);
        Date newLastReading = doDummyStoreAndGetLastReading(weekProfile, lastReading);
        assertNotNull(newLastReading);
        assertEquals(getLastReading(weekProfile), newLastReading);
    }

    /**
     * Get the end date of the most recent interval for a given profile data object, or null if there was no profile data
     *
     * @param profileData The profile data to get the last reading from.
     * @return The end date of the most recent interval or null if not found
     */
    private Date getLastReading(ProfileData profileData) {
        profileData.sort();
        List<IntervalData> intervalDatas = profileData.getIntervalDatas();
        if ((intervalDatas != null) && !intervalDatas.isEmpty()) {
            return intervalDatas.get(intervalDatas.size() - 1).getEndTime();
        }
        return null;
    }

    /**
     * Get the date of the interval that should append perfectly to the beginning of this profile
     *
     * @param profileData The profile data to get the previous reading from.
     * @return The date of the interval that appends perfectly to the beginning of the profile or null if profile is empty
     */
    private Date getEndTimeBeforeThisProfileData(ProfileData profileData) {
        profileData.sort();
        List<IntervalData> intervalDatas = profileData.getIntervalDatas();
        if ((intervalDatas != null) && !intervalDatas.isEmpty()) {
            Date endTime = intervalDatas.get(0).getEndTime();
            return new Date(endTime.getTime() - (PROFILE_INTERVAL * 1000));
        }
        return null;
    }

    /**
     * Creates a dummy RTU and Channel with the given parameters, stores the data and get the last reading for the channel
     *
     * @param profileData The profile data to store
     * @param lastReading The last reading of the channel before storing the data
     * @return The last reading from the dummy channel after storing the data
     */
    private Date doDummyStoreAndGetLastReading(ProfileData profileData, Calendar lastReading) {
        DummyChannel dummyChannel = new DummyChannel(0, PROFILE_INTERVAL, lastReading == null ? null : (Calendar) lastReading.clone());
        dummyChannel.setRtu(new DummyRtu(TIME_ZONE, dummyChannel));

        CtrSmsStoreObject storeObject = new CtrSmsStoreObject(CHANNEL_BACK_LOG);
        storeObject.add(dummyChannel, profileData);
        try {
            storeObject.doExecute();
        } catch (BusinessException e) {
            fail(e.getMessage());
        } catch (SQLException e) {
            fail(e.getMessage());
        }
        return dummyChannel.getLastReading();
    }

    /**
     * Creates a dummy RTU and Channel with the given parameters, stores the data and get the last reading for the channel
     *
     * @param profileData The profile data to store
     * @param lastReading The last reading of the channel before storing the data
     * @return The last reading from the dummy channel after storing the data
     */
    private Date doDummyStoreAndGetLastReading(ProfileData profileData, Date lastReading) {
        if (lastReading == null) {
            return doDummyStoreAndGetLastReading(profileData);
        } else {
            Calendar lastReadingCalendar = Calendar.getInstance();
            lastReadingCalendar.setTime(lastReading);
            return doDummyStoreAndGetLastReading(profileData, lastReadingCalendar);
        }
    }

    /**
     * Creates a dummy RTU and Channel withouth last reading (null),
     * stores the data and get the last reading for the channel
     *
     * @param profileData The profile data to store
     * @return The last reading from the dummy channel after storing the data
     */
    private Date doDummyStoreAndGetLastReading(ProfileData profileData) {
        return doDummyStoreAndGetLastReading(profileData, (Calendar) null);
    }

    /**
     * Create a calendar, using the channelBackLog value and a given offset in days
     *
     * @param offsetInDays the offset in days, compared to the channelBackLogDate
     * @return the new calendar
     */
    private Calendar getChannelBackLogDate(int offsetInDays) {
        Calendar channelBackLogDate = Calendar.getInstance();
        channelBackLogDate.set(Calendar.MILLISECOND, 0);
        channelBackLogDate.set(Calendar.SECOND, 0);
        channelBackLogDate.set(Calendar.MINUTE, 0);
        channelBackLogDate.set(Calendar.HOUR_OF_DAY, 0);
        channelBackLogDate.add(Calendar.DAY_OF_YEAR, -CHANNEL_BACK_LOG);
        channelBackLogDate.add(Calendar.DAY_OF_YEAR, offsetInDays);
        return channelBackLogDate;
    }

    /**
     * Create some dummy profile data for a week, with the given end date as last interval
     *
     * @param endDate The date of the last interval of the generated week profile
     * @return The generated profile data
     */
    private ProfileData createWeekProfileData(Calendar endDate) {
        ProfileData profileData = new ProfileData();
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        channelInfos.add(new ChannelInfo(0, "DummyChannel", Unit.get("kWh")));

        ArrayList<IntervalData> intervalDatas = new ArrayList<IntervalData>();

        Calendar endCalendar = (Calendar) endDate.clone();
        endCalendar.set(Calendar.MILLISECOND, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MINUTE, 0);

        long startDate = endCalendar.getTimeInMillis();
        for (int i = 0; i < (24 * 7); i++) {
            intervalDatas.add(new IntervalData(new Date(startDate - (i * 3600 * 1000))));
        }

        profileData.setChannelInfos(channelInfos);
        profileData.setMeterEvents(Collections.<MeterEvent>emptyList());
        profileData.setIntervalDatas(intervalDatas);
        profileData.setLoadProfileId(0);


        return profileData;
    }

}
