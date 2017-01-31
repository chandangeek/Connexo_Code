/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.objects;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.exceptions.InboundFrameException;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.protocolimplv2.ace4000.ACE4000;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestState;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;
import com.energyict.protocolimplv2.ace4000.requests.tracking.Tracker;
import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * @author gna & khe
 */
public class ObjectFactory {

    private final ACE4000 ace4000;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private Acknowledge acknowledge = null;
    private boolean sendAck = false;      //Indicates whether or not the parsed message must be ACK'ed.
    private int requestAttemptNumber = 0;

    private FirmwareVersion firmwareVersion = null;
    private NegativeAcknowledge negativeAcknowledge = null;
    private AutoPushConfig autoPushConfig = null;
    private FullMeterConfig fullMeterConfig = null;
    private Announcement announcement = null;
    private CurrentReadings currentReadings = null;
    private LoadProfile loadProfile = null;
    private MBusBillingData mBusBillingData = null;
    private MBusCurrentReadings mbCurrReadings = null;
    private ForceTime forceTime = null;
    private SyncTime syncTime = null;
    private DateTime readTime = null;
    private BillingConfig billingConfig = null;
    private MaxDemandRegister maximumDemandRegisters = null;
    private InstantVoltAndCurrent instantVoltAndCurrent = null;
    private BillingData billingData = null;
    private FirmwareUpgrade firmwareUpgrade = null;
    private Reject reject = null;
    private ContactorControlCommand contactorControlCommand = null;
    private DisplayMessage displayMessage = null;
    private DisplayConfiguration displayConfiguration = null;
    private LoadProfileConfiguration loadProfileConfiguration = null;
    private SpecialDataModeConfiguration specialDataModeConfiguration = null;
    private ConsumptionLimitationConfiguration consumptionLimitationConfiguration = null;
    private EmergencyConsumptionLimitationConfiguration emergencyConsumptionLimitationConfiguration = null;
    private TariffConfiguration tariffConfiguration = null;
    private MaxDemandConfiguration maxDemandConfiguration = null;

    private int trackingID = -1;
    private List<Integer> mustBeAcked;        //Contains the tracking number of the received frames that needs an ACK
    private EventData eventData = null;
    private PowerFailLog powerFailLog = null;
    private boolean inbound = false;

    private boolean clockWasSet = false;

    private String currentSlaveSerialNumber = "";
    private List<String> allSlaveSerialNumbers;
    private Map<Tracker, RequestState> requestStates = new HashMap<>();
    private Map<Tracker, String> reasonDescriptions = new HashMap<>();

    public ObjectFactory(ACE4000 ace4000, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        this.ace4000 = ace4000;
        this.readingTypeUtilService = readingTypeUtilService;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
    }

    /**
     * Overview of the pending requests (type and trackingId) and the result state
     */
    public Map<Tracker, RequestState> getRequestStates() {
        return requestStates;
    }

    /**
     * Map that holds the reason descriptions for failed requests, used in proper issue/problem
     */
    public Map<Tracker, String> getReasonDescriptions() {
        return reasonDescriptions;
    }

    /**
     * Add the request to the overview, for tracking purposes.
     * A request is identified by its type and a trackingId.
     * Any response can be linked to the request by the type (eg. registers are received)
     * or the trackingId (eg. nak or cak contain the trackingId of the related request)
     */
    private void addRequestToOverview(RequestType type) {
        getRequestStates().put(new Tracker(type, getTrackingID()), RequestState.Sent);
    }

    /**
     * Look up the tracker and update the related state to failed.
     * Also store the reason description why the request failed
     */
    private void updateRequestFailed(Tracker key, String reason) {
        for (Tracker realKey : getRequestStates().keySet()) {
            if (key.equals(realKey)) {
                getReasonDescriptions().put(realKey, reason);
                getRequestStates().put(realKey, RequestState.Fail);      //Uses the original key
            }
        }
    }

