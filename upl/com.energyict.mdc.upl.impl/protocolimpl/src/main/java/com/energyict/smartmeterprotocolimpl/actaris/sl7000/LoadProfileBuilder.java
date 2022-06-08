package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 19/07/12
 * Time: 10:13
 */
public class LoadProfileBuilder {

    // status bitString
    private static final int EV_WATCHDOG_RESET = 0x04;
    private static final int EV_DST = 0x08;
    private static final int EV_ALL_CLOCK_SETTINGS = 0x30;
    private static final int EV_POWER_FAILURE = 0x40;
    private static final int EV_CLOCK_SETTINGS = 0x20;

    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

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
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<>();

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
        loadProfileConfigurationList = new ArrayList<>();

        for (LoadProfileReader lpr : expectedLoadProfileReaders) {
            this.meterProtocol.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
            LoadProfileConfiguration lpc = new LoadProfileConfiguration(lpr.getProfileObisCode(), new DeviceIdentifierBySerialNumber(meterProtocol.getMeterSerialNumber()));

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
            } catch (IOException | NullPointerException e) {
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
        List<ChannelInfo> channelInfos = new ArrayList<>();
        List<CapturedObject> captureObjects = profileGeneric.getCaptureObjects();

        Map<ObisCode, ScalerUnit> loadProfileInformation = getLoadProfileInformation();

        for (CapturedObject capturedObject : captureObjects.subList(4, captureObjects.size())) {
            ObisCode registerObisCode = ObisCode.fromString(capturedObject.getLogicalName().toString());
            if (loadProfileContains(lpr, registerObisCode)) {
                ScalerUnit scalerUnit = loadProfileInformation.get(registerObisCode);

                if (scalerUnit != null) {
                    if (scalerUnit.getUnitCode() != 0) {
                        ChannelInfo channelInfo = new ChannelInfo(channelInfos.size(), registerObisCode.toString(), scalerUnit.getEisUnit(), meterProtocol.getMeterSerialNumber(), ParseUtils.isObisCodeCumulative(registerObisCode));
                        channelInfos.add(channelInfo);
                    } else {
                        ChannelInfo channelInfo = new ChannelInfo(channelInfos.size(), registerObisCode.toString(), Unit.getUndefined(), meterProtocol.getMeterSerialNumber(), true);
                        channelInfos.add(channelInfo);
                    }
                }
            }
        }

        return channelInfos;
    }


