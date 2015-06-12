package com.energyict.protocolimpl.dlms.g3.profile;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.LogicalName;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.common.DLMSProfileHelper;
import com.energyict.protocolimpl.dlms.common.ProfileCache;
import com.energyict.protocolimpl.dlms.g3.G3Cache;
import com.energyict.protocolimpl.dlms.g3.G3ProfileType;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 22/02/12
 * Time: 13:39
 */
public class G3Profile extends DLMSProfileHelper {

    private static final int SECONDS_IN_DAY = 60 * 60 * 24;
    private static final int SECONDS_IN_MONTH = SECONDS_IN_DAY * 31;

    private G3ProfileType profileType;

    public G3Profile(DlmsSession session, G3ProfileType profileType, G3Cache cache, String serialNumber) {
        super.setSession(session);
        super.setObisCode(profileType.getObisCode());
        super.setCache(cache);
        super.setSerialNumber(serialNumber);
        this.profileType = profileType;
    }

    public G3Profile(G3ProfileType profileType, ProfileCache profileCache, Logger logger, TimeZone timeZone, CosemObjectFactory cosemObjectFactory, String serialNumber) {
        super.setCosemObjectFactory(cosemObjectFactory);
        super.setLogger(logger);
        super.setTimeZone(timeZone);
        super.setObisCode(profileType.getObisCode());
        super.setCache(profileCache);
        super.setSerialNumber(serialNumber);
        this.profileType = profileType;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (isDaily() || isMonthly()) {
            return super.getNumberOfChannels();
        } else {
            switch (profileType) {
                case IMPORT_ACTIVE_POWER_PROFILE:
                    return 1;
                case EXPORT_ACTIVE_POWER_PROFILE:
                    return 1;
                default:
                    return -1;
            }
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (isDaily()) {
            return SECONDS_IN_DAY;
        } else if (isMonthly()) {
            return SECONDS_IN_MONTH;
        } else {
            return super.getProfileInterval();
        }
    }


    protected void setClockAndStatusPosition() {
        if (isMonthly() || isDaily()) {
            setStatusMask(0);        //No status, only a clock timestamp at the first captured_object
            setClockMask(1);
        }
    }

    @Override
    public void readChannelInfosFromDevice() throws IOException {
        if (isDaily() || isMonthly()) {
            super.readChannelInfosFromDevice();   //These profiles contain captured_objects
        } else {
            setChannelInfos(new ArrayList<ChannelInfo>());
            switch (profileType) {
                case IMPORT_ACTIVE_POWER_PROFILE:
                    ChannelInfo channelInfo = new ChannelInfo(0, "1.1.1.8.0.255", Unit.get(BaseUnit.WATT), getSerialNumber());
                    addChannelInfo(channelInfo);
                    break;
                case EXPORT_ACTIVE_POWER_PROFILE:
                    addChannelInfo(new ChannelInfo(0, "1.1.2.8.0.255", Unit.get(BaseUnit.WATT), getSerialNumber()));
                    break;
            }
        }
    }

    private CapturedObject createCapturedObject(String obisCode) {
        return new CapturedObject(DLMSClassId.REGISTER.getClassId(), new LogicalName(new OctetString(ObisCode.fromString(obisCode).getLN())), 2, 0);
    }

    public boolean isMonthly() {
        return profileType == G3ProfileType.MONTHLY_PROFILE;
    }

    public boolean isDaily() {
        return profileType == G3ProfileType.DAILY_PROFILE;
    }

    public ProfileData getProfileData(Date from, Date to) throws IOException {
        getLogger().fine("Reading out profile data from [" + from + "] to [" + to + "] for load profile [" + getObisCode() + "]");
        if (isDaily() || isMonthly()) {
            //This uses the common DLMS way for parsing intervals
            return super.getProfileData(from, to);
        } else {
            //The basic profile uses a compact array
            ProfileData profileData = new ProfileData();
            profileData.setChannelInfos(buildChannelInfos());

            byte[] profileRawData = getProfileGeneric().getBufferData(getCalendar(from), getCalendar(to));
            final List<IntervalData> intervalDatas = parseCompactArray(profileRawData);
            profileData.setIntervalDatas(intervalDatas);
            profileData.sort();

            return profileData;
        }
    }

    public List<IntervalData> parseCompactArray(byte[] profileRawData) throws IOException {
        return parseCompactArray(profileRawData, false);
    }

    /**
     * @param inverse Indicating if the entries in the compact array are ordered LIFO (true) or FIFO (false).
     *                Default is false.
     */
    public List<IntervalData> parseCompactArray(byte[] profileRawData, boolean inverse) throws IOException {
        final G3CompactProfile g3CompactProfile = new G3CompactProfile(profileRawData);
        G3LoadProfileEntry[] entries = g3CompactProfile.getEntries();

        if (entries.length == 0) {
            return new ArrayList<IntervalData>();
        }

        if (inverse) {
            List<G3LoadProfileEntry> g3List = Arrays.asList(entries);
            Collections.reverse(g3List);
            entries = g3List.toArray(entries);
        }

        return buildIntervalData(entries, inverse);
    }

    private final List<IntervalData> buildIntervalData(final G3LoadProfileEntry[] profileEntries, boolean inverse) throws IOException {
        final List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        int latestProfileInterval = getFirstInterval(profileEntries);
        int eiCode = 0;

        G3LoadProfileEntry dateStamp = null;
        Calendar calendar = null;

        for (final G3LoadProfileEntry entry : profileEntries) {

            if (entry.isDegraded()) {
                eiCode |= IntervalStateBits.BADTIME;       //Degraded means the clock can not be trusted
            }
            if (entry.isNormalValue()) { // normal interval value

                if (calendar == null) {
                    continue; // first the calendar has to be initialized with the start of load profile marker
                }
                final IntervalData intervaldata = new IntervalData(ProtocolTools.roundUpToNearestInterval(calendar.getTime(), latestProfileInterval / 60));
                intervaldata.addValue(entry.getValue());
                intervalDatas.add(intervaldata);
                latestProfileInterval = entry.getIntervalInSeconds();
                calendar.add(Calendar.SECOND, latestProfileInterval);
                continue;
            }

            if (entry.isPartialValue()) { // partial interval value

                if (calendar == null) {
                    continue; // first the calendar has to be initialized with the start of load profile marker
                }
                eiCode |= IntervalStateBits.SHORTLONG;      //Partial value is always S/L
                final IntervalData intervalData = new IntervalData(ProtocolTools.roundUpToNearestInterval(calendar.getTime(), latestProfileInterval / 60), eiCode);
                intervalData.addValue(entry.getValue());
                eiCode = 0;
                intervalDatas.add(intervalData);
                latestProfileInterval = entry.getIntervalInSeconds();
                calendar.add(Calendar.SECOND, latestProfileInterval);
                continue;
            }

            if (entry.isDate()) { // date stamp
                // date always followed by time? Do the processing if time is received
                dateStamp = entry;
                continue;
            }

            if (entry.isTime()) { // time stamp
                if (dateStamp == null) {
                    // change of the interval, only timestamp is received. Adjust time here...
                } else {
                    // set the calendar
                    calendar = ProtocolUtils.getCleanCalendar(getTimeZone());
                    calendar.set(Calendar.YEAR, dateStamp.getYear());
                    calendar.set(Calendar.MONTH, dateStamp.getMonth() - 1);
                    calendar.set(Calendar.DATE, dateStamp.getDay());
                    calendar.set(Calendar.HOUR_OF_DAY, entry.getHours());
                    calendar.set(Calendar.MINUTE, entry.getMinutes());
                    calendar.set(Calendar.SECOND, entry.getSeconds());
                    if (inverse) {
                        calendar.add(Calendar.SECOND, -1 * latestProfileInterval);  //Correction for the offset, looks like otherwise all entries are 1 interval later
                    }

                    dateStamp = null; // reset the dateStamp

                    if (entry.isStartOfLoadProfile()) {
                        calendar.add(Calendar.SECOND, latestProfileInterval);
                        // do nothing special...
                    } else if (entry.isPowerOff()) {
                        com.energyict.protocolimpl.base.ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
                        eiCode |= IntervalStateBits.POWERDOWN;
                    } else if (entry.isPowerOn()) {
                        com.energyict.protocolimpl.base.ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
                        eiCode |= IntervalStateBits.POWERUP;
                    } else if (entry.isChangeclockOldTime()) {
                        com.energyict.protocolimpl.base.ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
                        eiCode |= IntervalStateBits.SHORTLONG;
                    } else if (entry.isChangeclockNewTime()) {
                        com.energyict.protocolimpl.base.ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
                        eiCode |= IntervalStateBits.SHORTLONG;
                    }
                }
                continue;
            }

        }

        return intervalDatas;
    }

    private int getFirstInterval(G3LoadProfileEntry[] profileEntries) throws IOException {
        for (G3LoadProfileEntry entry : profileEntries) {
            if (entry.isValue()) {
                return entry.getIntervalInSeconds();
            }
        }
        return getProfileInterval();
    }
}