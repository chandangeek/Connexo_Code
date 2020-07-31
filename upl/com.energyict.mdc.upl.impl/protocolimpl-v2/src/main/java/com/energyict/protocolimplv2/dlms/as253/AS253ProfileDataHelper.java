package com.energyict.protocolimplv2.dlms.as253;

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

public class AS253ProfileDataHelper {

    private final AbstractDlmsProtocol protocol;
    private ProfileGeneric profileGeneric;
    private Long multiplier = null;
    private Integer scaleFactor = null;
    private int profileInterval = -1;
    private TimeZone timeZone;
    private LoadProfileReader loadProfileReader;

    AS253ProfileDataHelper(AbstractDlmsProtocol protocol, LoadProfileReader loadProfileReader) throws
            NotInObjectListException {
        this.protocol = protocol;
        this.loadProfileReader = loadProfileReader;
        this.profileGeneric = protocol.getDlmsSession()
                .getCosemObjectFactory()
                .getProfileGeneric(loadProfileReader.getProfileObisCode());
    }

    @SuppressWarnings("Duplicates")
    public List<IntervalData> getIntervalData() throws IOException {
        readMultiplierAndScaleFactor(loadProfileReader.getProfileObisCode());

        Calendar from = getFromCalendar(loadProfileReader);
        protocol.getLogger().info("Reading interval buffer from device for profile [" + loadProfileReader.getProfileObisCode() + "].");

        int profileEntriesInUse = getProfileGeneric().getEntriesInUse(); // The number of profile entries currently in use
        long interval = this.getProfileInterval();
        long a1800Time = protocol.getDlmsSession().getCosemObjectFactory().getClock(Clock.getDefaultObisCode()).getDateTime().getTime() / 1000;
        long fromTime = from.getTimeInMillis() / 1000;

        if (interval > 0) {
            long entriesToRead = ((a1800Time - fromTime) / interval) + 1;
            if (profileEntriesInUse == 0){
                return new ArrayList<>();// In case the profile buffer is empty
            } else if (entriesToRead > profileEntriesInUse) {
                entriesToRead = profileEntriesInUse; // It makes no sense to request more entries than the amount of entries currently in use
            } else if (entriesToRead < 0) {
                entriesToRead = 1;                   // This is the case when fromTime is after a1800Time, in fact telling to read out the future -> read out only the last entry
            }

            byte[] bufferData = getProfileGeneric().getBufferData(0, (int) entriesToRead, 0, 0);
            return parseBuffer(bufferData);
        } else {
            return new ArrayList<>();
        }

    }

    private ProfileGeneric getProfileGeneric() {
        return profileGeneric;
    }

    private void readMultiplierAndScaleFactor(ObisCode obisCode) throws IOException {

        ObisCode loadProfileMultiplier = null;
        ObisCode loadProfileScaleFactor = null;

        if (obisCode.equals(AS253LoadProfileDataReader.LOAD_PROFILE_PULSES)) {

            loadProfileMultiplier = AS253LoadProfileDataReader.MULTIPLIER_NON_INSTRUMENTATION;
            loadProfileScaleFactor = AS253LoadProfileDataReader.SCALE_FACTOR_NON_INSTRUMENTATION;

        } else if (obisCode.equals(AS253LoadProfileDataReader.LOAD_PROFILE_EU_CUMULATIVE) ||
                obisCode.equals(AS253LoadProfileDataReader.LOAD_PROFILE_EU_NONCUMULATIVE)) {

            loadProfileScaleFactor = AS253LoadProfileDataReader.SCALE_FACTOR_NON_INSTRUMENTATION;

        } else if (obisCode.equals(AS253LoadProfileDataReader.PROFILE_INSTRUMENTATION_SET1) ||
                obisCode.equals(AS253LoadProfileDataReader.PROFILE_INSTRUMENTATION_SET2)) {

            loadProfileMultiplier  = AS253LoadProfileDataReader.MULTIPLIER_INSTRUMENTATION;
            loadProfileScaleFactor = AS253LoadProfileDataReader.SCALE_FACTOR_INSTRUMENTATION;

        } else {
            protocol.getLogger().info("Could not determine Load Profile Multiplier and Scale Factor for OBIS code " + obisCode.toString());
        }

        if (loadProfileScaleFactor != null) {
            final Data scaleFactorData = protocol.getDlmsSession().getCosemObjectFactory().getData(loadProfileScaleFactor);
            AbstractDataType adt = scaleFactorData.getValueAttr();
            if (adt.isInteger64()) {
                scaleFactor = adt.getInteger64().intValue();
            } else if (adt.isInteger8()) {
                scaleFactor = adt.getInteger8().intValue();
            }
            protocol.getLogger().info("Profile scale factor: " + BigDecimal.ONE.scaleByPowerOfTen(scaleFactor));
        }

        if (loadProfileMultiplier != null) {
            final Data multiplierData = protocol.getDlmsSession().getCosemObjectFactory().getData(loadProfileMultiplier);
            AbstractDataType adt = multiplierData.getValueAttr();
            multiplier = adt.longValue();
            protocol.getLogger().info("Profile multiplier: " + multiplier);
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
     * @throws IOException
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
        }
        if (scaleFactor != null) {
            intervals.setScaleFactor(scaleFactor);
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
