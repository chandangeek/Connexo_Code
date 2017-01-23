package com.energyict.protocolimpl.dlms.elgama;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.ClockSettingLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.InternalErrorLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.MagneticInfluenceLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.OverCurrentLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.OverVoltageLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.ParameterChangeLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.PhaseChangeLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.PowerFailureLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.PowerOverLimitLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.ReverseCurrentLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.TerminalCoverOpenedLog;
import com.energyict.protocolimpl.dlms.elgama.eventlogging.UnderVoltageLog;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 23-dec-2010
 * Time: 10:32:24
 */
public class ProfileChannel {

    private static final int END_OF_INTEGRATION_PERIOD = 0x800000;
    private static final int START_OF_INTEGRATION_PERIOD = 0x080000;
    private static final int BEFORE_CLOCK_ADJUST = 0x008000;
    private static final int CLEAR_LOAD_PROFILE = 0x004000;
    private static final int POWER_DOWN = 0x000080;
    private static final int POWER_UP = 0x000040;
    private static final int AFTER_CLOCK_ADJUST = 0x000020;
    private static final int BILLING_PERIOD_RESET = 0x000010;
    private static final int DISTORTED_INTEGERATION_PERIOD = 0x000004;

    private static final ObisCode OBIS_CODE_LOAD_PROFILE_1 = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode POWER_FAILURE_EVENT_LOG = ObisCode.fromString("1.0.99.97.0.255");
    private static final ObisCode CHANGE_IN_NUMBER_OF_PHASES_EVENT_LOG = ObisCode.fromString("1.0.99.98.11.255");
    private static final ObisCode OVER_VOLTAGE_EVENT_LOG = ObisCode.fromString("1.0.99.98.12.255");
    private static final ObisCode UNDER_VOLTAGE_EVENT_LOG = ObisCode.fromString("1.0.99.98.13.255");
    private static final ObisCode POWER_OVER_LIMIT_EVENT_LOG = ObisCode.fromString("1.0.99.98.20.255");
    private static final ObisCode REVERSE_CURRENT_EVENT_LOG = ObisCode.fromString("1.0.99.98.21.255");
    private static final ObisCode OVER_CURRENT_EVENT_LOG = ObisCode.fromString("1.0.99.98.22.255");
    private static final ObisCode MAGNETIC_FIELD_INFLUENCE_EVENT_LOG = ObisCode.fromString("1.0.99.98.30.255");
    //private static final ObisCode OPENING_OF_MAIN_COVER_EVENT_LOG = ObisCode.fromString("1.0.99.98.31.255");
    private static final ObisCode OPENING_OF_TERMINAL_COVER_EVENT_LOG = ObisCode.fromString("1.0.99.98.32.255");
    private static final ObisCode CLOCK_SETTING_EVENT_LOG = ObisCode.fromString("1.0.99.98.40.255");
    private static final ObisCode PARAMETERIZATION_EVENT_LOG = ObisCode.fromString("1.0.99.98.41.255");
    private static final ObisCode ERROR_EVENT_LOG = ObisCode.fromString("1.0.99.98.50.255");

    private int profileInterval;
    private CapturedObjectsHelper capturedObjectsHelper;
    private Logger logger;
    private int numberOfChannels = -1;
    private TimeZone timeZone;
    private ProfileGeneric loadProfile;
    private CosemObjectFactory cosemObjectFactory;
    private DLMSMeterConfig meterConfig;

    public ProfileChannel(Logger logger, int profileInterval, ProfileGeneric loadProfile, TimeZone timeZone, CosemObjectFactory cosemObjectFactory, DLMSMeterConfig meterConfig) {
        this.logger = logger;
        this.timeZone = timeZone;
        this.cosemObjectFactory = cosemObjectFactory;
        this.meterConfig = meterConfig;
        this.profileInterval = profileInterval;
        this.loadProfile = loadProfile;
    }