    private boolean loadProfileContains(LoadProfileReader lpr, ObisCode obisCode) {
        for (ChannelInfo channelInfo : lpr.getChannelInfos()) {
            if (channelInfo.getChannelObisCode().equals(obisCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve the load profile information from the 'load profile information summary' profile
     */
    private Map<ObisCode, ScalerUnit> getLoadProfileInformation() throws IOException {
        ProfileGeneric loadProfileInformation = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(LOADPROFILE_INDFORMATION_OBIS);
        DataContainer buffer = loadProfileInformation.getBuffer();
        DataStructure dataStructure = buffer.getRoot().getStructure(0);

        int index = 0;
        Map<ObisCode, ScalerUnit> channelInfoMap = new HashMap<>();
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
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
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
        List<ProfileData> profileDataList = new ArrayList<>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = lpr.getProfileObisCode();
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(this.channelInfoMap.get(lpr));
                ProfileLimiter limiter = new ProfileLimiter(lpr.getStartReadingTime(), lpr.getEndReadingTime() == null ? new Date() : lpr.getEndReadingTime(), getLimitMaxNrOfDays(), meterProtocol.getTimeZone());
                this.meterProtocol.getLogger().log(Level.INFO, "Retrieving LoadProfile data for profile " + lpr.getProfileObisCode() +
                        " for period [" + DATE_FORMATTER.format(limiter.getFromDate()) + " - " + DATE_FORMATTER.format(limiter.getToDate()) + "].");

                DataContainer dataContainer = profile.getBuffer(limiter.getFromCalendar(), limiter.getToCalendar());
                buildProfileData(dataContainer, profileData, lpr, lpc);

                // If there are no intervals in the profile, read the profile data again, but now with limitMaxNrOfDays increased with the value of Custom Property limitMaxNrOfDays property
                // This way we can prevent the profile to be stuck at a certain date if there is a gap in the profile bigger than the limitMaxNrOfDays.
                while ((profileData.getIntervalDatas().isEmpty()) && (getLimitMaxNrOfDays() > 0) && (limiter.getOriginalToDate().getTime() >= limiter.getToDate().getTime())) {
                    limiter = new ProfileLimiter(lpr.getStartReadingTime(), lpr.getEndReadingTime() == null ? new Date() : lpr.getEndReadingTime(), limiter.getLimitMaxNrOfDays() + getLimitMaxNrOfDays(), meterProtocol.getTimeZone());
                    this.meterProtocol.getLogger().log(Level.INFO, "Retrieved no LoadProfile data for profile " + lpr.getProfileObisCode() +
                            " - re-retrieving data for period [" + DATE_FORMATTER.format(limiter.getFromDate()) + " - " + DATE_FORMATTER.format(limiter.getToDate()) + "].");
                    dataContainer = profile.getBuffer(limiter.getFromCalendar(), limiter.getToCalendar());
                    buildProfileData(dataContainer, profileData, lpr, lpc);
                }
                profileDataList.add(profileData);
            }
        }
        return profileDataList;
    }

    private int getLimitMaxNrOfDays() {
        return meterProtocol.getProperties().getLimitMaxNrOfDays();
    }

    protected void buildProfileData(DataContainer dataContainer, ProfileData profileData, LoadProfileReader lpr, LoadProfileConfiguration lpc) throws IOException {
        boolean currentAdd = true;
        boolean previousAdd = true;
        IntervalData previousIntervalData = null, currentIntervalData;

        if (dataContainer.getRoot().element.length == 0) {
            throw new IOException("No entries in object list.");
        }

        Calendar calendar = ProtocolUtils.initCalendar(false, meterProtocol.getTimeZone());

        for (int i = 0; i < dataContainer.getRoot().element.length; i++) { // for all retrieved intervals

            DataStructure currentStructure = dataContainer.getRoot().getStructure(i);

            if (currentStructure.isStructure(0)) {
                calendar = parseProfileStartDateTime(currentStructure, calendar, lpc); // new date and/or time?
            }

            int intervalStatus = 0;

            // Start of interval
            if (currentStructure.isStructure(0)) {
                currentAdd = parseStart(currentStructure, calendar, profileData, lpc);
                intervalStatus = getEiServerStatus(currentStructure.getStructure(0).getInteger(1));
            }
            // End of interval
            if (currentStructure.isStructure(1)) {
                currentAdd = parseEnd(currentStructure, calendar, profileData, lpc);
                intervalStatus = getEiServerStatus(currentStructure.getStructure(1).getInteger(1));
            }
            // time1
            if (currentStructure.isStructure(2)) {
                currentAdd = parseTime1(currentStructure, calendar, profileData, lpc);
                intervalStatus = getEiServerStatus(currentStructure.getStructure(2).getInteger(1));
            }
            // Time2
            if (currentStructure.isStructure(3)) {
                currentAdd = parseTime2(currentStructure, calendar, profileData, lpc);
                intervalStatus = getEiServerStatus(currentStructure.getStructure(3).getInteger(1));
            }

            // Adjust calendar for interval with profile interval period
            if (currentAdd) {
                calendar.add(Calendar.MINUTE, (lpc.getProfileInterval() / 60));
            }

            currentIntervalData = getIntervalData(currentStructure, calendar, lpr);

            if (currentAdd & !previousAdd) {
                currentIntervalData = addIntervalData(currentIntervalData, previousIntervalData);
            }
            currentIntervalData.setEiStatus(intervalStatus);

            // Add interval data...
            if (currentAdd) {
                profileData.addInterval(currentIntervalData);
            }
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            previousIntervalData = currentIntervalData;
            previousAdd = currentAdd;
        }
    }

    private Calendar parseProfileStartDateTime(DataStructure dataStructure, Calendar calendar, LoadProfileConfiguration lpc) {
        if (isNewDate(dataStructure.getStructure(0).getOctetString(0).getArray()) || isNewTime(dataStructure.getStructure(0).getOctetString(0).getArray())) {
            calendar = setCalendar(calendar, dataStructure.getStructure(0), 0, lpc);
        }
        return calendar;
    }

    private boolean isNewDate(byte[] array) {
        return (array[0] != -1) && (array[1] != -1) && (array[2] != -1) && (array[3] != -1);
    }

    private boolean isNewTime(byte[] array) {
        return (array[5] != -1) && (array[6] != -1) && (array[7] != -1);
    }

    private Calendar setCalendar(Calendar cal, DataStructure dataStructure, int btype, LoadProfileConfiguration lpc) {
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

    private boolean parseStart(DataStructure dataStructure, Calendar calendar, ProfileData profileData, LoadProfileConfiguration lpc) {
        calendar = setCalendar(calendar, dataStructure.getStructure(0), 1, lpc);
        final int startOfIntervalDate = dataStructure.getStructure(0).getInteger(1);

        if ((startOfIntervalDate & EV_CLOCK_SETTINGS) != 0) { // time set before
            profileData.addEvent(new MeterEvent(((Calendar) calendar.clone()).getTime(),
                    MeterEvent.SETCLOCK_AFTER,
                    dataStructure.getStructure(0).getInteger(1)));
        }
        if ((startOfIntervalDate & EV_POWER_FAILURE) != 0) { // power down
            profileData.addEvent(new MeterEvent(((Calendar) calendar.clone()).getTime(),
                    MeterEvent.POWERUP,
                    EV_POWER_FAILURE));
        }
        if ((startOfIntervalDate & EV_WATCHDOG_RESET) != 0) { // watchdog
            profileData.addEvent(new MeterEvent(((Calendar) calendar.clone()).getTime(),
                    MeterEvent.WATCHDOGRESET,
                    EV_WATCHDOG_RESET));
        }
        if ((startOfIntervalDate & EV_DST) != 0) { // watchdog
            profileData.addEvent(new MeterEvent(((Calendar) calendar.clone()).getTime(),
                    MeterEvent.SETCLOCK_AFTER,
                    EV_DST));
        }
        return true;
    }

    private boolean parseEnd(DataStructure dataStructure, Calendar calendar, ProfileData profileData, LoadProfileConfiguration lpc) {
        Calendar endIntervalCal = setCalendar(calendar, dataStructure.getStructure(1), 1, lpc);
        final int endOfIntervalDate = dataStructure.getStructure(1).getInteger(1);

        if ((endOfIntervalDate & EV_CLOCK_SETTINGS) != 0) { // time set before
            profileData.addEvent(new MeterEvent(((Calendar) endIntervalCal.clone()).getTime(),
                    MeterEvent.SETCLOCK_BEFORE,
                    dataStructure.getStructure(1).getInteger(1)));
        }

        if ((endOfIntervalDate & EV_POWER_FAILURE) != 0) { // power down
            profileData.addEvent(new MeterEvent(((Calendar) endIntervalCal.clone()).getTime(),
                    MeterEvent.POWERDOWN,
                    EV_POWER_FAILURE));
            return true; // KV 16012004
        }

        /* No WD event added cause time is set to 00h00'00" */
        if ((endOfIntervalDate & EV_DST) != 0) { // power down
            profileData.addEvent(new MeterEvent(((Calendar) endIntervalCal.clone()).getTime(),
                    MeterEvent.SETCLOCK_BEFORE,
                    EV_DST));
            return true;
        }

        return (lpc.getProfileInterval() * 1000) - (endIntervalCal.getTimeInMillis() - calendar.getTimeInMillis()) <= 2000;
    }

    private boolean parseTime1(DataStructure dataStructure, Calendar calendar, ProfileData profileData, LoadProfileConfiguration lpc) {
        calendar = setCalendar(calendar, dataStructure.getStructure(2), 1, lpc);
        final int time1 = dataStructure.getStructure(2).getInteger(1);

        if ((time1 & EV_CLOCK_SETTINGS) != 0) { // time set before
            profileData.addEvent(new MeterEvent(((Calendar) calendar.clone()).getTime(),
                    MeterEvent.SETCLOCK_BEFORE,
                    dataStructure.getStructure(2).getInteger(1)));
        }

        if ((time1 & EV_POWER_FAILURE) != 0) {// power down
            profileData.addEvent(new MeterEvent(((Calendar) calendar.clone()).getTime(),
                    MeterEvent.POWERDOWN,
                    EV_POWER_FAILURE));
        }
        return true;
    }

    private boolean parseTime2(DataStructure dataStructure, Calendar calendar, ProfileData profileData, LoadProfileConfiguration lpc) {
        calendar = setCalendar(calendar, dataStructure.getStructure(3), 1, lpc);
        final int time2 = dataStructure.getStructure(3).getInteger(1);

        if ((time2 & EV_CLOCK_SETTINGS) != 0) { // time set before
            profileData.addEvent(new MeterEvent(((Calendar) calendar.clone()).getTime(),
                    MeterEvent.SETCLOCK_AFTER,
                    dataStructure.getStructure(3).getInteger(1)));
        }

        if ((time2 & EV_POWER_FAILURE) != 0) {// power down
            profileData.addEvent(new MeterEvent(((Calendar) calendar.clone()).getTime(),
                    MeterEvent.POWERUP,
                    EV_POWER_FAILURE));
        }
        return true;
    }
    private IntervalData addIntervalData(IntervalData currentIntervalData, IntervalData previousIntervalData) {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        int current;
        for (int i = 0; i < currentCount; i++) {
            current = (currentIntervalData.get(i)).intValue() + (previousIntervalData.get(i)).intValue();
            intervalData.addValue(current);
        }
        return intervalData;
    }

    private IntervalData getIntervalData(DataStructure dataStructure, Calendar calendar, LoadProfileReader lpr) throws IOException {
        // Add interval data...
        IntervalData intervalData = new IntervalData(((Calendar) calendar.clone()).getTime());
        List<CapturedObject> captureObjects = meterProtocol.getDlmsSession().getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects();
        for (int i = 0; i < captureObjects.size(); i++) {
            if ((captureObjects.get(i).getClassId() == DLMSClassId.REGISTER.getClassId() ||
                    captureObjects.get(i).getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId() ||
                    captureObjects.get(i).getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()) && isChannelInLpr(lpr, captureObjects.get(i).getObisCode())) {
                intervalData.addValue(dataStructure.getInteger(i));
            }
        }
        return intervalData;
    }

    private boolean isChannelInLpr(LoadProfileReader lpr, ObisCode obisCode) {
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
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && new DeviceIdentifierBySerialNumber(loadProfileReader.getMeterSerialNumber()).equalsIgnoreCase(lpc.getDeviceIdentifier())) {
                return lpc;
            }
        }
        return null;
    }

    protected int getEiServerStatus(int protocolStatus) {
        int status = IntervalStateBits.OK;
        BigInteger protocolStatusBig = BigInteger.valueOf(protocolStatus);

        if (protocolStatusBig.testBit(6)) { // Power failure
            status = status | IntervalStateBits.POWERDOWN;
            status = status | IntervalStateBits.POWERUP;
        }
        if (protocolStatusBig.testBit(5)) { // Clock Settings
            status = status | IntervalStateBits.SHORTLONG;
        }
        if (protocolStatusBig.testBit(3)) { // DST
            status = status | IntervalStateBits.OTHER;
        }
        if (protocolStatusBig.testBit(2)) {
            status = status | IntervalStateBits.WATCHDOGRESET;
        }

        return status;
    }
}