    /**
     * Look up the tracker and update the related state to successful
     */
    private void updateRequestSuccess(Tracker key) {
        for (Tracker realKey : getRequestStates().keySet()) {
            if (key.equals(realKey)) {
                getRequestStates().put(realKey, RequestState.Success);      //Uses the original key
            }
        }
    }

    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }

    public boolean isInbound() {
        return inbound;
    }

    public boolean isClockWasSet() {
        return clockWasSet;
    }

    public void setClockWasSet(boolean clockWasSet) {
        this.clockWasSet = clockWasSet;
    }

    public FullMeterConfig getFullMeterConfig() {
        if (fullMeterConfig == null) {
            fullMeterConfig = new FullMeterConfig(this);
        }
        return fullMeterConfig;
    }

    public Announcement getAnnouncement() {
        if (announcement == null) {
            announcement = new Announcement(this);
        }
        return announcement;
    }

    public FirmwareUpgrade getFirmwareUpgrade() {
        if (firmwareUpgrade == null) {
            firmwareUpgrade = new FirmwareUpgrade(this);
        }
        return firmwareUpgrade;
    }

    public Reject getReject() {
        if (reject == null) {
            reject = new Reject(null);
        }
        return reject;
    }

    public ContactorControlCommand getContactorControlCommand() {
        if (contactorControlCommand == null) {
            contactorControlCommand = new ContactorControlCommand(this);
        }
        return contactorControlCommand;
    }

    public DisplayMessage getDisplayMessage() {
        if (displayMessage == null) {
            displayMessage = new DisplayMessage(this);
        }
        return displayMessage;
    }

    public DisplayConfiguration getDisplayConfiguration() {
        if (displayConfiguration == null) {
            displayConfiguration = new DisplayConfiguration(this);
        }
        return displayConfiguration;
    }

    public LoadProfileConfiguration getLoadProfileConfiguration() {
        if (loadProfileConfiguration == null) {
            loadProfileConfiguration = new LoadProfileConfiguration(this);
        }
        return loadProfileConfiguration;
    }

    public SpecialDataModeConfiguration getSpecialDataModeConfiguration() {
        if (specialDataModeConfiguration == null) {
            specialDataModeConfiguration = new SpecialDataModeConfiguration(this);
        }
        return specialDataModeConfiguration;
    }

    public ConsumptionLimitationConfiguration getConsumptionLimitationConfiguration() {
        if (consumptionLimitationConfiguration == null) {
            consumptionLimitationConfiguration = new ConsumptionLimitationConfiguration(this);
        }
        return consumptionLimitationConfiguration;
    }

    public EmergencyConsumptionLimitationConfiguration getEmergencyConsumptionLimitationConfiguration() {
        if (emergencyConsumptionLimitationConfiguration == null) {
            emergencyConsumptionLimitationConfiguration = new EmergencyConsumptionLimitationConfiguration(this);
        }
        return emergencyConsumptionLimitationConfiguration;
    }

    public TariffConfiguration getTariffConfiguration() {
        if (tariffConfiguration == null) {
            tariffConfiguration = new TariffConfiguration(this);
        }
        return tariffConfiguration;
    }

    public MaxDemandConfiguration getMaxDemandConfiguration() {
        if (maxDemandConfiguration == null) {
            maxDemandConfiguration = new MaxDemandConfiguration(this);
        }
        return maxDemandConfiguration;
    }

    public NegativeAcknowledge getNegativeAcknowledge() {
        if (negativeAcknowledge == null) {
            negativeAcknowledge = new NegativeAcknowledge(this);
        }
        return negativeAcknowledge;
    }

    public CurrentReadings getCurrentReadings() {
        if (currentReadings == null) {
            currentReadings = new CurrentReadings(this);
        }
        return currentReadings;
    }

    public MBusCurrentReadings getMBusCurrentReadings() {
        if (mbCurrReadings == null) {
            mbCurrReadings = new MBusCurrentReadings(this);
        }
        return mbCurrReadings;
    }

    public LoadProfile getLoadProfile() {
        if (loadProfile == null) {
            loadProfile = new LoadProfile(this);
        }
        return loadProfile;
    }

    public MBusBillingData getMBusBillingData() {
        if (mBusBillingData == null) {
            mBusBillingData = new MBusBillingData(this);
        }
        return mBusBillingData;
    }

    public MaxDemandRegister getMaximumDemandRegisters() {
        if (maximumDemandRegisters == null) {
            maximumDemandRegisters = new MaxDemandRegister(this);
        }
        return maximumDemandRegisters;
    }

    public EventData getEventData() {
        if (eventData == null) {
            eventData = new EventData(this);
        }
        return eventData;
    }

    public PowerFailLog getPowerFailLog() {
        if (powerFailLog == null) {
            powerFailLog = new PowerFailLog(this);
        }
        return powerFailLog;
    }

    public InstantVoltAndCurrent getInstantVoltAndCurrent() {
        if (instantVoltAndCurrent == null) {
            instantVoltAndCurrent = new InstantVoltAndCurrent(this);
        }
        return instantVoltAndCurrent;
    }

    public BillingData getBillingData() {
        if (billingData == null) {
            billingData = new BillingData(this);
        }
        return billingData;
    }

    public BillingConfig getBillingConfig() {
        if (billingConfig == null) {
            billingConfig = new BillingConfig(this);
        }
        return billingConfig;
    }

    public ForceTime getForceTime() {
        if (forceTime == null) {
            forceTime = new ForceTime(this);
        }
        return forceTime;
    }

    public DateTime getReadTime() {
        if (readTime == null) {
            readTime = new DateTime(this);
        }
        return readTime;
    }

    public SyncTime getSyncTime() {
        if (syncTime == null) {
            syncTime = new SyncTime(this);
        }
        return syncTime;
    }

    public Acknowledge getAcknowledge() {
        if (acknowledge == null) {
            acknowledge = new Acknowledge(this);
        }
        return acknowledge;
    }

    public FirmwareVersion getFirmwareVersion() {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(this);
        }
        return firmwareVersion;
    }

    public AutoPushConfig getAutoPushConfig() {
        if (autoPushConfig == null) {
            autoPushConfig = new AutoPushConfig(this);
        }
        return autoPushConfig;
    }

    public int getRequestAttemptNumber() {
        return requestAttemptNumber;  //Used for logging in all ObjectFactory requests
    }

    public void increaseRequestAttemptNumber() {
        requestAttemptNumber++;
    }

    public void setRequestAttemptNumber(int attemptNumber) {
        this.requestAttemptNumber = attemptNumber;
    }

    private String getRetryDescription() {
        return getRequestAttemptNumber() == 0 ? "" : " [Retry " + (getRequestAttemptNumber()) + "]";
    }

    /**
     * Send a request for full meter configuration
     *
     * @throws java.io.IOException when the communication fails
     */
    public void sendFullMeterConfigRequest() throws IOException {
        log(Level.INFO, "Sending meter configuration request" + getRetryDescription());
        getFullMeterConfig().request();
    }

    /**
     * Send xml with the meters autopush config - Startime, Stoptime pushwindow ...
     *
     * @param enabled                - enabled daily push
     * @param start                  - startTime in minutes after midnight
     * @param stop                   - stopTime in minutes after midnight
     * @param random                 - true/false if push can start randomly in pushwindow
     * @param retryWindowPercentage: the percentage of the window that is reserved for retries
     * @throws java.io.IOException when the communication fails
     */
    public void setAutoPushConfig(int enabled, int start, int stop, boolean random, int retryWindowPercentage) throws IOException {
        log(Level.INFO, "Sending request to change the auto push configuration" + getRetryDescription());
        getAutoPushConfig().setEnableState(enabled);
        getAutoPushConfig().setOpen(start);
        getAutoPushConfig().setClose(stop);
        getAutoPushConfig().setRandom(random);
        getAutoPushConfig().setRetryWindowPercentage(retryWindowPercentage);
        getAutoPushConfig().request();
    }

    public int sendFirmwareUpgradeRequest(String path, int jarSize, int jadSize) {
        log(Level.INFO, "Sending request do a firmware upgrade" + getRetryDescription());
        getFirmwareUpgrade().setPath(path);
        getFirmwareUpgrade().setJarSize(jarSize);
        getFirmwareUpgrade().setJadSize(jadSize);
        getFirmwareUpgrade().request();
        addRequestToOverview(RequestType.Firmware);
        return getTrackingID();
    }

    /**
     * Send a command to the meter to connect or disconnect the contactor.
     *
     * @param date when to execute the command. This date is optional.
     * @param cmd  connect or disconnect
     */
    public int sendContactorCommand(Date date, int cmd) {
        getContactorControlCommand().setCommand(cmd);
        getContactorControlCommand().setDate(date);
        getContactorControlCommand().request();

        String commandDescription = "";
        switch (cmd) {
            case 0:
                commandDescription = "(connect)";
                break;
            case 1:
                commandDescription = "(disconnect)";
                break;
        }
        log(Level.INFO, "Sending a contactor control command " + commandDescription + getRetryDescription());
        addRequestToOverview(RequestType.Contactor);
        return getTrackingID();
    }

    public void sendDisplayMessage(int mode, String message) {
        if ((message != null) && !"".equals(message)) {
            log(Level.INFO, "Sending a display message [" + message + "]" + getRetryDescription());
        } else {
            log(Level.INFO, "Disabling the display message" + getRetryDescription());
        }
        getDisplayMessage().setMode(mode);
        getDisplayMessage().setMessage(message);
        getDisplayMessage().request();
        addRequestToOverview(RequestType.Config);
    }

    public void sendDisplayConfigurationRequest(int resolution, String sequence, String originalSequence, int interval) {
        log(Level.INFO, "Configuring the display settings, sequence = [" + originalSequence + "]" + getRetryDescription());
        getDisplayConfiguration().setResolutionCode(resolution);
        getDisplayConfiguration().setSequence(sequence);
        getDisplayConfiguration().setInterval(interval);
        getDisplayConfiguration().request();
        addRequestToOverview(RequestType.Config);
    }

    public void sendLoadProfileConfiguration(int enable, int intervalCode, int maxNumberOfRecords) {
        log(Level.INFO, "Sending request to configure the load profile data recording" + getRetryDescription());
        getLoadProfileConfiguration().setEnable(enable);
        getLoadProfileConfiguration().setInterval(intervalCode);
        getLoadProfileConfiguration().setMaxNumberOfRecords(maxNumberOfRecords);
        getLoadProfileConfiguration().request();
        addRequestToOverview(RequestType.Config);
    }

    public void sendSDMConfiguration(int billingEnable, int billingInterval, int billingNumber, int loadProfileEnable, int loadProfileInterval, int loadProfileNumber, int duration, Date date) {
        log(Level.INFO, "Sending request to configure the special data mode" + getRetryDescription());
        getSpecialDataModeConfiguration().setBillingEnable(billingEnable);
        getSpecialDataModeConfiguration().setBillingInterval(billingInterval);
        getSpecialDataModeConfiguration().setBillingMaxNumberOfRecords(billingNumber);
        getSpecialDataModeConfiguration().setLoadProfileEnable(loadProfileEnable);
        getSpecialDataModeConfiguration().setLoadProfileInterval(loadProfileInterval);
        getSpecialDataModeConfiguration().setLoadProfileMaxNumberOfRecords(loadProfileNumber);
        getSpecialDataModeConfiguration().setDurationInDays(duration);
        getSpecialDataModeConfiguration().setActivationDate(date);
        getSpecialDataModeConfiguration().request();
        addRequestToOverview(RequestType.Config);
    }

    public void sendMaxDemandConfiguration(int register, int numberOfSubIntervals, int subIntervalDuration) {
        log(Level.INFO, "Sending request to configure maximum demand settings" + getRetryDescription());
        getMaxDemandConfiguration().setNumberOfSubIntervals(numberOfSubIntervals);
        getMaxDemandConfiguration().setSubIntervalDuration(subIntervalDuration);
        getMaxDemandConfiguration().setRegister(register);
        getMaxDemandConfiguration().request();
        addRequestToOverview(RequestType.Config);
    }

    public void sendConsumptionLimitationConfigurationRequest(Date date, int numberOfSubIntervals, int subIntervalDuration, int ovlRate, int thresholdTolerance, int thresholdSelection, List<String> switchingMomentsDP0, List<Integer> thresholdsDP0, List<Integer> unitsDP0, List<String> actionsDP0, List<String> switchingMomentsDP1, List<Integer> thresholdsDP1, List<Integer> unitsDP1, List<String> actionsDP1, List<Integer> weekProfile) {
        log(Level.INFO, "Sending request to configure consumption limitations" + getRetryDescription());
        getConsumptionLimitationConfiguration().setDate(date);
        getConsumptionLimitationConfiguration().setNumberOfSubIntervals(numberOfSubIntervals);
        getConsumptionLimitationConfiguration().setSubIntervalDuration(subIntervalDuration);
        getConsumptionLimitationConfiguration().setOvlRate(ovlRate);
        getConsumptionLimitationConfiguration().setThresholdTolerance(thresholdTolerance);
        getConsumptionLimitationConfiguration().setThresholdSelection(thresholdSelection);
        getConsumptionLimitationConfiguration().setSwitchingMomentsDP0(switchingMomentsDP0);
        getConsumptionLimitationConfiguration().setThresholdsDP0(thresholdsDP0);
        getConsumptionLimitationConfiguration().setUnitsDP0(unitsDP0);
        getConsumptionLimitationConfiguration().setActionsDP0(actionsDP0);
        getConsumptionLimitationConfiguration().setSwitchingMomentsDP1(switchingMomentsDP1);
        getConsumptionLimitationConfiguration().setThresholdsDP1(thresholdsDP1);
        getConsumptionLimitationConfiguration().setUnitsDP1(unitsDP1);
        getConsumptionLimitationConfiguration().setActionsDP1(actionsDP1);
        getConsumptionLimitationConfiguration().setWeekProfile(weekProfile);
        getConsumptionLimitationConfiguration().request();
        addRequestToOverview(RequestType.Config);
    }

    public void sendEmergencyConsumptionLimitationConfigurationRequest(int duration, int threshold, int unit, int overrideRate) {
        log(Level.INFO, "Sending request to configure emergency consumption limitations" + getRetryDescription());
        getEmergencyConsumptionLimitationConfiguration().setDuration(duration);
        getEmergencyConsumptionLimitationConfiguration().setThreshold(threshold);
        getEmergencyConsumptionLimitationConfiguration().setUnit(unit);
        getEmergencyConsumptionLimitationConfiguration().setOvlRate(overrideRate);
        getEmergencyConsumptionLimitationConfiguration().request();
        addRequestToOverview(RequestType.Config);
    }

    public void sendTariffConfiguration(int number, int numberOfRates, com.elster.jupiter.calendar.Calendar calendar) {
        log(Level.INFO, "Sending request to configure tariff settings" + getRetryDescription());
        getTariffConfiguration().setTariffNumber(number);
        getTariffConfiguration().setNumberOfRates(numberOfRates);
        getTariffConfiguration().setCalendar(calendar);
        getTariffConfiguration().request();
        addRequestToOverview(RequestType.Config);
    }

    /**
     * Request all the load profile data
     *
     * @throws java.io.IOException when the communication fails
     */
    public void sendLoadProfileRequest() throws IOException {
        log(Level.INFO, "Sending profile data request (all data)" + getRetryDescription());
        getLoadProfile().request();
        addRequestToOverview(RequestType.LoadProfile);
    }

    /**
     * Request the loadprofile data from a certain point in time
     *
     * @param from equals the the point in time
     * @throws java.io.IOException when the communication fails
     */
    public void sendLoadProfileRequest(Date from) throws IOException {
        log(Level.INFO, "Sending profile data request, from date = " + from.toString() + getRetryDescription());
        getLoadProfile().setFrom(from);
        getLoadProfile().request();
        addRequestToOverview(RequestType.LoadProfile);
    }

    public void sendLoadProfileRequest(Date from, Date toDate) {
        log(Level.INFO, "Sending profile data request, from date = " + from.toString() + ", to date = " + toDate.toString() + getRetryDescription());
        getLoadProfile().setFrom(from);
        getLoadProfile().setToDate(toDate);
        getLoadProfile().request();
        addRequestToOverview(RequestType.LoadProfile);
    }

    /**
     * Request all the MBus billing data
     */
    public void sendMBusBillingDataRequest() {
        log(Level.INFO, "Sending MBus billing data request (all data)" + getRetryDescription());
        getMBusBillingData().request();
        addRequestToOverview(RequestType.MBusBillingRegister);
    }

    /**
     * Request the instant voltage and current registers
     */
    public void sendInstantVoltageAndCurrentRequest() {
        log(Level.INFO, "Sending request for instantaneous voltage and current registers" + getRetryDescription());
        getInstantVoltAndCurrent().request();
        addRequestToOverview(RequestType.InstantRegisters);
    }

    /**
     * Request the MBus billing data from a certain point in time
     *
     * @param from equals the point in time
     */
    public void sendMBusBillingDataRequest(Date from) {
        log(Level.INFO, "Sending MBus billing data request, from date = " + from.toString() + getRetryDescription());
        getMBusBillingData().setFrom(from);
        getMBusBillingData().request();
        addRequestToOverview(RequestType.MBusBillingRegister);
    }

    /**
     * Request the MBus current registers
     */
    public void sendMBusCurrentRegistersRequest() {
        log(Level.INFO, "Sending MBus current registers request" + getRetryDescription());
        getMBusCurrentReadings().request();
        addRequestToOverview(RequestType.MBusCurrentRegister);
    }

    /**
     * Request all billing data from the E-meter
     */
    public void sendBDRequest() {
        log(Level.INFO, "Sending billing data (all) request" + getRetryDescription());
        getBillingData().request();
        addRequestToOverview(RequestType.BillingRegisters);
    }

    /**
     * Request the billing data from the E-meter from a certain point in time
     *
     * @param from equals the point in time
     */
    public void sendBDRequest(Date from) {
        log(Level.INFO, "Sending billing data request, from date = " + from.toString() + getRetryDescription());
        getBillingData().setFrom(from);
        getBillingData().request();
        addRequestToOverview(RequestType.BillingRegisters);
    }

    /**
     * Request the current registers
     */
    public void sendCurrentRegisterRequest() {
        log(Level.INFO, "Sending current registers request" + getRetryDescription());
        getCurrentReadings().request();
        addRequestToOverview(RequestType.CurrentRegisters);
    }

    /**
     * Send the E-meters billingdata  configuration
     *
     * @param enabled   - billingdata is enabled/disabled
     * @param intervals - interval in seconds between two records
     * @param numbOfInt - number of records to store
     * @throws java.io.IOException when the communication fails
     */
    public void sendBDConfig(int enabled, int intervals, int numbOfInt) throws IOException {
        log(Level.INFO, "Sending billing data configuration (for e-meter) request" + getRetryDescription());
        getBillingConfig().setEnabled(enabled);
        getBillingConfig().setInterval(intervals);
        getBillingConfig().setNumOfRecs(numbOfInt);
        getBillingConfig().request();
        addRequestToOverview(RequestType.Config);
    }

    /**
     * Force the meter time to the system time
     */
    public void sendForceTime(Date newTime) {
        log(Level.INFO, "Sending force time request, keeping the DST and meter time zone in mind" + getRetryDescription());
        getForceTime().setNewTime(newTime);
        getForceTime().request();
        //State can be tracked by checking the cached time difference
    }

    /**
     * Send a request to read the events
     */
    public void sendEventRequest() {
        log(Level.INFO, "Sending request to read the events" + getRetryDescription());
        getEventData().request();
        addRequestToOverview(RequestType.Events);
    }

    /**
     * Sync the meter time to the system time
     *
     * @param meterTime   used in the time sync message
     * @param receiveTime used in the time sync message
     */
    public void sendSyncTime(long meterTime, long receiveTime) {
        log(Level.INFO, "Sending time sync request");
        getSyncTime().setMeterTime(meterTime);
        getSyncTime().setReceiveTime(receiveTime);
        getSyncTime().request();
        //No need to track the result, this only serves as an ack for the meter after receiving its date and time
    }

    /**
     * Send an acknowledgment with the current (not incremented) tracking ID
     */
    public void sendAcknowledge() {
        log(Level.INFO, "Sending acknowledge with tracking ID [" + getTrackingID() + "]");
        getAcknowledge().setTrackingId(getTrackingID());
        getAcknowledge().request();
    }

    /**
     * Send an acknowledgment with a given tracking ID
     */
    public void sendAcknowledge(int trackingID) {
        log(Level.INFO, "Sending acknowledge with tracking ID [" + trackingID + "]");
        getAcknowledge().setTrackingId(trackingID);
        getAcknowledge().request();
    }

    /**
     * Request the firmware versions of the meter
     */
    public void sendFirmwareRequest() {
        log(Level.INFO, "Sending firmware version request" + getRetryDescription());
        getFirmwareVersion().request();
        addRequestToOverview(RequestType.FirmwareVersion);
    }

    public ACE4000 getAce4000() {
        return ace4000;
    }

    private void ackLater(int trackingID) {
        getMustBeAcked().add(trackingID);
    }

    public List<Integer> getMustBeAcked() {
        if (mustBeAcked == null) {
            mustBeAcked = new ArrayList<>();
        }
        return mustBeAcked;
    }

    /**
     * Send the acks for the received frames, after the device has been found in the database
     * Also send a sync time command if the meter time was received
     */
    public void provideReponseAfterInbound() {
        if (isClockWasSet()) {
            sendSyncTime(getReadTime().getMeterTime(), getReadTime().getReceiveTime());
        }
        for (Integer trackingId : getMustBeAcked()) {
            sendAcknowledge(trackingId);
        }
        mustBeAcked = null;
    }

    /**
     * Parse the received XML to the corresponding object
     *
     * @param xml - the received MeterXML string
     */
    public void parseXML(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;
            try {
                document = builder.parse(new InputSource(new StringReader(xml)));
            } catch (IOException e) {
                throw new InboundFrameException(MessageSeeds.INBOUND_UNEXPECTED_FRAME, xml, e.getMessage());
            }
            Element topElement = document.getDocumentElement();
            parseElements(topElement);
        } catch (ParserConfigurationException | SAXException e) {
            throw new InboundFrameException(MessageSeeds.INBOUND_UNEXPECTED_FRAME, xml, e.getMessage());
        }
    }

    private void parseElements(Element element) throws SAXException {
        String nodeName = element.getNodeName();
        if (nodeName.equalsIgnoreCase(XMLTags.MPUSH)) {
            NodeList nodes = element.getElementsByTagName(XMLTags.METERDATA);
            Element md = (Element) nodes.item(0);
            String currentSerialNumber = null;       //The serial number of the master meter, or of the MBus slave meter if MBus registers were received!

            if (md.getNodeName().equalsIgnoreCase(XMLTags.METERDATA)) {
                NodeList mdNodeList = md.getChildNodes();

                for (int i = 0; i < mdNodeList.getLength(); i++) {
                    Element mdElement = (Element) mdNodeList.item(i);
                    if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.SERIALNUMBER)) {
                        currentSerialNumber = mdElement.getTextContent();
                        getAce4000().setSerialNumber(currentSerialNumber);
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.TRACKER)) {
                        setTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));    // add the radix because we receive hex
                        if (inbound) {
                            ackLater(getTrackingID());
                        } else {
                            sendAck = true;
                        }
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.ACKNOWLEDGE)) {
                        setTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));
                        log(Level.INFO, "Received an acknowledge with tracking ID [" + getTrackingID() + "]");
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.CONFIGACK)) {
                        int ackedTrackingId = Integer.parseInt(mdElement.getTextContent(), 16);     //Tracking ID of the config request that succeeded
                        log(Level.INFO, "Received a configuration acknowledge with tracking ID [" + ackedTrackingId + "]");
                        updateRequestSuccess(new Tracker(RequestType.Config, ackedTrackingId));
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.NACK)) {
                        getNegativeAcknowledge().parse(mdElement);                          //Contains tracking ID of the request that failed
                        log(Level.INFO, "Received a negative acknowledgement, reason: " + getNegativeAcknowledge().getReasonDescription());
                        updateRequestFailed(new Tracker(getNegativeAcknowledge().getFailedTrackingId()), getNegativeAcknowledge().getReasonDescription());
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.REJECT)) { //Should never happen
                        getReject().parse(mdElement);
                        log(Level.INFO, "Message was rejected, reason: " + getReject().getReasonDescription());
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.LOADPR)) {
                        log(Level.INFO, "Received a loadProfile element.");
                        getLoadProfile().parse(mdElement);
                        updateRequestSuccess(new Tracker(RequestType.LoadProfile));
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.LOADPRABS)) {
                        log(Level.INFO, "Received a loadProfile element.");
                        getLoadProfile().parse(mdElement);
                        updateRequestSuccess(new Tracker(RequestType.LoadProfile));
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.EVENT)) {
                        log(Level.INFO, "Received events");
                        getEventData().parse(mdElement);
                        updateRequestSuccess(new Tracker(RequestType.Events));
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.POWERFAIL)) {
                        log(Level.INFO, "Received power fail log");
                        getPowerFailLog().parse(mdElement);
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.ANNOUNCE)) {
                        log(Level.INFO, "Received a device announcement.");
                        getAnnouncement().parse(mdElement);
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.INSTVC)) {
                        log(Level.INFO, "Received instantaneous registers");
                        getInstantVoltAndCurrent().parse(mdElement);
                        for (RegisterValue registerValue : getInstantVoltAndCurrent().getMrd().getRegisterValues()) {
                            getAce4000().getCollectedInstantRegisters().add(createCommonRegister(registerValue));
                        }
                        updateRequestSuccess(new Tracker(RequestType.InstantRegisters));
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.CURREADING)) {
                        log(Level.INFO, "Received current readings from meter.");
                        getCurrentReadings().parse(mdElement);
                        for (RegisterValue registerValue : getCurrentReadings().getMrd().getRegisterValues()) {
                            getAce4000().getCollectedCurrentRegisters().add(createCommonRegister(registerValue));
                            getAce4000().addReceivedRegisterObisCode(registerValue.getObisCode());
                        }
                        updateRequestSuccess(new Tracker(RequestType.CurrentRegisters));
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.BILLDATA)) {
                        log(Level.INFO, "Received billing data from meter.");
                        getBillingData().parse(mdElement);
                        for (RegisterValue registerValue : getBillingData().getMrd().getRegisterValues()) {
                            getAce4000().getCollectedBillingRegisters().add(createBillingRegister(registerValue));
                            getAce4000().addReceivedRegisterObisCode(registerValue.getObisCode());
                        }
                        updateRequestSuccess(new Tracker(RequestType.BillingRegisters));
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.MAXDEMAND)) {
                        log(Level.INFO, "Received maximum demand registers.");
                        getMaximumDemandRegisters().parse(mdElement);
                        for (RegisterValue registerValue : getMaximumDemandRegisters().getMdr().getRegisterValues()) {
                            getAce4000().getCollectedMaxDemandRegisters().add(createCommonRegister(registerValue));
                            getAce4000().addReceivedRegisterObisCode(registerValue.getObisCode());
                        }
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.MBUSBILLINGDATA)) {
                        log(Level.INFO, "Received MBus billing data.");
                        setCurrentSlaveSerialNumber(currentSerialNumber);
                        getMBusBillingData().parse(mdElement);
                        for (RegisterValue registerValue : getMBusBillingData().getMrd().getRegisterValues()) {
                            getAce4000().getCollectedMBusBillingRegisters().add(createCommonRegister(registerValue, this.identificationService.createDeviceIdentifierByCallHomeId(currentSerialNumber)));
                            getAce4000().addReceivedRegisterObisCode(registerValue.getObisCode());
                        }
                        updateRequestSuccess(new Tracker(RequestType.MBusBillingRegister));
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.MBUSCREADING)) {
                        log(Level.INFO, "Received current readings from MBus meter.");
                        setCurrentSlaveSerialNumber(currentSerialNumber);
                        getMBusCurrentReadings().parse(mdElement);
                        for (RegisterValue registerValue : getMBusCurrentReadings().getMrd().getRegisterValues()) {
                            getAce4000().getCollectedMBusCurrentRegisters().add(createCommonRegister(registerValue, this.identificationService.createDeviceIdentifierByCallHomeId(currentSerialNumber)));
                            getAce4000().addReceivedRegisterObisCode(registerValue.getObisCode());
                        }
                        updateRequestSuccess(new Tracker(RequestType.MBusCurrentRegister));
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.RESFIRMWARE)) {
                        log(Level.INFO, "Received firmware versions.");
                        getFirmwareVersion().parse(mdElement);
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.CONFIGURATION)) {
                        log(Level.INFO, "Received configuration from meter.");
                        getFullMeterConfig().parse(mdElement);
                    } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.METERTIME)) {
                        log(Level.INFO, "Received timing parameters.");
                        //This will cache the time difference
                        getReadTime().parse(mdElement);
                    }
                }
            } else {
                throw new SAXException("Unknown tag found in xml response: " + nodes.item(0).getNodeName());
            }
        } else {
            throw new SAXException("Unknown tag found in xml response: " + element.getNodeName());
        }
        if (sendAck && !inbound) {
            sendAcknowledge();
            sendAck = false;
        }
    }

    private void setCurrentSlaveSerialNumber(String pushedSerialNumber) {
        this.currentSlaveSerialNumber = pushedSerialNumber;
        if (!getAllSlaveSerialNumbers().contains(pushedSerialNumber)) {
            //Add to the list of slave devices
            getAllSlaveSerialNumbers().add(pushedSerialNumber);
        }
    }

    public String getCurrentSlaveSerialNumber() {
        return currentSlaveSerialNumber;
    }

    public List<String> getAllSlaveSerialNumbers() {
        if (allSlaveSerialNumbers == null) {
            allSlaveSerialNumbers = new ArrayList<>();
        }
        return allSlaveSerialNumbers;
    }

    private CollectedRegister createCommonRegister(RegisterValue registerValue) {
        return createCommonRegister(registerValue, getAce4000().getDeviceIdentifier());
    }

    private CollectedRegister createCommonRegister(RegisterValue registerValue, DeviceIdentifier<?> deviceIdentifier) {
        //TODO should this be max demand register?
        CollectedRegister deviceRegister =
                this.collectedDataFactory.
                        createMaximumDemandCollectedRegister(
                                new RegisterDataIdentifierByObisCodeAndDevice(
                                        registerValue.getObisCode(),
                                        registerValue.getObisCode(),
                                        deviceIdentifier), this.readingTypeUtilService.getReadingTypeFrom(registerValue.getObisCode(), registerValue.getQuantity().getUnit()));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(
                registerValue.getReadTime().toInstant(),
                registerValue.getFromTime().toInstant(),
                registerValue.getToTime().toInstant(),
                registerValue.getEventTime().toInstant());
        return deviceRegister;
    }

    private CollectedRegister createBillingRegister(RegisterValue registerValue) {
        CollectedRegister deviceRegister =
                this.collectedDataFactory.
                        createBillingCollectedRegister(
                                new RegisterDataIdentifierByObisCodeAndDevice(
                                        registerValue.getObisCode(),
                                        registerValue.getObisCode(),
                                        getAce4000().getDeviceIdentifier()), this.readingTypeUtilService.getReadingTypeFrom(registerValue.getObisCode(), registerValue.getQuantity().getUnit()));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(
                registerValue.getReadTime().toInstant(),
                registerValue.getFromTime().toInstant(),
                registerValue.getToTime().toInstant());
        return deviceRegister;
    }

    public List<CollectedLoadProfile> createCollectedLoadProfiles() {
        return createCollectedLoadProfiles(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);
    }

    public List<CollectedLoadProfile> createCollectedLoadProfiles(ObisCode obisCode) {
        List<CollectedLoadProfile> collectedLoadProfiles = new ArrayList<>();
        CollectedLoadProfile collectedLoadProfile =
                this.collectedDataFactory.
                        createCollectedLoadProfile(
                                this.identificationService.createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(
                                        obisCode,
                                        getAce4000().getDeviceIdentifier()));
        collectedLoadProfile.setCollectedData(getLoadProfile().getProfileData().getIntervalDatas(), getLoadProfile().getProfileData().getChannelInfos());
        collectedLoadProfiles.add(collectedLoadProfile);
        return collectedLoadProfiles;
    }

    protected void log(Level level, String msg) {
        getAce4000().getLogger().log(level, msg);
    }

    public int getTrackingID() {
        return trackingID;
    }

    protected int getIncreasedTrackingID() {
        trackingID++;
        if (trackingID > 4096) {
            trackingID = 0;
        }
        return trackingID;
    }

    protected void setTrackingID(int trackingID) {
        this.trackingID = trackingID;
    }

    public Date getCurrentMeterTime() {
        return getCurrentMeterTime(getAce4000().isDst(), new Date());
    }

    public Date getMeterTime(Date date) {
        return getCurrentMeterTime(getAce4000().isDst(), date);
    }

    /**
     * Convert a given date to a time stamp in the meter's time zone
     *
     * @param dst  whether or not to add dst offset
     * @param date the given date
     * @return the resulting date
     */
    public Date getCurrentMeterTime(boolean dst, Date date) {
        TimeZone timeZone = getAce4000().getTimeZone();
        Calendar now = Calendar.getInstance(timeZone);
        now.setTime(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        cal.set(Calendar.DATE, now.get(Calendar.DATE));
        cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND));
        cal.setLenient(true);
        cal.add(Calendar.HOUR_OF_DAY, dst ? -1 : 0);   //Convert to base time: cut off the DST offset
        return cal.getTime();
    }

    public Date convertMeterDateToSystemDate(long seconds) {
        return convertMeterDateToSystemDate(new Date(seconds * 1000));
    }

    /**
     * Convert a timestamp received from the meter to a system time stamp
     *
     * @param date the given timestamp
     * @return the converted date in the system's time zone
     */
    public Date convertMeterDateToSystemDate(Date date) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTime(date);

        Calendar meter = Calendar.getInstance(getAce4000().getTimeZone());

        meter.set(Calendar.YEAR, utc.get(Calendar.YEAR));
        meter.set(Calendar.MONTH, utc.get(Calendar.MONTH));
        meter.set(Calendar.DATE, utc.get(Calendar.DATE));
        meter.set(Calendar.HOUR_OF_DAY, utc.get(Calendar.HOUR_OF_DAY));
        meter.set(Calendar.MINUTE, utc.get(Calendar.MINUTE));
        meter.set(Calendar.SECOND, utc.get(Calendar.SECOND));
        meter.set(Calendar.MILLISECOND, utc.get(Calendar.MILLISECOND));
        meter.setLenient(true);
        meter.add(Calendar.HOUR_OF_DAY, getAce4000().isDst() ? 1 : 0);    //Convert from base time to actual time: add DST offset
        return meter.getTime();
    }

    public CollectedLogBook getDeviceLogBook(LogBookIdentifier identifier) {
        CollectedLogBook deviceLogBook = this.collectedDataFactory.createCollectedLogBook(identifier);
        deviceLogBook.setMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(getAllMeterEvents(), this.meteringService));
        return deviceLogBook;
    }

    public List<MeterEvent> getAllMeterEvents() {
        List<MeterEvent> allEvents = getEventData().getMeterEvents();
        if (allEvents == null) {
            allEvents = new ArrayList<>();
        }
        MeterEvent powerFailEvent = getPowerFailLog().getMeterEvent();
        if (powerFailEvent != null) {
            allEvents.add(powerFailEvent);
        }
        MeterEvent announceEvent = getAnnouncement().getMeterEvent();
        if (powerFailEvent != null) {
            allEvents.add(announceEvent);
        }
        return allEvents;
    }

    public CollectedFirmwareVersion createCollectedFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = this.collectedDataFactory.createFirmwareVersionsCollectedData(getAce4000().getDeviceIdentifier());
        firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getFirmwareVersion().getMetrologyFirmwareVersion());
        firmwareVersionsCollectedData.setActiveCommunicationFirmwareVersion(getFirmwareVersion().getAuxiliaryFirmwareVersion());
        return firmwareVersionsCollectedData;
    }

    public CollectedFirmwareVersion createFailedFirmwareVersions(Issue issue) {
        CollectedFirmwareVersion firmwareVersionsCollectedData = this.collectedDataFactory.createFirmwareVersionsCollectedData(getAce4000().getDeviceIdentifier());
        firmwareVersionsCollectedData.setFailureInformation(ResultType.DataIncomplete, issue);
        return firmwareVersionsCollectedData;
    }
}