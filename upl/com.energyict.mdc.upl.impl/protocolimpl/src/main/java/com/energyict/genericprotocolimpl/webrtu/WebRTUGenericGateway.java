package com.energyict.genericprotocolimpl.webrtu;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.ConfigurationSupport;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.*;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.mdw.amr.*;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.CommunicationProfileShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 18/01/12
 * Time: 15:03
 */
public class WebRTUGenericGateway implements GenericProtocol {

    private static final String FRIENDLY_NAME = "MUC___";
    private static final int HOUR = 3600000;

    private long startTime = -1;
    private long timeDiff = -1;
    private Logger logger;
    private Properties properties = null;
    private List<List<CommunicationProfileAction>> infosForSlaveSchedules = new ArrayList<List<CommunicationProfileAction>>();
    private MeterProtocol meterProtocol = null;
    private MeterReadingData meterReadingData = null;
    private ProfileData profileData = null;
    private WavenisStack wavenisStack = null;
    private WaveModuleLinkAdaptor waveModuleLinkAdaptor;
    private List<Integer> failedSchedulesIds = new ArrayList<Integer>();
    private List<Integer> successfulSchedulesIds = new ArrayList<Integer>();
    private List<RegisterGroup> rtuRegisterGroups;

    /**
     * Necessary to know which communication schedules should be journal'ed
     */
    private static enum CommunicationProfileAction {
        PROFILEDATA,
        REGISTERS,
        WRITECLOCK,
        FORCECLOCK,
        MESSAGES
    }

    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        Device rtu = scheduler.getRtu();
        List<Device> slaves = rtu.getDownstreamDevices(); //Should be a collection of WaveFlow, WaveTherm, RTM, ... devices
        setLogger(logger);
        for (Device slave : slaves) {
            List<CommunicationScheduler> inboundSlaveSchedules = getInboundSlaveSchedules(slave);
            try {
                meterReadingData = null;
                profileData = null;
                meterProtocol = null;
                timeDiff = -1;

                if (inboundSlaveSchedules.size() == 0) {
                    getLogger().info("No active communication schedules for slave " + slave.getSerialNumber() + ", skipping");
                    continue;
                }
                CommunicationProfileShadow fullSlaveSchedule = createFullSlaveSchedule(inboundSlaveSchedules);

                RegisterProtocol registerProtocol = null;
                MessageProtocol messageProtocol = null;

                ConfigurationSupport configurationSupport = slave.getOldProtocol().newInstance();
                if (configurationSupport instanceof RegisterProtocol) {
                    registerProtocol = (RegisterProtocol) configurationSupport;
                }
                if (configurationSupport instanceof MessageProtocol) {
                    messageProtocol = (MessageProtocol) configurationSupport;
                }

                try {
                    if (configurationSupport instanceof MeterProtocol) {
                        meterProtocol = (MeterProtocol) configurationSupport;
                        initAndConnect(link, logger, slave);
                    } else {
                        meterProtocol = null;
                        String msg = "Protocol for RTU with serial number " + slave.getSerialNumber() + " must implement the MeterProtocol interface";
                        logError(inboundSlaveSchedules, msg, AmrJournalEntry.CC_PROTOCOLERROR);    //All schedules fail here
                        continue;    //A continue will move on to the next slave device
                    }
                } catch (IOException e) {
                    meterProtocol = null;
                    logError(inboundSlaveSchedules, e.getMessage(), AmrJournalEntry.CC_NOCONNECTION);           //All schedules fail here
                    continue;
                }

                getLogger().log(Level.FINE, "Protocol version: " + meterProtocol.getProtocolVersion());
                try {
                    getLogger().log(Level.FINE, "Meter firmware version: " + meterProtocol.getFirmwareVersion());
                } catch (UnsupportedException e) {
                    getLogger().info(e.getMessage());         //Move on
                } catch (IOException e) {
                    logError(inboundSlaveSchedules, e.getMessage(), AmrJournalEntry.CC_IOERROR);                //All schedules fail here
                    continue;
                }

                //Force clock
                if (fullSlaveSchedule.getForceClock()) {
                    try {
                        meterProtocol.setTime();
                        rememberSchedulesAsSuccessful(inboundSlaveSchedules, CommunicationProfileAction.FORCECLOCK);
                        timeDiff = -1;
                    } catch (IOException e) {
                        logError(inboundSlaveSchedules, e.getMessage(), CommunicationProfileAction.FORCECLOCK, AmrJournalEntry.CC_IOERROR);
                        continue;
                    }
                }

                //Profile data
                if (fullSlaveSchedule.getReadDemandValues()) {
                    int eiServerProfileInterval = getProfileInterval(getProperties());
                    int meterNrOfChannels = meterProtocol.getNumberOfChannels();
                    int eiServerNumberOfChannels = slave.getShadow().getChannelShadows().size();
                    int meterProfileInterval = meterProtocol.getProfileInterval();
                    if (meterProfileInterval != eiServerProfileInterval) {
                        String msg = "Profile interval setting in EiServer configuration (" + eiServerProfileInterval + " sec) is different than requested from the meter (" + meterProfileInterval + " sec)";
                        logError(inboundSlaveSchedules, msg, CommunicationProfileAction.PROFILEDATA, AmrJournalEntry.CC_CONFIGURATION);
                        rememberSchedulesAsFailed(inboundSlaveSchedules, CommunicationProfileAction.PROFILEDATA);
                    } else if (meterNrOfChannels != eiServerNumberOfChannels) {
                        String msg = "Number of channels configured in eiserver (" + eiServerNumberOfChannels + ") is different than the number of channels on the meter (" + meterNrOfChannels + ")";
                        logError(inboundSlaveSchedules, msg, CommunicationProfileAction.PROFILEDATA, AmrJournalEntry.CC_CONFIGURATION);
                        rememberSchedulesAsFailed(inboundSlaveSchedules, CommunicationProfileAction.PROFILEDATA);
                    } else {
                        Date validatedLastReading = validateLastReading(slave.getLastReading(), slave.getTimeZone());
                        if (validatedLastReading == null) {
                            String msg = "Last reading is null!";
                            logError(inboundSlaveSchedules, msg, CommunicationProfileAction.PROFILEDATA, AmrJournalEntry.CC_CONFIGURATION);
                            rememberSchedulesAsFailed(inboundSlaveSchedules, CommunicationProfileAction.PROFILEDATA);
                        } else {
                            try {
                                getLogger().log(Level.INFO, "Retrieving profile data from " + validatedLastReading + " to " + new Date());
                                profileData = meterProtocol.getProfileData(slave.getLastReading(), validatedLastReading, fullSlaveSchedule.getReadMeterEvents());
                                if (profileData != null) {
                                    validateProfileData(profileData);
                                    profileData.sort();

                                    boolean markAsBadTime = false;
                                    long differenceInMillis = getTimeDifference();
                                    if (differenceInMillis > (fullSlaveSchedule.getMaximumClockDifference())) {
                                        if (!fullSlaveSchedule.getForceClock()) {
                                            if (!fullSlaveSchedule.getCollectOutsideBoundary()) {
                                                String msg = "Time difference exceeds configured maximum: " + (differenceInMillis / 1000) + " s >" + fullSlaveSchedule.getMaximumClockDifference() + " s";
                                                logError(inboundSlaveSchedules, msg, AmrJournalEntry.CC_TIME_ERROR);
                                                continue;
                                            } else {
                                                markAsBadTime = true;
                                            }
                                        }
                                    }

                                    if (markAsBadTime) {
                                        profileData.markIntervalsAsBadTime();
                                    }
                                    rememberSchedulesAsSuccessful(inboundSlaveSchedules, CommunicationProfileAction.PROFILEDATA);
                                } else {
                                    String msg = "No profile data available, data logging possibly not started..";
                                    logError(inboundSlaveSchedules, msg, CommunicationProfileAction.PROFILEDATA, AmrJournalEntry.CC_UNEXPECTED_ERROR);
                                    rememberSchedulesAsFailed(inboundSlaveSchedules, CommunicationProfileAction.PROFILEDATA);
                                }
                            } catch (IOException e) {
                                logError(inboundSlaveSchedules, e.getMessage(), CommunicationProfileAction.PROFILEDATA, AmrJournalEntry.CC_IOERROR);
                                continue;
                            }
                        }
                    }
                }

                //Registers
                if (fullSlaveSchedule.getReadMeterReadings()) {
                    if (registerProtocol != null) {
                        List<Register> registers = getScheduledRegisters(slave.getRegisters());
                        meterReadingData = new MeterReadingData();
                        StringBuilder sb = new StringBuilder();
                        String separator = "";
                        try {
                            for (Register register : registers) {
                                try {
                                    RegisterValue registerValue = registerProtocol.readRegister(getCorrectedObisCode(register));
                                    registerValue.setRtuRegisterId(register.getId());
                                    meterReadingData.add(registerValue);
                                } catch (NoSuchRegisterException e) {
                                    String obisCode = getCorrectedObisCode(register).toString();
                                    getLogger().severe("Register with obiscode " + obisCode + " is not supported: " + e.getMessage());
                                    sb.append(separator).append(obisCode);
                                    separator = ", ";
                                }
                            }
                        } catch (IOException e) {
                            logError(inboundSlaveSchedules, e.getMessage(), CommunicationProfileAction.REGISTERS, AmrJournalEntry.CC_IOERROR);
                            continue;
                        }
                        if (sb.toString() != null && !sb.toString().equals("")) {
                            logError(inboundSlaveSchedules, "Unsupported registers: " + sb.toString(), CommunicationProfileAction.REGISTERS, AmrJournalEntry.CC_CONFIGURATION);
                            rememberSchedulesAsFailed(inboundSlaveSchedules, CommunicationProfileAction.REGISTERS);
                        } else {
                            rememberSchedulesAsSuccessful(inboundSlaveSchedules, CommunicationProfileAction.REGISTERS);
                        }
                    } else {
                        String msg = "Couldn't read the registers for RTU with serial number " + slave.getSerialNumber() + ", the protocol should implement the RegisterProtocol interface";
                        logProtocolError(inboundSlaveSchedules, msg, CommunicationProfileAction.REGISTERS);
                        rememberSchedulesAsFailed(inboundSlaveSchedules, CommunicationProfileAction.REGISTERS);
                    }
                }

                //Write clock
                if (fullSlaveSchedule.getWriteClock()) {
                    long diff = getTimeDifference();
                    int max = fullSlaveSchedule.getMaximumClockDifference();
                    int min = fullSlaveSchedule.getMinimumClockDifference();
                    if (diff > max) {
                        String msg = "Will not write clock, time difference is greater than maximum difference (" + diff + " > " + max + ")";
                        logError(inboundSlaveSchedules, msg, CommunicationProfileAction.WRITECLOCK, AmrJournalEntry.CC_TIME_ERROR);
                        rememberSchedulesAsFailed(inboundSlaveSchedules, CommunicationProfileAction.WRITECLOCK);
                    } else if (diff < min) {
                        String msg = "Will not write clock, time difference is smaller than minimum difference (" + diff + " < " + min + ")";
                        logError(inboundSlaveSchedules, msg, CommunicationProfileAction.WRITECLOCK, AmrJournalEntry.CC_TIME_ERROR);
                        rememberSchedulesAsFailed(inboundSlaveSchedules, CommunicationProfileAction.WRITECLOCK);
                    } else {
                        try {
                            meterProtocol.setTime();
                            rememberSchedulesAsSuccessful(inboundSlaveSchedules, CommunicationProfileAction.WRITECLOCK);
                            timeDiff = -1;
                        } catch (IOException e) {
                            logError(inboundSlaveSchedules, e.getMessage(), CommunicationProfileAction.WRITECLOCK, AmrJournalEntry.CC_IOERROR);
                            continue;
                        }
                    }
                }

                //Send messages
                if (fullSlaveSchedule.getSendRtuMessage()) {
                    if (messageProtocol != null) {
                        List<OldDeviceMessage> messages = getPendingMessages(slave);
                        try {
                            messageProtocol.applyMessages(messages);
                            for (OldDeviceMessage message : messages) {
                                MessageResult messageResult = messageProtocol.queryMessage(new MessageEntry(message.getContents(), message.getTrackingId()));
                                if (messageResult.isSuccess()) {
                                    message.confirm();
                                }
                                if (messageResult.isFailed()) {
                                    message.setFailed();
                                }
                                if (messageResult.isQueued()) {
                                    message.setSent();
                                }
                                if (messageResult.isUnknown()) {
                                    message.setIndoubt();
                                }
                            }
                            rememberSchedulesAsSuccessful(inboundSlaveSchedules, CommunicationProfileAction.MESSAGES);
                        } catch (IOException e) {
                            logError(inboundSlaveSchedules, e.getMessage(), CommunicationProfileAction.MESSAGES, AmrJournalEntry.CC_IOERROR);
                            continue;
                        }
                    } else {
                        String msg = "Couldn't execute messages for RTU with serial number " + slave.getSerialNumber() + ", the protocol should implement the MessageProtocol interface";
                        logProtocolError(inboundSlaveSchedules, msg, CommunicationProfileAction.MESSAGES);
                        rememberSchedulesAsFailed(inboundSlaveSchedules, CommunicationProfileAction.MESSAGES);
                    }
                }
            } catch (IOException e) {
                logError(inboundSlaveSchedules, e.getMessage(), AmrJournalEntry.CC_IOERROR);
            } finally {
                try {
                    if (meterProtocol != null) {
                        meterProtocol.release();
                        meterProtocol.disconnect();
                        getLogger().info("Meter protocol disconnected");
                        storeData(slave);
                        logSuccess(inboundSlaveSchedules, slave);       //Log a success to the successful schedules.
                    }
                } catch (IOException e) {
                    logError(inboundSlaveSchedules, e.getMessage(), AmrJournalEntry.CC_IOERROR);
                }
            }
        }
        stopWavenisStack();
        timeDiff = 0;
    }

    private List<OldDeviceMessage> getPendingMessages(Device slave) {
        List<OldDeviceMessage> newMessages = new ArrayList<OldDeviceMessage>();
        for (OldDeviceMessage rtuMessage : slave.getOldMessages()) {
            if (rtuMessage.isPending()) {
                newMessages.add(rtuMessage);
            }
        }
        return newMessages;
    }

    /**
     * Returns a list of the slave registers that are meant to be read on this communication schedule
     *
     * @param allSlaveRegisters list of all slave registers
     * @return list of the relevant registers
     */
    private List<Register> getScheduledRegisters(List<Register> allSlaveRegisters) {
        List<Register> relevantRegisters = new ArrayList<Register>();
        for (Register slaveRegister : allSlaveRegisters) {
            if (CommonUtils.isInRegisterGroup(rtuRegisterGroups, slaveRegister)) {
                relevantRegisters.add(slaveRegister);
            }
        }
        return relevantRegisters;
    }

    private ObisCode getCorrectedObisCode(Register slaveRegister) {
        ObisCode obisCode = slaveRegister.getRegisterSpec().getDeviceObisCode();
        if (obisCode == null) {
            obisCode = slaveRegister.getRegisterMapping().getObisCode();
            return ProtocolTools.setObisCodeField(obisCode, 1, (byte) (slaveRegister.getRegisterSpec().getDeviceChannelIndex() & 0x0FF));
        } else {
            return obisCode;
        }
    }

    private void stopWavenisStack() {
        if (wavenisStack != null) {
            wavenisStack.stop();
        }
    }

    private void storeData(Device slave) throws SQLException, BusinessException {
        if (meterReadingData != null) {
            slave.store(meterReadingData);
        }
        if (profileData != null) {
            slave.store(profileData);
        }
    }

    protected void validateProfileData(ProfileData profileData) {
        Iterator it = profileData.getIntervalDatas().iterator();
        while (it.hasNext()) {
            IntervalData ivdt = (IntervalData) it.next();
            if (ivdt.getEndTime().after(new Date())) {
                it.remove();
            }
        }
    }

    /**
     * Remember that this slave schedule failed. A success should not be scheduled on this schedule, during this communication session.
     * The other slave schedules can still be executed and be journal'ed as success
     */
    private void rememberSchedulesAsFailed(List<CommunicationScheduler> allInboundSlaveSchedules, CommunicationProfileAction action) {
        List<CommunicationScheduler> relevantSchedules = getRelevantSchedulesForAction(allInboundSlaveSchedules, action);
        for (CommunicationScheduler communicationScheduler : relevantSchedules) {
            int id = communicationScheduler.getId();
            if (!failedSchedulesIds.contains(id)) {
                failedSchedulesIds.add(id);
            }
        }
    }

    /**
     * Remember that this action (and the relevant schedules) was successful.
     * At the end of the communication session, the schedule will be logged as successful, except when another action of said schedule failed.
     */
    private void rememberSchedulesAsSuccessful(List<CommunicationScheduler> allInboundSlaveSchedules, CommunicationProfileAction action) {
        List<CommunicationScheduler> relevantSchedules = getRelevantSchedulesForAction(allInboundSlaveSchedules, action);
        for (CommunicationScheduler communicationScheduler : relevantSchedules) {
            int id = communicationScheduler.getId();
            if (!successfulSchedulesIds.contains(id)) {
                successfulSchedulesIds.add(id);
            }
        }
    }

    private Date validateLastReading(Date lastReading, TimeZone timeZone) throws BusinessException {
        if (lastReading == null) {
            return null;
        }
        Date testdate;
        if (timeZone.inDaylightTime(lastReading)) {
            testdate = new Date(lastReading.getTime() + HOUR);
            if (timeZone.inDaylightTime(testdate)) {
                return lastReading;
            } else {
                return new Date(lastReading.getTime() - HOUR);
            }
        } else {
            testdate = new Date(lastReading.getTime() - HOUR);
        }

        if (timeZone.inDaylightTime(testdate)) {
            return new Date(testdate.getTime() - HOUR);
        } else {
            return lastReading;
        }
    }

    private void initAndConnect(Link link, Logger logger, Device slave) throws IOException {
        storeStartTime();
        meterProtocol.setProperties(getProperties(slave));

        if (wavenisStack == null) {
            startTheWavenisStack(link.getInputStream(), link.getOutputStream());
        }
        connectOverWavenis();

        meterProtocol.init(waveModuleLinkAdaptor.getInputStream(), waveModuleLinkAdaptor.getOutputStream(), getTimeZone(slave), logger);
        meterProtocol.connect();
        getLogger().info("Meter protocol connected to device with RF radio address " + getNodeId());
    }

    private String getNodeId() throws IOException {
        String nodeId = getProperties().getProperty(MeterProtocol.NODEID);
        if (nodeId == null || nodeId.equals("")) {
            throw new IOException("Node ID is missing, should contain the wavenis RF address!");
        }
        return nodeId;
    }

    public void connectOverWavenis() throws IOException {
        String strDialAddress1 = getNodeId();
        int iTimeout = getProtocolTimeout();

        // create link
        // strDialAddress1 radioaddress[_repeater1][,repeater2][,repeater3]
        // 001122334455_001122334455,001122334455,001122334455

        String[] temp = strDialAddress1.split("_");
        String radioAddress = temp[0];
        String[] repeaterRadioAddresses = null;

        if (temp.length > 1) {
            repeaterRadioAddresses = temp[1].split(",");
        }

        if (radioAddress.length() != 12) {
            throw new IOException("WavenisRFdialer: Invalid radio address [" + radioAddress + "]");
        } else {
            try {
                Long.parseLong(radioAddress, 16);
            } catch (NumberFormatException e) {
                throw new IOException("WavenisRFdialer: Invalid radio address [" + radioAddress + "]");
            }
            if (repeaterRadioAddresses != null) {
                for (String repeaterRadioAddress : repeaterRadioAddresses) {
                    if (repeaterRadioAddress.length() != 12) {
                        throw new IOException("WavenisRFdialer: Invalid radio repeater address [" + repeaterRadioAddress + "]");
                    }
                    try {
                        Long.parseLong(repeaterRadioAddress, 16);
                    } catch (NumberFormatException e) {
                        throw new IOException("WavenisRFdialer: Invalid radio repeater address [" + repeaterRadioAddress + "]");
                    }
                }
            }
        }

        // here repeater radio addresses are validated

        if (wavenisStack != null) {

            WaveModule waveModule = wavenisStack.getWaveModuleFactory().find(radioAddress);
            waveModuleLinkAdaptor = new WaveModuleLinkAdaptor();

            if (repeaterRadioAddresses != null) {
                waveModule.changeRoute(repeaterRadioAddresses);
            }

            waveModule.setConfigRFResponseTimeoutInMs(iTimeout);
            waveModuleLinkAdaptor.init(waveModule);
        } else {
            throw new IOException("WavenisRFdialer: connect(...), WavenisStack not started,...");
        }
    }

    private final void startTheWavenisStack(InputStream inputStream, OutputStream outputStream) throws IOException {
        wavenisStack = WavenisStackImpl.getInstance(FRIENDLY_NAME, 1, null);
        wavenisStack.start(inputStream, outputStream);

        logger.info("WavenisRFdialer: stop the network management layer (not needed in case of dialer usage)");
        wavenisStack.getNetworkManagement().stop();

        logger.info("WavenisRFdialer: initialize root with friendly name [" + FRIENDLY_NAME + "]");
        try {
            wavenisStack.getWaveCard().initializeRoot(FRIENDLY_NAME, 1);
        } catch (WavenisParameterException e) {
            logger.warning("WavenisRFdialer: initialize root with friendly name [" + FRIENDLY_NAME + "] failed, " + e.getMessage());
        }

        logger.info("WavenisRFdialer: set the relay route status true");
        try {
            wavenisStack.getWaveCard().activateRelayRouteStatus(true);
        } catch (WavenisParameterException e) {
            logger.warning("WavenisRFdialer: set the relay route status true failed, " + e.getMessage());
        }

        try {
            logger.info("WavenisRFdialer: Wavecard RTC = " + wavenisStack.getSettingParameterCommandFactory().readRealtimeClock());
        } catch (WavenisParameterException e) {
            logger.warning("WavenisRFdialer: read wavecard clock failed, " + e.getMessage());
        }

        try {
            wavenisStack.getWaveCard().syncTimeIfNeeded();
        } catch (IOException e) {
            logger.warning("WavenisRFdialer: sync wavecard clock failed, " + e.getMessage());
        }
    }

    private CommunicationProfileShadow createFullSlaveSchedule(List<CommunicationScheduler> inboundSlaveSchedules) throws BusinessException, SQLException {
        rtuRegisterGroups = new ArrayList<RegisterGroup>();
        infosForSlaveSchedules = new ArrayList<List<CommunicationProfileAction>>();
        if (inboundSlaveSchedules.size() == 0) {
            return null;
        }
        CommunicationProfileShadow shadow = inboundSlaveSchedules.get(0).getCommunicationProfile().getShadow();

        for (CommunicationScheduler inboundSlaveSchedule : inboundSlaveSchedules) {
            List<CommunicationProfileAction> infoForSlaveSchedule = new ArrayList<CommunicationProfileAction>();
            if (inboundSlaveSchedule.getNextCommunication() == null) {
                getLogger().info("Slave: " + inboundSlaveSchedule.getRtu().getSerialNumber() + ", communication scheduler '" + inboundSlaveSchedule.displayString() + "' next communication is 'null'. Skipping.");
            } else if (inboundSlaveSchedule.getNextCommunication().after(new Date())) {
                getLogger().info("Slave: " + inboundSlaveSchedule.getRtu().getSerialNumber() + ", communication scheduler '" + inboundSlaveSchedule.displayString() + "' next communication not reached yet. Skipping.");
            } else {
                inboundSlaveSchedule.startCommunication();
                CommunicationProfile communicationProfile = inboundSlaveSchedule.getCommunicationProfile();
                if (communicationProfile.getReadDemandValues()) {     //Profile data
                    infoForSlaveSchedule.add(CommunicationProfileAction.PROFILEDATA);                //TODO readAllProfileData ? :S
                    shadow.setReadDemandValues(true);
                }
                if (communicationProfile.getReadMeterReadings()) {    //Registers
                    infoForSlaveSchedule.add(CommunicationProfileAction.REGISTERS);
                    rtuRegisterGroups.addAll(communicationProfile.getRtuRegisterGroups());           //Remember which registers are on this schedule
                    shadow.setReadMeterReadings(true);
                }
                if (communicationProfile.getReadMeterEvents()) {      //Events
                    infoForSlaveSchedule.add(CommunicationProfileAction.PROFILEDATA);
                    shadow.setReadMeterEvents(true);
                }
                if (communicationProfile.getSendRtuMessage()) {       //Messages
                    infoForSlaveSchedule.add(CommunicationProfileAction.MESSAGES);
                    shadow.setSendRtuMessage(true);
                }
                if (communicationProfile.getWriteClock()) {
                    infoForSlaveSchedule.add(CommunicationProfileAction.WRITECLOCK);
                    shadow.setWriteClock(true);
                    shadow.setMaximumClockDifference(communicationProfile.getMaximumClockDifference());
                    shadow.setMinimumClockDifference(communicationProfile.getMinimumClockDifference());
                }
                if (communicationProfile.getForceClock()) {
                    infoForSlaveSchedule.add(CommunicationProfileAction.FORCECLOCK);
                    shadow.setForceClock(true);
                }
                infosForSlaveSchedules.add(infoForSlaveSchedule);
            }
        }

        return shadow;
    }

    private void storeStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    private int getProfileInterval(Properties properties) throws IOException {
        try {
            return (Integer.parseInt(properties.getProperty("ProfileInterval", "900").trim())); // configured profile interval in seconds
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing the profile interval: " + e.getMessage());
        }
    }

    private int getProtocolTimeout() throws IOException {
        try {
            return (Integer.parseInt(getProperties().getProperty("Timeout", "5000").trim())); // configured profile interval in seconds
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing the timeout: " + e.getMessage());
        }
    }

    private TimeZone getTimeZone(Device slave) {
        TimeZone timeZone = slave.getTimeZone();
        if (timeZone == null) {
            getLogger().warning("Using system timezone, device timezone is null!");
            timeZone = TimeZone.getDefault();
        }
        return timeZone;
    }

    private Properties getProperties(Device slave) {
        properties = slave.getProperties().toStringProperties();
        Properties protocolProperties = slave.getOldProtocol().getProperties().toStringProperties();

        for (Object keyObject : protocolProperties.keySet()) {
            String key;
            try {
                key = (String) keyObject;
            } catch (ClassCastException e) {
                continue;
            }
            properties.setProperty(key, protocolProperties.getProperty(key));
        }

        // NOTE: these properties should be set by the framework
//        if (slave.getDeviceId() != null) {
//            properties.setProperty(MeterProtocol.ADDRESS, slave.getDeviceId());
//        }
//        if (slave.getPassword() != null) {
//            properties.setProperty(MeterProtocol.PASSWORD, slave.getPassword());
//        }
//        if (slave.getNodeAddress() != null) {
//            properties.setProperty(MeterProtocol.NODEID, slave.getNodeAddress());
//        }

        if (slave.getChannels().get(0).getIntervalInSeconds() != 0) {
            properties.setProperty(MeterProtocol.PROFILEINTERVAL, String.valueOf(slave.getChannels().get(0).getIntervalInSeconds()));
        } else {
            getLogger().log(Level.WARNING, "ProfileInterval in database for this meter is set to 0, protocolreader will use a default value of 900 sec!");
        }

        if (slave.getSerialNumber() != null) {
            properties.setProperty(MeterProtocol.SERIALNUMBER, slave.getSerialNumber());
        }
        return properties;
    }

    /**
     * List a successful action for certain schedules of a slave
     *
     * @param commSchedules all (inbound) schedules of a slave
     * @param slave         the slave RTU...
     */
    private void logSuccess(List<CommunicationScheduler> commSchedules, Device slave) {
        for (CommunicationScheduler commSchedule : commSchedules) {
            if (successfulSchedulesIds.contains(commSchedule.getId()) && !failedSchedulesIds.contains(commSchedule.getId())) {
                logSuccess(commSchedule, slave);
            }      //TODO what to do with unhandled schedules????
        }
    }

    private void logSuccess(CommunicationScheduler commSchedule, Device slave) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(new Date(), AmrJournalEntry.CONNECTTIME, getConnectTime()));
        journal.add(new AmrJournalEntry(new Date(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + slave.getSerialNumber() + "]"));
        journal.add(new AmrJournalEntry(new Date(), AmrJournalEntry.TIMEDIFF, "" + getTimeDifference()));
        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_OK));
        try {
            commSchedule.journal(journal);
            commSchedule.logSuccess(new Date());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    /**
     * All schedules log a failure
     *
     * @param commSchedules    list of schedules for a slave device
     * @param exceptionMessage the message (describing the error) that should be logged on the schedule
     * @param completionCode   the completion code for the journal of the schedules
     */
    private void logError(List<CommunicationScheduler> commSchedules, String exceptionMessage, int completionCode) {
        getLogger().severe(exceptionMessage);
        for (CommunicationScheduler commSchedule : commSchedules) {
            logProtocolError(commSchedule, exceptionMessage, completionCode);
        }
    }

    /**
     * Logs a failure on the relevant slave schedules.
     *
     * @param commSchedules    all schedules of the slave
     * @param exceptionMessage the error message
     * @param action           the action that caused the error.
     * @param completionCode   the completion code for the amr journal (e.g. CC_CONFIGURATION_ERROR)
     */
    private void logError(List<CommunicationScheduler> commSchedules, String exceptionMessage, CommunicationProfileAction action, int completionCode) {
        getLogger().severe(exceptionMessage);
        List<CommunicationScheduler> relevantSchedules = getRelevantSchedulesForAction(commSchedules, action);
        for (CommunicationScheduler commSchedule : relevantSchedules) {
            logProtocolError(commSchedule, exceptionMessage, completionCode);
        }
    }

    private List<CommunicationScheduler> getRelevantSchedulesForAction(List<CommunicationScheduler> commSchedules, CommunicationProfileAction action) {
        List<CommunicationScheduler> relevantSchedules = new ArrayList<CommunicationScheduler>();
        int scheduleCount = 0;
        for (List<CommunicationProfileAction> infoForSlaveSchedule : infosForSlaveSchedules) {
            if (infoForSlaveSchedule.contains(action)) {
                relevantSchedules.add(commSchedules.get(scheduleCount));     //A schedule is relevant if it contains the action (LP, registers, ...) that the AMR logging is meant for.
            }
            scheduleCount++;
        }
        return relevantSchedules;
    }

    private void logProtocolError(List<CommunicationScheduler> commSchedules, String exceptionMessage, CommunicationProfileAction action) {
        logError(commSchedules, exceptionMessage, action, AmrJournalEntry.CC_PROTOCOLERROR);
    }


    private void logProtocolError(CommunicationScheduler commSchedule, String exceptionMessage, int completionCode) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(new Date(), AmrJournalEntry.START, new Date().toString()));
        journal.add(new AmrJournalEntry(new Date(), AmrJournalEntry.CONNECTTIME, getConnectTime()));
        journal.add(new AmrJournalEntry(new Date(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + commSchedule.getRtu().getSerialNumber() + "]"));
        journal.add(new AmrJournalEntry(new Date(), AmrJournalEntry.TIMEDIFF, "" + getTimeDifference()));
        journal.add(new AmrJournalEntry(completionCode));
        journal.add(new AmrJournalEntry(AmrJournalEntry.DETAIL, exceptionMessage));
        try {
            commSchedule.journal(journal);
            commSchedule.logFailure(new Date(), "");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }


    private String getConnectTime() {
        String connectTimeStr = "0";
        if (startTime != -1) {
            long connectTime = (System.currentTimeMillis() - startTime) / 1000;
            if (connectTime > 0) {
                connectTimeStr = "" + connectTime;
            }
        }
        startTime = -1;
        return connectTimeStr;
    }

    private List<CommunicationScheduler> getInboundSlaveSchedules(Device slave) {
        List<CommunicationScheduler> inboundSchedules = new ArrayList<CommunicationScheduler>();
        // NOTE: we don't work with schedulers anymore!

//        for (CommunicationScheduler slaveSchedule : slave.getCommunicationSchedulers()) {
//            if (slaveSchedule.getModemPool().getInbound()) {
//                if (slaveSchedule.getNextCommunication() == null) {
//                    getLogger().info("Slave: " + slaveSchedule.getRtu().getSerialNumber() + ", communication scheduler '" + slaveSchedule.displayString() + "' next communication is 'null'. Skipping.");
//                } else if (slaveSchedule.getNextCommunication().after(new Date())) {
//                    getLogger().info("Slave: " + slaveSchedule.getRtu().getSerialNumber() + ", communication scheduler '" + slaveSchedule.displayString() + "' next communication not reached yet. Skipping.");
//                } else {
//                    inboundSchedules.add(slaveSchedule);
//                }
//            }
//        }
        return inboundSchedules;
    }

    public long getTimeDifference() {
        if (timeDiff == -1) {            //Cache the time difference per slave
            timeDiff = calcTimeDiff();
        }
        return timeDiff;
    }

    private long calcTimeDiff() {
        if (meterProtocol == null) {
            return 0;
        }
        try {
            return getDiff(System.currentTimeMillis(), meterProtocol.getTime().getTime());
        } catch (IOException e) {
            return 0;
        }
    }


    private long getDiff(long systemDate, long date) {
        return Math.abs(date - systemDate);
    }

    public String getVersion() {
        return "$Date: 2012-02-16 14:45:23 +0100 (do, 16 feb 2012) $";
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }
}