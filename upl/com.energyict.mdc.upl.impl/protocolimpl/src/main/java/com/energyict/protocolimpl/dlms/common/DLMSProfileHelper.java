package com.energyict.protocolimpl.dlms.common;

import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class containing functionality to create the channelInfos for a load profile and cache it.
 * This is used for the Prime and the G3 protocol.
 * It also contains the common method to fetch and parse intervals from a LP buffer.
 * <p/>
 * Copyrights EnergyICT
 * Date: 16/08/12
 * Time: 10:39
 * Author: khe
 */
public class DLMSProfileHelper {

    private DlmsSession session;
    private List<ChannelInfo> channelInfos = null;
    private ProfileCache cache;
    private ObisCode obisCode;
    private ProfileGeneric profileGeneric = null;
    private int profileInterval = -1;
    private int clockMask = 1;        //By default, the first captured_object is the clock timestamp
    private int statusMask = 2;       //By default, the second captured_object is the interval status
    private Logger logger = null;
    private TimeZone timeZone = null;
    private CosemObjectFactory cosemObjectFactory;
    private String serialNumber = "";

    protected void setSession(DlmsSession session) {
        this.session = session;
    }

    protected String getSerialNumber() {
        return serialNumber;
    }

    protected void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    protected void setStatusMask(int statusMask) {
        this.statusMask = statusMask;
    }

    protected void setClockMask(int clockMask) {
        this.clockMask = clockMask;
    }

    protected void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    protected DlmsSession getSession() {
        return session;
    }

