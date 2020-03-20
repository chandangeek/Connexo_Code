package com.energyict.protocolimplv2.dlms.a1860.profiledata;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimpl.dlms.a1800.A1800DLMSProfileIntervals;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class A1860ProfileDataHelper {

    private final AbstractDlmsProtocol protocol;
    private ProfileGeneric profileGeneric;
    private Long multiplier = null;
    private Integer scaleFactor = null;
    private int profileInterval = -1;
    private TimeZone timeZone;
    private LoadProfileReader loadProfileReader;

    A1860ProfileDataHelper(AbstractDlmsProtocol protocol, LoadProfileReader loadProfileReader) throws
            NotInObjectListException {
        this.protocol = protocol;
        this.loadProfileReader = loadProfileReader;
        this.profileGeneric = protocol.getDlmsSession()
                .getCosemObjectFactory()
                .getProfileGeneric(loadProfileReader.getProfileObisCode());
    }

    @SuppressWarnings("Duplicates")
    public List<IntervalData> getIntervalData(boolean selectiveHistoricRead) throws IOException {
        readMultiplierAndScaleFactor(loadProfileReader.getProfileObisCode());

        Calendar from = getFromCalendar(loadProfileReader);
        protocol.journal("Reading interval buffer from device for profile [" + loadProfileReader.getProfileObisCode() + "].");

        int profileEntriesInUse = getProfileGeneric().getEntriesInUse(); // The number of profile entries currently in use

        if (profileEntriesInUse == 0) {
            protocol.journal("No profile entries currently in use.");
            return new ArrayList<>();
        }

        long interval = this.getProfileInterval();
        long a1800Time = protocol.getDlmsSession().getCosemObjectFactory().getClock(Clock.getDefaultObisCode()).getDateTime().getTime() / 1000;
        long fromTime = from.getTimeInMillis() / 1000;

        if (interval > 0) {
            if (!selectiveHistoricRead) {
                long entriesToRead = ((a1800Time - fromTime) / interval) + 1;
                if (entriesToRead > profileEntriesInUse) {
                    entriesToRead = profileEntriesInUse; // It makes no sense to request more entries than the amount of entries currently in use
                } else if (entriesToRead < 0) {
                    entriesToRead = 1;                   // This is the case when fromTime is after a1800Time, in fact telling to read out the future -> read out only the last entry
                }

                byte[] bufferData = getProfileGeneric().getBufferData(0, (int) entriesToRead, 0, 0);
                return parseBuffer(bufferData);
            } else {
                return getHistoricData(profileEntriesInUse, a1800Time, interval);
            }
        } else {
            return new ArrayList<>();
        }

    }

    private List<IntervalData> getHistoricData(int profileEntriesInUse, long a1800Time, long interval) throws IOException {
        long fromTime = loadProfileReader.getStartReadingTime().getTime() / 1000;
        long toTime = loadProfileReader.getEndReadingTime().getTime() / 1000;

        long totalEntries = ((a1800Time - fromTime) / interval) + 1;

        int fromEntry = (int) ((a1800Time - toTime) / interval) + 1;
        int entriesToRead = (int) totalEntries - fromEntry;
        int toEntry = fromEntry + entriesToRead;

        if (fromEntry > profileEntriesInUse) {
            protocol.journal("Trying to read from entry " + fromEntry + " but the maximum entry is " + profileEntriesInUse + ". No entries will be read.");
            return new ArrayList<>();
        } else if (fromEntry < 0) {
            protocol.journal("Trying to read from entry " + fromEntry + " (data from the future not available yet). Overriding to 0.");
            fromEntry = 0;
        }

        if (toEntry > profileEntriesInUse) {
            protocol.journal("Trying to read until entry " + toEntry + " but the maximum entry is " + profileEntriesInUse + ". Overriding to maximum.");
            toEntry = profileEntriesInUse;
        } else if (toEntry < 0) {
            protocol.journal("Trying to read until entry " + toEntry + " (data from the future not available yet). Overriding to 1.");
            toEntry = 1;
        }

        protocol.journal("Requesting data from entry " + fromEntry + " to entry " + toEntry);

        byte[] bufferData = getProfileGeneric().getBufferData(fromEntry, toEntry, 0, 0);
        final List<IntervalData> intervalData = parseBuffer(bufferData);

        if (intervalData.size() > 0) {
            protocol.journal("Received intervals from " + intervalData.get(intervalData.size() - 1).getEndTime() + " to " + intervalData.get(0).getEndTime());
        }

        return intervalData;
    }

    private ProfileGeneric getProfileGeneric() {
        return profileGeneric;
    }

    private void readMultiplierAndScaleFactor(ObisCode obisCode) throws IOException {

        ObisCode loadProfileMultiplier = null;
        ObisCode loadProfileScaleFactor = null;

        if (obisCode.equals(A1860LoadProfileDataReader.LOAD_PROFILE_PULSES)) {

            loadProfileMultiplier = A1860LoadProfileDataReader.MULTIPLIER_NON_INSTRUMENTATION;
            loadProfileScaleFactor = A1860LoadProfileDataReader.SCALE_FACTOR_NON_INSTRUMENTATION;

        } else if (obisCode.equals(A1860LoadProfileDataReader.LOAD_PROFILE_EU_CUMULATIVE) ||
                obisCode.equals(A1860LoadProfileDataReader.LOAD_PROFILE_EU_NONCUMULATIVE)) {

            loadProfileScaleFactor = A1860LoadProfileDataReader.SCALE_FACTOR_NON_INSTRUMENTATION;

        } else if (obisCode.equals(A1860LoadProfileDataReader.PROFILE_INSTRUMENTATION_SET1) ||
                obisCode.equals(A1860LoadProfileDataReader.PROFILE_INSTRUMENTATION_SET2)) {

            loadProfileMultiplier  = A1860LoadProfileDataReader.MULTIPLIER_INSTRUMENTATION;
            loadProfileScaleFactor = A1860LoadProfileDataReader.SCALE_FACTOR_INSTRUMENTATION;

        } else {
            protocol.journal("Could not determine Load Profile Multiplier and Scale Factor for OBIS code " + obisCode.toString());
        }

        if (loadProfileScaleFactor != null) {
            final Data scaleFactorData = protocol.getDlmsSession().getCosemObjectFactory().getData(loadProfileScaleFactor);
            AbstractDataType adt = scaleFactorData.getValueAttr();
            if (adt.isInteger64()) {
                scaleFactor = adt.getInteger64().intValue();
            } else if (adt.isInteger8()) {
                scaleFactor = adt.getInteger8().intValue();
            }
            protocol.journal("Profile scale factor: " + BigDecimal.ONE.scaleByPowerOfTen(scaleFactor));
        }

        if (loadProfileMultiplier != null) {
            final Data multiplierData = protocol.getDlmsSession().getCosemObjectFactory().getData(loadProfileMultiplier);
            AbstractDataType adt = multiplierData.getValueAttr();
            multiplier = adt.longValue();
            protocol.journal("Profile multiplier: " + multiplier);
        }
    }

    private Calendar getFromCalendar(LoadProfileReader loadProfileReader) {
        ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), 25);
        Calendar fromCal = Calendar.getInstance(protocol.getTimeZone());
        fromCal.setTime(profileLimiter.getFromDate());
        fromCal.set(Calendar.SECOND, 0);
        return fromCal;
    }

    /**
     * Try to read the profile interval from the meter. This is the interval in seconds.
     * The profile interval is cached for future use.
     *
     * @return The profile interval
     * @throws java.io.IOException
     */
    private int getProfileInterval() throws IOException {
        if (profileInterval == -1) {
            try {
                this.profileInterval = getProfileGeneric().getCapturePeriod();
            } catch (IOException e) {
                throw new NestedIOException(e, "Unable to read profile interval: " + e.getMessage());
            }
        }
        return profileInterval;
    }

    private List<IntervalData> parseBuffer(byte[] bufferData) throws IOException {
        A1800DLMSProfileIntervals intervals = new A1800DLMSProfileIntervals(bufferData, 0x0001, 0x0002, -1, 0x0004, null);
        if (multiplier != null) {
            intervals.setMultiplier(multiplier);
            protocol.journal("Setting multiplier = " + multiplier);
        }
        if (scaleFactor != null) {
            intervals.setScaleFactor(scaleFactor);
            protocol.journal("Setting scale = " + scaleFactor);
        }
        return intervals.parseIntervals(getProfileInterval(), getTimeZone());
    }

    private TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = protocol.getTimeZone();
        }
        return timeZone;
    }
}