    public ProfileGeneric getLoadProfile() {
        return loadProfile;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public int getProfileInterval() {
        return profileInterval;
    }


    public DLMSMeterConfig getMeterConfig() {
        return meterConfig;
    }


    /**
     * Getter for the scaler unit
     *
     * @param channelId: the channel id for the scaler unit
     * @return the scaler unit
     * @throws java.io.IOException when there's a problem communicating with the meter.
     */
    private ScalerUnit getScalerUnit(final int channelId) throws IOException {
        ScalerUnit unit;
        ObisCode obisCode = getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId);
        obisCode = fixObisCode(obisCode);
        UniversalObject uo = getMeterConfig().findObject(obisCode);
        int classId = uo.getClassID();

        if (classId == DLMSClassId.REGISTER.getClassId()) {
            unit = getCosemObjectFactory().getRegister(obisCode).getScalerUnit();
        } else if (classId == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            unit = getCosemObjectFactory().getDemandRegister(obisCode).getScalerUnit();
        } else if (classId == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            unit = getCosemObjectFactory().getExtendedRegister(obisCode).getScalerUnit();
        } else {
            throw new IllegalArgumentException("G3B, getScalerUnit(), invalid channelId, " + channelId);
        }
        if (unit.getUnitCode() == 0) {
            logger.info("Channel [" + channelId + "] has a scaler unit with unit code [0], using a unitless scalerunit.");
            unit = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
        }
        return unit;
    }

    /**
     * Getter for the number of channels
     *
     * @return the number of channels
     * @throws IOException when there's a problem communicating with the meter.
     */
    public final int getNumberOfChannels() throws IOException {
        if (numberOfChannels == -1) {
            logger.info("Loading the number of channels, looping over the captured objects...");
            numberOfChannels = getCapturedObjectsHelper().getNrOfchannels();
            logger.info("Got [" + this.numberOfChannels + "] actual channels in load profile (out of [" + capturedObjectsHelper.getNrOfCapturedObjects() + "] captured objects)");
        }
        return this.numberOfChannels;
    }

    /**
     * Helper object for the captured objects
     *
     * @return the helper object
     * @throws IOException when there's a problem communicating with the meter.
     */
    private CapturedObjectsHelper getCapturedObjectsHelper() throws IOException {
        if (this.capturedObjectsHelper == null) {
            logger.info("Initializing the CapturedObjectsHelper using the generic profile, profile OBIS code is [" + OBIS_CODE_LOAD_PROFILE_1.toString() + "]");
            final ProfileGeneric profileGeneric = getLoadProfile();
            capturedObjectsHelper = profileGeneric.getCaptureObjectsHelper();
            logger.info("Done, load profile [" + OBIS_CODE_LOAD_PROFILE_1 + "] has [" + capturedObjectsHelper.getNrOfCapturedObjects() + "] captured objects...");
        }
        return this.capturedObjectsHelper;
    }


    /**
     * Changes the nr5 into nr4
     *
     * @param obisCode the obis code that needs to be fixed
     * @return the fixed obis code
     */
    private ObisCode fixObisCode(ObisCode obisCode) {
        if (obisCode.equals(ObisCode.fromString("1.0.4.5.0.255")) ||
        obisCode.equals(ObisCode.fromString("1.0.3.5.0.255")) ||
        obisCode.equals(ObisCode.fromString("1.0.2.5.0.255")) ||
        obisCode.equals(ObisCode.fromString("1.0.1.5.0.255"))) {
            return ProtocolTools.setObisCodeField(obisCode, 3, (byte) 4);
        }
        return obisCode;
    }