    protected void setCosemObjectFactory(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    protected void setCache(ProfileCache cache) {
        this.cache = cache;
    }

    protected void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public List<ChannelInfo> getChannelInfos() {
        return channelInfos;
    }

    protected void setChannelInfos(List<ChannelInfo> channelInfos) {
        this.channelInfos = channelInfos;
    }

    protected void addChannelInfo(ChannelInfo channelInfo) {
        this.channelInfos.add(channelInfo);
    }

    protected ObisCode getObisCode() {
        return obisCode;
    }

    /**
     * Lazy getter for the ProfileGeneric object
     *
     * @return The {@link com.energyict.dlms.cosem.ProfileGeneric} object
     * @throws java.io.IOException If we were unable to fetch the {@link com.energyict.dlms.cosem.ProfileGeneric} object
     *                     from the {@link com.energyict.dlms.cosem.CosemObjectFactory}
     */
    protected ProfileGeneric getProfileGeneric() throws IOException {
        if (profileGeneric == null) {
            this.profileGeneric = getCosemObjectFactory().getProfileGeneric(obisCode);
        }
        return profileGeneric;
    }

    /**
     * Get the cached channel info's or read them from the device if the cache is empty.
     * This is the fastest and preferred way to get the channel info's instead of {@link DLMSProfileHelper#readChannelInfosFromDevice()}
     *
     * @return A list iof channel info's
     * @throws java.io.IOException If there occurred an error while reading the channel infos
     */
    protected List<ChannelInfo> buildChannelInfos() throws IOException {
        if (channelInfos == null) {
            channelInfos = this.cache.getChannelInfo(obisCode);
            if (channelInfos != null) {
                getLogger().info("Fetched channel info from cache for profile [" + obisCode + "]. No need to fetch them from the meter.");
            } else {
                readChannelInfosFromDevice();
                if (channelInfos != null) {
                    this.cache.cache(obisCode, channelInfos);
                }
            }
        }
        return channelInfos;
    }

    protected Logger getLogger() {
        if (logger == null) {
            logger = getSession().getLogger();
        }
        return logger;
    }

    public int getNumberOfChannels() throws IOException {
        return getProfileGeneric().getNumberOfProfileChannels();
    }

    /**
     * Re-init the list of channel info's and build them again with the data received from the meter
     * This method does not use the cache, so it can be time consuming. You should use {@link DLMSProfileHelper#buildChannelInfos} to get the channel info's
     *
     * @throws java.io.IOException If there was an error while reading the channel info's the meter
     */
    protected void readChannelInfosFromDevice() throws IOException {
        getLogger().info("Reading captured object from device for profile [" + obisCode + "].");

        this.channelInfos = new ArrayList<ChannelInfo>(getNumberOfChannels());
        List<CapturedObject> universalObjects = getProfileGeneric().getCaptureObjects();
        int channelIndex = 0;
        for (CapturedObject capturedObject : universalObjects) {
            if (isRealChannelData(capturedObject)) {
                ObisCode obis = capturedObject.getObisCode();
                Unit unit = getUnit(capturedObject);
                String name = obis.toString();
                ChannelInfo channelInfo = new ChannelInfo(channelIndex++, name, unit, getSerialNumber());
                if (ParseUtils.isObisCodeCumulative(obis)) {
                    channelInfo.setCumulative();
                }
                this.channelInfos.add(channelInfo);
            }
        }
    }

    /**
     * Check if the data is a real channel instead of meta data about the actual value like
     * the timestamp or interval status.
     *
     * @param capturedObject The captured object to validate
     * @return true if the channel is not a timestamp of interval state bit
     */
    protected boolean isRealChannelData(CapturedObject capturedObject) {
        DLMSClassId classId = DLMSClassId.findById(capturedObject.getClassId());
        return classId != DLMSClassId.CLOCK && classId != DLMSClassId.DATA;
    }

    /**
     * Preferred way to get the unit for a given CapturedObject. This method uses the cache if possible, so this is the fastest way to get the Unit
     * (Compared to {@link DLMSProfileHelper#readUnitFromDevice(com.energyict.dlms.cosem.CapturedObject)})
     *
     * @param capturedObject The captured object to get the unit from
     * @return The unit or null if not available
     * @throws java.io.IOException If there occurred an error while reading the unit from the device
     */
    protected final Unit getUnit(CapturedObject capturedObject) throws IOException {
        Unit unit = cache.getUnit(capturedObject);
        if (unit == null) {
            unit = readUnitFromDevice(capturedObject);
            if (unit != null) {
                cache.cache(capturedObject, unit);
            }
        }
        return unit;
    }

    /**
     * Read the unit for a given CapturedObject directly from the device, without using the cache. It's best to use
     * {@link DLMSProfileHelper#getUnit(com.energyict.dlms.cosem.CapturedObject)} to get the unit for a given CapturedObject
     * instead of calling this method directly
     *
     * @param capturedObject The captured object to get the unit from
     * @return The unit or {@link com.energyict.cbo.Unit#getUndefined()} if not available
     * @throws java.io.IOException If there occurred an error while reading the unit from the device
     */
    private final Unit readUnitFromDevice(final CapturedObject capturedObject) throws IOException {
        final ObisCode obis = capturedObject.getObisCode();
        final DLMSClassId classId = DLMSClassId.findById(capturedObject.getClassId());

        final int attr = capturedObject.getAttributeIndex();
        switch (classId) {
            case REGISTER: {
                return getCosemObjectFactory().getRegister(obis).getScalerUnit().getEisUnit();
            }

            case EXTENDED_REGISTER: {
                if (attr == ExtendedRegisterAttributes.VALUE.getAttributeNumber()) {
                    return getCosemObjectFactory().getExtendedRegister(obis).getScalerUnit().getEisUnit();
                } else if (attr == ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber()) {
                    return Unit.get("ms");
                }
                return Unit.getUndefined();
            }

            case DEMAND_REGISTER: {
                if (attr == DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber()) {
                    return getCosemObjectFactory().getDemandRegister(obis).getScalerUnit().getEisUnit();
                } else if (attr == DemandRegisterAttributes.LAST_AVG_VALUE.getAttributeNumber()) {
                    return getCosemObjectFactory().getDemandRegister(obis).getScalerUnit().getEisUnit();
                } else if (attr == DemandRegisterAttributes.LAST_AVG_VALUE.getAttributeNumber()) {
                    return Unit.get("ms");
                }
                return Unit.getUndefined();
            }

            default: {
                return Unit.getUndefined();
            }

        }
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        if (cosemObjectFactory == null) {
            cosemObjectFactory = getSession().getCosemObjectFactory();
        }
        return cosemObjectFactory;
    }

    /**
     * Try to read the profile interval from the meter. This is the interval in seconds.
     * The profile interval is cached for future use.
     *
     * @return The profile interval
     * @throws java.io.IOException
     */
    public int getProfileInterval() throws IOException {
        if (profileInterval == -1) {
            try {
                this.profileInterval = getProfileGeneric().getCapturePeriod();
            } catch (IOException e) {
                throw new NestedIOException(e, "Unable to read profile interval: " + e.getMessage());
            }
        }
        return profileInterval;
    }

    /**
     * Fetch the profile data from the device, given the from date and the to date as limits
     *
     * @param fromDate The from date limit
     * @param toDate   The to date limit
     * @return The profile data
     * @throws java.io.IOException If there occurred an error while reading the profile data from the meter
     */
    public ProfileData getProfileData(Date fromDate, Date toDate) throws IOException {
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(buildChannelInfos());
        profileData.setIntervalDatas(getIntervalDatas(getCalendar(fromDate), getCalendar(toDate)));
        return profileData;
    }

    /**
     * Fetch the profile buffer from the device, taking the from and to date in account,
     * and convert it to a list of interval data with status bits
     * <p/>
     * This method catches ClassCastExceptions (in case of unexpected data types in LP entries) to end the DLMS session properly
     *
     * @param from The date to fetch event from
     * @param to   The to date
     * @return The list of interval data
     * @throws java.io.IOException If there was an error while reading the profile buffer from the device
     */
    protected final List<IntervalData> getIntervalDatas(Calendar from, Calendar to) throws IOException {
        getLogger().info("Reading interval buffer from device for profile [" + getObisCode() + "].");
        byte[] bufferData = getProfileGeneric().getBufferData(from, to);
        return parseBuffer(bufferData);
    }

    public List<IntervalData> parseBuffer(byte[] bufferData) throws IOException {
        setClockAndStatusPosition();
        DLMSProfileIntervals intervals = new DLMSProfileIntervals(bufferData, clockMask, statusMask, -1, new DlmsProfileIntervalStatusBits());
        try {
            return intervals.parseIntervals(getProfileInterval(), getTimeZone());
        } catch (ClassCastException e) {
            throw new IOException(e.getMessage());
        }
    }

    protected TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = getSession().getTimeZone();
        }
        return timeZone;
    }

    protected void setClockAndStatusPosition() {
        //Subclasses can override, managing multiple load profiles with different captured_object definitions
    }

    /**
     * Convert a given date to a calendar, using the device time zone from EIServer
     *
     * @param date The date
     * @return The new calendar object, with the given date and device timezone
     */
    protected final Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.setTime(date);
        return calendar;
    }
}