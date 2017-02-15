/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class LoadProfileBuilder {

    // status bitString
    private static final int EV_WATCHDOG_RESET = 0x04;
    private static final int EV_DST = 0x08;
    //private static final int EV_EXTERNAL_CLOCK_SYNC=0x10;
    //private static final int EV_CLOCK_SETTINGS=0x20;
    private static final int EV_ALL_CLOCK_SETTINGS = 0x30;
    private static final int EV_POWER_FAILURE = 0x40;
    private static final int EV_START_OF_MEASUREMENT = 0x80;

    /**
     * Load profile information summary object, holding scaler and unit of each channel
     */
    private static ObisCode LOADPROFILE_INDFORMATION_OBIS = ObisCode.fromString("0.0.99.128.1.255");

    /**
     * Load profiling parameters objects, holding tuime parameters for the load profiling management
     */
    private static ObisCode LOADPROFILING_PARAMETERS_OBIS = ObisCode.fromString("0.0.136.0.1.255");


    /**
     * The device protocol
     */
    private ActarisSl7000 meterProtocol;

    /**
     * The list of LoadProfileReaders which are expected to be fetched
     */
    private List<LoadProfileReader> expectedLoadProfileReaders;

    /**
     * The list of <CODE>LoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the {@link #expectedLoadProfileReaders}
     */
    private List<LoadProfileConfiguration> loadProfileConfigurationList;

    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<LoadProfileReader, List<ChannelInfo>>();

    public LoadProfileBuilder(ActarisSl7000 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #meterProtocol}
     *
     * @param loadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>LoadProfileConfiguration</CODE> objects which are in the device
     * @throws java.io.IOException when error occurred during dataFetching or -Parsing
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) throws IOException {
        expectedLoadProfileReaders = loadProfileReaders;
        loadProfileConfigurationList = new ArrayList<LoadProfileConfiguration>();

        for (LoadProfileReader lpr : expectedLoadProfileReaders) {
            this.meterProtocol.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
            LoadProfileConfiguration lpc = new LoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getDeviceIdentifier());

            try {
                UniversalObject uo;
                if (!meterProtocol.isOldFirmware()) {
                    uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), lpr.getProfileObisCode());
                } else {
                    uo = new UniversalObject(lpr.getProfileObisCode(), DLMSClassId.PROFILE_GENERIC);
                }
                ProfileGeneric pg = new ProfileGeneric(meterProtocol.getDlmsSession(), new ObjectReference(uo.getLNArray(), DLMSClassId.PROFILE_GENERIC.getClassId()));

                List<ChannelInfo> channelInfos = constructChannelInfos(pg, lpr);

                lpc.setChannelInfos(channelInfos);

                Data loadProfilingParameters = meterProtocol.getDlmsSession().getCosemObjectFactory().getData(LOADPROFILING_PARAMETERS_OBIS);
                DataContainer dataContainer = loadProfilingParameters.getDataContainer();
                lpc.setProfileInterval(dataContainer.getRoot().getInteger(0) * 60);

                if (!channelInfoMap.containsKey(lpr)) {
                    channelInfoMap.put(lpr, channelInfos);
                }
            } catch (NestedIOException e) {
                if (ProtocolTools.getRootCause(e) instanceof ConnectionException || ProtocolTools.getRootCause(e) instanceof DLMSConnectionException) {
                    throw e;    // In case of a connection exception (of which we cannot recover), do throw the error.
                }
                lpc.setSupportedByMeter(false);
            } catch (IOException e) {
                lpc.setSupportedByMeter(false);
            } catch (NullPointerException e) {
                lpc.setSupportedByMeter(false);
            }
            loadProfileConfigurationList.add(lpc);
        }
        return loadProfileConfigurationList;
    }

    /**
     * Construct a list of <CODE>ChannelInfos</CODE>.
     **/
    private List<ChannelInfo> constructChannelInfos(ProfileGeneric profileGeneric, LoadProfileReader lpr) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        List<CapturedObject> captureObjects = profileGeneric.getCaptureObjects();

        HashMap<ObisCode, ScalerUnit> loadProfileInformation = getLoadProfileInformation();

        for (CapturedObject capturedObject : captureObjects.subList(4, captureObjects.size())) {
            ObisCode registerObisCode = ObisCode.fromString(capturedObject.getLogicalName().toString());
            Optional<ChannelInfo> channelInfoOptional = loadProfileContains(lpr, registerObisCode);
            if (channelInfoOptional.isPresent()) {
                ScalerUnit scalerUnit = loadProfileInformation.get(registerObisCode);

                if (scalerUnit != null) {
                    if (scalerUnit.getUnitCode() != 0) {
                        ChannelInfo channelInfo = new ChannelInfo(channelInfos.size(), registerObisCode.toString(), scalerUnit.getEisUnit(), meterProtocol.getMeterSerialNumber(), true, channelInfoOptional.get().getReadingType());
                        channelInfos.add(channelInfo);
                    } else {
                        ChannelInfo channelInfo = new ChannelInfo(channelInfos.size(), registerObisCode.toString(), Unit.getUndefined(), meterProtocol.getMeterSerialNumber(), true, channelInfoOptional.get().getReadingType());
                        channelInfos.add(channelInfo);
                    }
                }
            }
        }

        return channelInfos;
    }


    private Optional<ChannelInfo> loadProfileContains(LoadProfileReader lpr, ObisCode obisCode) throws IOException {
        for (ChannelInfo channelInfo : lpr.getChannelInfos()) {
            if (channelInfo.getChannelObisCode().equals(obisCode)) {
                return Optional.of(channelInfo);
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieve the load profile information from the 'load profile information summary' profile
     */
    private HashMap<ObisCode, ScalerUnit> getLoadProfileInformation() throws IOException {
        ProfileGeneric loadProfileInformation = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(LOADPROFILE_INDFORMATION_OBIS);
        DataContainer buffer = loadProfileInformation.getBuffer();
        DataStructure dataStructure = buffer.getRoot().getStructure(0);

        int index = 0;
        HashMap<ObisCode, ScalerUnit> channelInfoMap = new HashMap<ObisCode, ScalerUnit>();
        while (dataStructure.isOctetString(index) && dataStructure.isStructure(index + 1)) {
             ObisCode obisCode = dataStructure.getOctetString(index).toObisCode();
             ScalerUnit scalerUnit = new ScalerUnit(dataStructure.getStructure(index + 1).getInteger(0),
                     dataStructure.getStructure(index + 1).getInteger(1));
            channelInfoMap.put(obisCode, scalerUnit);
            index = index + 2;
        }
        return channelInfoMap;
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>. If {@link LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = lpr.getProfileObisCode();
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(this.channelInfoMap.get(lpr));

                Calendar fromCalendar = Calendar.getInstance(meterProtocol.getTimeZone());
                fromCalendar.setTimeInMillis(lpr.getStartReadingTime().toEpochMilli());
                roundCalendarToMidnight(fromCalendar, false);

                Calendar toCalendar = Calendar.getInstance(meterProtocol.getTimeZone());
                toCalendar.setTimeInMillis(lpr.getEndReadingTime().toEpochMilli());
                roundCalendarToMidnight(toCalendar, true);

                DataContainer dataContainer = profile.getBuffer(fromCalendar, toCalendar);
                buildProfileData(dataContainer, profileData, lpr, lpc);
                profileDataList.add(profileData);
            }
        }
        return profileDataList;
    }

    protected void buildProfileData(DataContainer dataContainer, ProfileData profileData, LoadProfileReader lpr, LoadProfileConfiguration lpc) throws IOException {
        Calendar calendar = null;
        int i;
        boolean currentAdd = true, previousAdd = true;
        IntervalData previousIntervalData = null, currentIntervalData;

        if (dataContainer.getRoot().element.length == 0) {
            throw new IOException("No entries in object list.");
        }

        calendar = ProtocolUtils.initCalendar(false, meterProtocol.getTimeZone());

        for (i = 0; i < dataContainer.getRoot().element.length; i++) { // for all retrieved intervals
            if (dataContainer.getRoot().getStructure(i).isStructure(0)) {
                calendar = parseProfileStartDateTime(dataContainer.getRoot().getStructure(i), calendar, lpc); // new date and/or time?
            }

            // Start of interval
            if (dataContainer.getRoot().getStructure(i).isStructure(0)) {
                currentAdd = parseStart(dataContainer.getRoot().getStructure(i), calendar, profileData, lpc);
            }
            // End of interval
            if (dataContainer.getRoot().getStructure(i).isStructure(1)) {
                currentAdd = parseEnd(dataContainer.getRoot().getStructure(i), calendar, profileData, lpc);
            }
            // time1
            if (dataContainer.getRoot().getStructure(i).isStructure(2)) {
                currentAdd = parseTime1(dataContainer.getRoot().getStructure(i), calendar, profileData, lpc);
            }
            // Time2
            if (dataContainer.getRoot().getStructure(i).isStructure(3)) {
                currentAdd = parseTime2(dataContainer.getRoot().getStructure(i), calendar, profileData, lpc);
            }

            // Adjust calendar for interval with profile interval period
            if (currentAdd) {
                calendar.add(calendar.MINUTE, (lpc.getProfileInterval() / 60));
            }

            currentIntervalData = getIntervalData(dataContainer.getRoot().getStructure(i), calendar, lpr);

            if (currentAdd & !previousAdd) {
                currentIntervalData = addIntervalData(currentIntervalData, previousIntervalData);
            }

            // Add interval data...
            if (currentAdd) {
                profileData.addInterval(currentIntervalData);
            }

            previousIntervalData = currentIntervalData;
            previousAdd = currentAdd;
        }
    }

    private Calendar parseProfileStartDateTime(DataStructure dataStructure, Calendar calendar, LoadProfileConfiguration lpc) throws IOException {
        if (isNewDate(dataStructure.getStructure(0).getOctetString(0).getArray()) || isNewTime(dataStructure.getStructure(0).getOctetString(0).getArray())) {
            calendar = setCalendar(calendar, dataStructure.getStructure(0), 0, lpc);
        }
        return calendar;
    }

    private boolean isNewDate(byte[] array) {
        if ((array[0] != -1) && (array[1] != -1) && (array[2] != -1) && (array[3] != -1)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNewTime(byte[] array) {
        if ((array[5] != -1) && (array[6] != -1) && (array[7] != -1)) {
            return true;
        } else {
            return false;
        }
    }

    private Calendar setCalendar(Calendar cal, DataStructure dataStructure, int btype, LoadProfileConfiguration lpc) throws IOException {
        Calendar calendar = (Calendar) cal.clone();
        if (dataStructure.getOctetString(0).getArray()[0] != -1) {
            calendar.set(Calendar.YEAR, (((int) dataStructure.getOctetString(0).getArray()[0] & 0xff) << 8) |
                    (((int) dataStructure.getOctetString(0).getArray()[1] & 0xff)));
        }

        if (dataStructure.getOctetString(0).getArray()[2] != -1) {
            calendar.set(Calendar.MONTH, ((int) dataStructure.getOctetString(0).getArray()[2] & 0xff) - 1);
        }

        if (dataStructure.getOctetString(0).getArray()[3] != -1) {
            calendar.set(Calendar.DAY_OF_MONTH, ((int) dataStructure.getOctetString(0).getArray()[3] & 0xff));
        }

        if (dataStructure.getOctetString(0).getArray()[5] != -1) {
            calendar.set(Calendar.HOUR_OF_DAY, ((int) dataStructure.getOctetString(0).getArray()[5] & 0xff));
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
        }

        if (btype == 0) {
            if (dataStructure.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, (((int) dataStructure.getOctetString(0).getArray()[6] & 0xff) / (lpc.getProfileInterval() / 60)) * (lpc.getProfileInterval() / 60));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            calendar.set(Calendar.SECOND, 0);
        } else {
            if (dataStructure.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, ((int) dataStructure.getOctetString(0).getArray()[6] & 0xff));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            if (dataStructure.getOctetString(0).getArray()[7] != -1) {
                calendar.set(Calendar.SECOND, ((int) dataStructure.getOctetString(0).getArray()[7] & 0xff));
            } else {
                calendar.set(Calendar.SECOND, 0);
            }
        }

        // if DSA, add 1 hour
        if (dataStructure.getOctetString(0).getArray()[11] != -1) {
            if ((dataStructure.getOctetString(0).getArray()[11] & (byte) 0x80) == 0x80) {
                calendar.add(Calendar.HOUR_OF_DAY, -1);
            }
        }
        return calendar;
    }

    private boolean parseStart(DataStructure dataStructure, Calendar calendar, ProfileData profileData, LoadProfileConfiguration lpc) throws IOException {
        calendar = setCalendar(calendar, dataStructure.getStructure(0), 1, lpc);
        if ((dataStructure.getStructure(0).getInteger(1) & EV_ALL_CLOCK_SETTINGS) != 0) { // time set before
            profileData.addEvent(new MeterEvent(new Date(((Calendar) calendar.clone()).getTime().getTime()),
                    (int) MeterEvent.SETCLOCK_AFTER,
                    (int) dataStructure.getStructure(0).getInteger(1)));
        }
        if ((dataStructure.getStructure(0).getInteger(1) & EV_POWER_FAILURE) != 0) { // power down
            profileData.addEvent(new MeterEvent(new Date(((Calendar) calendar.clone()).getTime().getTime()),
                    (int) MeterEvent.POWERUP,
                    (int) EV_POWER_FAILURE));
        }
        if ((dataStructure.getStructure(0).getInteger(1) & EV_WATCHDOG_RESET) != 0) { // watchdog
            profileData.addEvent(new MeterEvent(new Date(((Calendar) calendar.clone()).getTime().getTime()),
                    (int) MeterEvent.WATCHDOGRESET,
                    (int) EV_WATCHDOG_RESET));
        }
        if ((dataStructure.getStructure(0).getInteger(1) & EV_DST) != 0) { // watchdog
            profileData.addEvent(new MeterEvent(new Date(((Calendar) calendar.clone()).getTime().getTime()),
                    (int) MeterEvent.SETCLOCK_AFTER,
                    (int) EV_DST));
        }
        return true;
    }

    private boolean parseEnd(DataStructure dataStructure, Calendar calendar, ProfileData profileData, LoadProfileConfiguration lpc) throws IOException {
        Calendar endIntervalCal = setCalendar(calendar, dataStructure.getStructure(1), 1, lpc);

        if ((dataStructure.getStructure(1).getInteger(1) & EV_ALL_CLOCK_SETTINGS) != 0) { // time set before
            profileData.addEvent(new MeterEvent(new Date(((Calendar) endIntervalCal.clone()).getTime().getTime()),
                    (int) MeterEvent.SETCLOCK_BEFORE,
                    (int) dataStructure.getStructure(1).getInteger(1)));
        }

        if ((dataStructure.getStructure(1).getInteger(1) & EV_POWER_FAILURE) != 0) { // power down
            profileData.addEvent(new MeterEvent(new Date(((Calendar) endIntervalCal.clone()).getTime().getTime()),
                    (int) MeterEvent.POWERDOWN,
                    (int) EV_POWER_FAILURE));
            return true; // KV 16012004
        }

        /* No WD event added cause time is set to 00h00'00" */
        if ((dataStructure.getStructure(1).getInteger(1) & EV_DST) != 0) { // power down
            profileData.addEvent(new MeterEvent(new Date(((Calendar) endIntervalCal.clone()).getTime().getTime()),
                    (int) MeterEvent.SETCLOCK_BEFORE,
                    (int) EV_DST));
            return true;
        }

        if ((lpc.getProfileInterval() * 1000) - (endIntervalCal.getTimeInMillis() - calendar.getTimeInMillis()) <= 2000) {
            return true;    //GN 25042008 special case ...
        }
        return false;
    }

    private boolean parseTime1(DataStructure dataStructure, Calendar calendar, ProfileData profileData, LoadProfileConfiguration lpc) throws IOException {
        calendar = setCalendar(calendar, dataStructure.getStructure(2), 1, lpc);

        if ((dataStructure.getStructure(2).getInteger(1) & EV_ALL_CLOCK_SETTINGS) != 0) { // time set before
            profileData.addEvent(new MeterEvent(new Date(((Calendar) calendar.clone()).getTime().getTime()),
                    (int) MeterEvent.SETCLOCK_BEFORE,
                    (int) dataStructure.getStructure(2).getInteger(1)));
        }

        if ((dataStructure.getStructure(2).getInteger(1) & EV_POWER_FAILURE) != 0) {// power down
            profileData.addEvent(new MeterEvent(new Date(((Calendar) calendar.clone()).getTime().getTime()),
                    (int) MeterEvent.POWERDOWN,
                    (int) EV_POWER_FAILURE));
        }
        return true;
    }

    private boolean parseTime2(DataStructure dataStructure, Calendar calendar, ProfileData profileData, LoadProfileConfiguration lpc) throws IOException {
        calendar = setCalendar(calendar, dataStructure.getStructure(3), 1, lpc);

        if ((dataStructure.getStructure(3).getInteger(1) & EV_ALL_CLOCK_SETTINGS) != 0) { // time set before
            profileData.addEvent(new MeterEvent(new Date(((Calendar) calendar.clone()).getTime().getTime()),
                    (int) MeterEvent.SETCLOCK_AFTER,
                    (int) dataStructure.getStructure(3).getInteger(1)));
        }

        if ((dataStructure.getStructure(3).getInteger(1) & EV_POWER_FAILURE) != 0) {// power down
            profileData.addEvent(new MeterEvent(new Date(((Calendar) calendar.clone()).getTime().getTime()),
                    (int) MeterEvent.POWERUP,
                    (int) EV_POWER_FAILURE));
        }
        return true;
    }

    private IntervalData addIntervalData(IntervalData currentIntervalData, IntervalData previousIntervalData) {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        int current, i;
        for (i = 0; i < currentCount; i++) {
            current = (currentIntervalData.get(i)).intValue() + (previousIntervalData.get(i)).intValue();
            intervalData.addValue(new Integer(current));
        }
        return intervalData;
    }

    private IntervalData getIntervalData(DataStructure dataStructure, Calendar calendar, LoadProfileReader lpr) throws IOException {
        // Add interval data...
        IntervalData intervalData = new IntervalData(new Date(((Calendar) calendar.clone()).getTime().getTime()));
         List<CapturedObject> captureObjects = meterProtocol.getDlmsSession().getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects();
        for (int i = 0; i < captureObjects.size(); i++) {
            if ((captureObjects.get(i).getClassId() == DLMSClassId.REGISTER.getClassId() ||
                    captureObjects.get(i).getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId() ||
                    captureObjects.get(i).getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()) && isChannelInLpr(lpr, captureObjects.get(i).getObisCode())) {
                intervalData.addValue(new Integer(dataStructure.getInteger(i)));
            }
        }
        return intervalData;
    }

    private boolean isChannelInLpr(LoadProfileReader lpr, ObisCode obisCode) throws IOException {
        for (ChannelInfo each : lpr.getChannelInfos()) {
            if (each.getChannelObisCode().equals(obisCode)) {
                return true;
            }
        }
        return false;
    }

    public Calendar roundCalendarToMidnight(Calendar cal, boolean roundUp) {
        if (roundUp) {
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR ) +1);
        }
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Look for the <CODE>LoadProfileConfiguration</CODE> in the previously build up list
     *
     * @param loadProfileReader the reader linking to the <CODE>LoadProfileConfiguration</CODE>
     * @return requested configuration
     */
    private LoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getDeviceIdentifier().getIdentifier().equals(lpc.getDeviceIdentifier().getIdentifier())) {
                return lpc;
            }
        }
        return null;
    }
}