    /**
     * Maps the interval end time to a calendar
     *
     * @param previousTime
     * @param cal          the calendar object
     * @param intervalData the intervaldata
     * @return the calendar object
     * @throws IOException when there's a problem communicating with the meter.
     */
    private Calendar mapIntervalEndTimeToCalendar(Calendar previousTime, final Calendar cal, final DataStructure intervalData) throws IOException {
        final Calendar calendar = (Calendar) cal.clone();
        Calendar prevTime = (Calendar) cal.clone();
        if (previousTime != null) {
            prevTime = (Calendar) previousTime.clone();
        }

        //Only hourly values have a timestamp.
        if (!intervalData.isOctetString(0)) {
            prevTime.add(Calendar.SECOND, getProfileInterval());
            return prevTime;
        }

        OctetString octetString = intervalData.getOctetString(0);
        calendar.setTime(octetString.toDate(getTimeZone()));

        // if DST, add 1 hour
        byte[] dateArray = octetString.getArray();
        if (dateArray[11] != -1) {
            if ((dateArray[11] & (byte) 0x80) == 0x80) {
                calendar.add(Calendar.HOUR_OF_DAY, -1);
            }
        }

        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }


    /**
     * Maps the interval state bits to a status for EiServer
     *
     * @param protocolStatus: the interval state bits
     * @return the EiServer status
     */
    private int map2IntervalStateBits(int protocolStatus) {
        int eiStatus = 0;
        if ((protocolStatus & END_OF_INTEGRATION_PERIOD) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & START_OF_INTEGRATION_PERIOD) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & BEFORE_CLOCK_ADJUST) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & POWER_DOWN) != 0) {
            eiStatus |= IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatus & POWER_UP) != 0) {
            eiStatus |= IntervalStateBits.POWERUP;
        }
        if ((protocolStatus & AFTER_CLOCK_ADJUST) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & BILLING_PERIOD_RESET) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & DISTORTED_INTEGERATION_PERIOD) != 0) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatus & CLEAR_LOAD_PROFILE) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        return eiStatus;
    }

    /**
     * @param from:          the from date
     * @param to:            the to date
     * @param includeEvents: boolean, whether or not to include the events in the profile
     * @return the profile data
     * @throws IOException when there's a problem communicating with the meter.
     */
    public ProfileData getProfileData(final Calendar from, final Calendar to, final boolean includeEvents) throws IOException {
        logger.info("Loading profile data starting at [" + from.getTime().toString() + "], ending at [" + to.getTime().toString() + "], " + (includeEvents ? "" : "not") + " including events");
        final ProfileData profileData = new ProfileData();
        final DataContainer datacontainer = getLoadProfile().getBuffer(from, to);

        logger.info("Building channel information...");
        for (int i = 0; i < this.getNumberOfChannels(); i++) {
            final ScalerUnit scalerUnit = this.getScalerUnit(i);
            logger.info("Scaler unit for channel [" + i + "] is [" + scalerUnit + "]");
            final ChannelInfo channelInfo = new ChannelInfo(i, "G3B_" + i, scalerUnit.getEisUnit());
            final CapturedObject channelCapturedObject = getCapturedObjectsHelper().getProfileDataChannelCapturedObject(i);
            if (ParseUtils.isObisCodeCumulative(channelCapturedObject.getLogicalName().getObisCode())) {
                logger.info("Indicating that channel [" + i + "] is cumulative...");
                channelInfo.setCumulative();
            }
            profileData.addChannel(channelInfo);
        }

        logger.info("Building profile data...");
        final Object[] loadProfileEntries = datacontainer.getRoot().getElements();
        if (loadProfileEntries.length == 0) {
            logger.log(Level.INFO, "There are no entries in the load profile, nothing to build...");
        } else {
            logger.log(Level.INFO, "Got [" + datacontainer.getRoot().element.length + "] entries in the load profile, building profile data...");
            Calendar previousTimeStamp = null;
            int[] extra = new int[getNumberOfChannels()];
            boolean interValue = false;
            int nrOfCapturedObjects = getCapturedObjectsHelper().getNrOfCapturedObjects();
            int nrOfOtherChannels = nrOfCapturedObjects - getNumberOfChannels();

            for (int i = 0; i < loadProfileEntries.length; i++) {
                logger.info("Processing interval [" + i + "]");
                final DataStructure intervalData = datacontainer.getRoot().getStructure(i);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Mapping interval end time...");
                }
                Calendar calendar = ProtocolUtils.initCalendar(getTimeZone().inDaylightTime(new Date()), getTimeZone());     //TODO: DST
                calendar = this.mapIntervalEndTimeToCalendar(previousTimeStamp, calendar, intervalData);

                final int protocolStatus;
                if (intervalData.isOctetString(1)) {
                    protocolStatus = ProtocolTools.getIntFromBytes(intervalData.getOctetString(1).getArray());
                } else if (intervalData.isInteger(1)) {
                    protocolStatus = intervalData.getInteger(1);
                } else {
                    throw new IOException("Unrecognized protocol status. Expected integer or octet string.");
                }
                final int eiStatus = this.map2IntervalStateBits(protocolStatus);

                final IntervalData data = new IntervalData(new Date(((Calendar) calendar.clone()).getTime().getTime()), eiStatus, protocolStatus);
                logger.info("Adding channel data.");
                for (int j = 0; j < nrOfCapturedObjects; j++) {
                    if (getCapturedObjectsHelper().isChannelData(j)) {
                        if (isValidTimeStamp(calendar)) {
                            //Add previous value to current value (extra) if the previous value was a the most recent "inter value".
                            if ((previousTimeStamp == null) || (!isValidTimeStamp(previousTimeStamp) && isRecentStamp(previousTimeStamp, calendar))) {
                                data.addValue(extra[j - nrOfOtherChannels] + intervalData.getInteger(j));
                            } else {
                                data.addValue(intervalData.getInteger(j));
                            }
                            extra[j - nrOfOtherChannels] = 0;
                            interValue = false;
                        } else {
                            //Else it's a value between 2 intervals (stored due to an unexpected event).
                            //Save the value, add it with the next value.
                            if (!isRecentStamp(previousTimeStamp, calendar)) {
                                extra[j - nrOfOtherChannels] = 0;
                            }
                            extra[j - nrOfOtherChannels] += intervalData.getInteger(j);
                            interValue = true;
                        }
                    }
                }

                previousTimeStamp = (Calendar) calendar.clone();
                if (!interValue && (data.getValueCount() == getNumberOfChannels())) {
                    profileData.addInterval(data);
                }
            }
        }
        if (includeEvents) {
            logger.info("Requested to include meter events, loading...");
            profileData.setMeterEvents(getMeterEvents(from, to));
        }
        return profileData;
    }

    /**
     * Checks if a given previous stamp is recent. (not older than 15 minutes from given calendar stamp)
     *
     * @param previousTimeStamp
     * @param calendar
     * @return
     */
    private boolean isRecentStamp(Calendar previousTimeStamp, Calendar calendar) {
        Calendar recentStamp = (Calendar) calendar.clone();
        recentStamp.add(Calendar.MINUTE, -1 * getProfileInterval() / 60); //Go back 15 min
        if (previousTimeStamp == null) {
            return false;
        }
        return (previousTimeStamp.after(recentStamp)) && (previousTimeStamp.before(calendar));
    }

    /**
     * Checks if this is a valid time stamp for the current profile interval.
     * E.g. an invalid time stamp can be 14:46:20, while 14:45:00 is a valid one.
     *
     * @param calendar: the given time stamp
     * @return boolean
     */
    private boolean isValidTimeStamp(Calendar calendar) {
        if ((calendar.get(Calendar.MINUTE) % (getProfileInterval() / 60)) == 0) {
            if (calendar.get(Calendar.SECOND) < 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for the meter events. Used while requesting the profile data (if the boolean includeEvents is true)
     *
     * @param from: the from date
     * @param to:   the to date
     * @return a list of meter events
     * @throws IOException when there's a problem communicating with the meter.
     */
    private List<MeterEvent> getMeterEvents(final Calendar from, final Calendar to) throws IOException {
        logger.info("Fetching meter events from [" + (from != null ? from.getTime() : "Not specified") + "] to [" + (to != null ? to.getTime() : "Not specified") + "]");

        final DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(POWER_FAILURE_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcPhaseChanges = getCosemObjectFactory().getProfileGeneric(CHANGE_IN_NUMBER_OF_PHASES_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcOverVoltage = getCosemObjectFactory().getProfileGeneric(OVER_VOLTAGE_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcUnderVoltage = getCosemObjectFactory().getProfileGeneric(UNDER_VOLTAGE_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcPowerOverLimit = getCosemObjectFactory().getProfileGeneric(POWER_OVER_LIMIT_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcReverseCurrent = getCosemObjectFactory().getProfileGeneric(REVERSE_CURRENT_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcOverCurrent = getCosemObjectFactory().getProfileGeneric(OVER_CURRENT_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcMagneticInfluence = getCosemObjectFactory().getProfileGeneric(MAGNETIC_FIELD_INFLUENCE_EVENT_LOG).getBuffer(from, to);
        //final DataContainer dcCoverOpen = getCosemObjectFactory().getProfileGeneric(OPENING_OF_MAIN_COVER_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcTerminalOpen = getCosemObjectFactory().getProfileGeneric(OPENING_OF_TERMINAL_COVER_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcClockSetting = getCosemObjectFactory().getProfileGeneric(CLOCK_SETTING_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcParameterChange = getCosemObjectFactory().getProfileGeneric(PARAMETERIZATION_EVENT_LOG).getBuffer(from, to);
        final DataContainer dcInternalError = getCosemObjectFactory().getProfileGeneric(ERROR_EVENT_LOG).getBuffer(from, to);

        final PowerFailureLog eventLog1 = new PowerFailureLog(getTimeZone(), dcPowerFailure);
        final PhaseChangeLog eventLog2 = new PhaseChangeLog(getTimeZone(), dcPhaseChanges);
        final OverVoltageLog eventLog3 = new OverVoltageLog(getTimeZone(), dcOverVoltage);
        final UnderVoltageLog eventLog4 = new UnderVoltageLog(getTimeZone(), dcUnderVoltage);
        final PowerOverLimitLog eventLog5 = new PowerOverLimitLog(getTimeZone(), dcPowerOverLimit);
        final ReverseCurrentLog eventLog6 = new ReverseCurrentLog(getTimeZone(), dcReverseCurrent);
        final OverCurrentLog eventLog7 = new OverCurrentLog(getTimeZone(), dcOverCurrent);
        final MagneticInfluenceLog eventLog8 = new MagneticInfluenceLog(getTimeZone(), dcMagneticInfluence);
        //final MeterCoverOpenedLog eventLog9 = new MeterCoverOpenedLog(getTimeZone(), dcCoverOpen);
        final TerminalCoverOpenedLog eventLog10 = new TerminalCoverOpenedLog(getTimeZone(), dcTerminalOpen);
        final ClockSettingLog eventLog11 = new ClockSettingLog(getTimeZone(), dcClockSetting);
        final ParameterChangeLog eventLog12 = new ParameterChangeLog(getTimeZone(), dcParameterChange);
        final InternalErrorLog eventLog13 = new InternalErrorLog(getTimeZone(), dcInternalError);

        final List<MeterEvent> events = new ArrayList<MeterEvent>();
        events.addAll(eventLog1.getMeterEvents());
        events.addAll(eventLog2.getMeterEvents());
        events.addAll(eventLog3.getMeterEvents());
        events.addAll(eventLog4.getMeterEvents());
        events.addAll(eventLog5.getMeterEvents());
        events.addAll(eventLog6.getMeterEvents());
        events.addAll(eventLog7.getMeterEvents());
        events.addAll(eventLog8.getMeterEvents());
        //events.addAll(eventLog9.getMeterEvents());
        events.addAll(eventLog10.getMeterEvents());
        events.addAll(eventLog11.getMeterEvents());
        events.addAll(eventLog12.getMeterEvents());
        events.addAll(eventLog13.getMeterEvents());

        return events;
    }
}