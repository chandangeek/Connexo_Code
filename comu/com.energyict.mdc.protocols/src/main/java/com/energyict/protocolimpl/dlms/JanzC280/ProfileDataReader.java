/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.JanzC280;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimpl.dlms.JanzC280.events.GeneralEventLog;
import com.energyict.protocolimpl.dlms.JanzC280.events.QualityEventLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class ProfileDataReader {

    private static ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("1.0.99.98.0.255");
    private static ObisCode QUALITY_LOG = ObisCode.fromString("1.0.99.98.1.255");
    private static final int MILLIS_IN_SECOND = 1000;
    private static long BASE_NUMBER_OF_SECONDS = (long) 946684800; // = number of seconds 1 Jan 1970 UTC - 1 Jan 2000 UTC

    protected JanzC280 janzC280;

    public ProfileDataReader(JanzC280 janzC280) {
        this.janzC280 = janzC280;
    }

    /**
     * Retrieve the profile data present in the device
     * Note: all channels are separated from each other and correspond to an unique ProfileGeneric class_id 7 object.
     * The buffer of each profile generic contains the load for 1 channel.
     *
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        ProfileData profileData;
        try {
            if (to == null) {
                to = new Date();
            }

            // from and to date should be transformed to number of seconds since 1 jan 2000.
            Calendar fromCal = Calendar.getInstance(janzC280.getTimeZone());
            fromCal.setTime(from);
            Calendar toCal = Calendar.getInstance(janzC280.getTimeZone());
            toCal.setTime(to);

            long fromDateStamp = getHoraLegalFromCalendar(fromCal);
            long toDateStamp = getHoraLegalFromCalendar(toCal);

            int length = janzC280.getLoadProfileObisCodes().length;
            ProfileGeneric[] profileGenerics = new ProfileGeneric[length];
            profileData = new ProfileData();
            List<IntervalData> intervalDatas = new ArrayList<IntervalData>();

            for (int i = 0; i < length; i++) {
                profileGenerics[i] = janzC280.getCosemObjectFactory().getProfileGeneric(janzC280.getLoadProfileObisCodes()[i]);
                profileData.addChannel(getChannelInfo(i));
                DataContainer buffer = profileGenerics[i].getBuffer(fromDateStamp, toDateStamp);
                Object[] loadProfileEntries = buffer.getRoot().getElements();

                for (int index = 0; index < loadProfileEntries.length; index++) {
                    DataStructure structure = buffer.getRoot().getStructure(index);
                    byte[] eventDef = new byte[]{(byte) structure.getInteger(0), (byte) structure.getInteger(1)};

                    long horaLegal = structure.getValue(2);
                    Calendar cal = getCalendarFromHoraLegal(horaLegal);
                    int protocolStatus = Integer.parseInt(String.valueOf(structure.getValue(0))
                            + (structure.getValue(1) < 10 ? "0" : "")
                            + String.valueOf(structure.getValue(1)));

                    float value = structure.getFloat(4);

                    // Construct an intervalData object
                    // The first iteration, a new intervalData object should be created.
                    // All following iterations should inject their values into to the existing object.
                    if (i == 0) {
                        List<IntervalValue> values = new ArrayList<IntervalValue>();
                        values.add(new IntervalValue(value, 0, 0));
                        intervalDatas.add(new IntervalData(cal.getTime(), getEiServerStatus(eventDef), protocolStatus, 0, values));
                    } else {
                        IntervalData intervalData = intervalDatas.get(index);
                        if (intervalData.getEndTime().equals(cal.getTime())) {
                            intervalData.getIntervalValues().add(new IntervalValue(value, protocolStatus, getEiServerStatus(eventDef)));
                        } else {
                            throw new IOException("Error while constructing the profile data - The interval periods of the different channels do not match!");
                        }
                    }
                }
                profileData.setIntervalDatas(intervalDatas);
            }

            if (includeEvents) {
                List<MeterEvent> meterEvents = getMeterEvents(fromDateStamp, toDateStamp);
                profileData.setMeterEvents(meterEvents);
            }

        } catch (DataAccessResultException dataBlockUnavailable) {
            // When data is searched by entry or by dates, but the EB has no data to satisfy the search,
            // it will return the error 'data block unavailable'.
            // Here we just absorb the error and return an empty dataContainer.
            profileData = new ProfileData();
            janzC280.getLogger().warning("Warning: The meter contains no profile data for the given period!");
        }

        return profileData;
    }

     /**
     * Convert the Hora Legal timestamp to a Calendar
     *
     * @param horaLegal: Number of seconds [1 Jan 2000 - current UTC time] + time zone offset in seconds + daylight saving offset (0 if in winterTime | 3600 seconds if in summerTime).
     * @return a Calendar containing the date/time.
     */
    private Calendar getCalendarFromHoraLegal(long horaLegal) {
        Calendar gmtCal = ProtocolUtils.getCleanGMTCalendar();
        gmtCal.setTimeInMillis((BASE_NUMBER_OF_SECONDS + horaLegal) * MILLIS_IN_SECOND);

        Calendar localCal = Calendar.getInstance(janzC280.getTimeZone());
        localCal.set(Calendar.YEAR, gmtCal.get(Calendar.YEAR));
        localCal.set(Calendar.MONTH, gmtCal.get(Calendar.MONTH));
        localCal.set(Calendar.DAY_OF_MONTH, gmtCal.get(Calendar.DAY_OF_MONTH));
        localCal.set(Calendar.HOUR_OF_DAY, gmtCal.get(Calendar.HOUR_OF_DAY));
        localCal.set(Calendar.MINUTE, gmtCal.get(Calendar.MINUTE));
        localCal.set(Calendar.SECOND, gmtCal.get(Calendar.SECOND));
        localCal.set(Calendar.MILLISECOND, gmtCal.get(Calendar.MILLISECOND));
        return localCal;
    }

     /**
     * Convert the Calendar to a Hora Legal timestamp
      *
      * @param cal
     * @return hora legal timestamp
     */
    private long getHoraLegalFromCalendar(Calendar cal) {
        long timestamp = cal.getTimeInMillis() / MILLIS_IN_SECOND;
        timestamp -= BASE_NUMBER_OF_SECONDS;
        timestamp += (janzC280.getTimeZone().getRawOffset() / MILLIS_IN_SECOND);
        timestamp += (janzC280.getTimeZone().inDaylightTime(cal.getTime()) ? 3600 : 0);
        return timestamp;
    }

    protected List<MeterEvent> getMeterEvents(long fromCal, long toCal) throws IOException {
        List<MeterEvent> events = new ArrayList<MeterEvent>();
        events.addAll(getGeneralEventLog(fromCal, toCal));
        events.addAll(getQualityEventLog(fromCal, toCal));
        return events;
    }

    protected ChannelInfo getChannelInfo(int index) throws IOException {
        Integer chnNumber = janzC280.getEnabledChannelNumbers().get(index);
        ObisCode obisCode = ObisCode.fromString("1.0.99.128." + chnNumber + ".255");
        RegisterValue registerValue = janzC280.readRegister(obisCode);
        return new ChannelInfo(index, chnNumber - 1, "1.0.99.1." + chnNumber + ".255", registerValue.getQuantity().getUnit());
    }

    private int getEiServerStatus(byte[] eventDef) {
        if (eventDef[0] == 0) {
            return IntervalStateBits.OK;
        }
        if (eventDef[0] == 4) {
            return IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if (eventDef[0] == 5) {
            return IntervalStateBits.OTHER;
        }
        if (eventDef[0] == 7) {
            return IntervalStateBits.PHASEFAILURE;
        }
        if (eventDef[0] == 8) {
            return IntervalStateBits.PHASEFAILURE;
        }
        return IntervalStateBits.OTHER;
    }

    private List<MeterEvent> getQualityEventLog(long fromCal, long toCal) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        try {
            DataContainer QualityEventLogDC = janzC280.getCosemObjectFactory().getProfileGeneric(QUALITY_LOG).getBuffer(fromCal, toCal);
            QualityEventLog qualityEventLog = new QualityEventLog(janzC280.getTimeZone(), QualityEventLogDC);
            meterEvents = qualityEventLog.getMeterEvents();
        } catch (DataAccessResultException e) {
            // Object undefined error
            janzC280.getLogger().log(Level.INFO, "Quality event log is empty.");
        }
        return meterEvents;
    }

    // Get all general events.
    private List<MeterEvent> getGeneralEventLog(long fromCal, long toCal) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        try {
            // Retrieve events with priority level I
            DataContainer generalEventLogDC = janzC280.getCosemObjectFactory().getProfileGeneric(STANDARD_EVENT_LOG).getBuffer(fromCal, toCal);
            GeneralEventLog generalEventLog = new GeneralEventLog(janzC280.getTimeZone(), generalEventLogDC);
            meterEvents = generalEventLog.getMeterEvents();
        } catch (DataAccessResultException e) {
            // Object undefined error
            janzC280.getLogger().log(Level.INFO, "General event log is empty.");
        }
        return meterEvents;
    }
}