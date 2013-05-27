package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.ace4000.ACE4000;
import com.energyict.genericprotocolimpl.ace4000.objects.xml.XMLTags;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.Device;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * @author gna & khe
 */
public class ObjectFactory {

    private ACE4000 ace4000;
    private Acknowledge acknowledge = null;
    private ConfigurationAcknowledge configurationAcknowledge = null;
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
    private TimeConfig configTime = null;
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
    private boolean sendAck = false;      //Indicates whether or not the parsed message must be ACK'ed.
    private boolean requestsAllowed = true;      //Indicates if a GPRS session is alive
    private EventData eventData = null;
    private PowerFailLog powerFailLog = null;
    private List<MeterEvent> meterEvents;

    private boolean receivedCurrentRegisters = false;
    private boolean receivedBillingRegisters = false;
    private boolean receivedMaxDemandRegisters = false;
    private boolean receivedInstantRegisters = false;
    private Boolean receivedMBusCurrentRegisters = null;       //Nack when device doesn't support slave meters
    private Boolean receivedMBusBillingRegisters = null;       //Nack when device doesn't support slave meters
    private Boolean receivedEvents = null;
    private Boolean receivedLoadProfile = null;
    private Boolean connectSucceeded = null;
    private Boolean disconnectSucceeded = null;
    private Boolean displayMessageSucceeded = null;
    private Boolean displayConfigurationSucceeded = null;
    private Boolean loadProfileConfigurationSucceeded = null;
    private Boolean sdmConfigurationSucceeded = null;
    private Boolean maxDemandConfigurationSucceeded = null;
    private Boolean consumptionLimitationConfigurationSucceeded = null;
    private Boolean emergencyConsumptionLimitationConfigurationSucceeded = null;
    private Boolean tariffConfigurationSucceeded = null;
    private Boolean firmWareSucceeded = null;
    private int connectTrackingId = -2;
    private int disconnectTrackingId = -2;
    private int firmwareTrackingId = -2;
    private int mBusBillingRegistersTrackingId = -2;
    private int mBusCurrentRegistersTrackingId = -2;
    private int displayMessageTrackingId = -2;
    private int displayConfigurationTrackingId = -2;
    private int loadProfileConfigurationTrackingId = -2;
    private int sdmConfigurationTrackingId = -2;
    private int maxDemandConfigurationTrackingId = -2;
    private int consumptionLimitationConfigurationTrackingId = -2;
    private int emergencyConsumptionLimitationConfigurationTrackingId = -2;
    private int tariffConfigurationTrackingId = -2;
    private boolean clockWasSet = false;

    public ObjectFactory(ACE4000 ace4000) {
        this.ace4000 = ace4000;
    }

    public void setRequestsAllowed(boolean requestsAllowed) {
        this.requestsAllowed = requestsAllowed;
    }

    public boolean isRequestsAllowed() {
        return requestsAllowed;
    }

    public boolean isReceivedBillingRegisters() {
        return receivedBillingRegisters;
    }

    public boolean isClockWasSet() {
        return clockWasSet;
    }

    public Boolean isReceivedMBusBillingRegisters() {
        return receivedMBusBillingRegisters;
    }

    public void setClockWasSet(boolean clockWasSet) {
        this.clockWasSet = clockWasSet;
    }

    public boolean isReceivedCurrentRegisters() {
        return receivedCurrentRegisters;
    }

    public boolean isReceivedInstantRegisters() {
        return receivedInstantRegisters;
    }

    public boolean isReceivedMaxDemandRegisters() {
        return receivedMaxDemandRegisters;
    }

    public Boolean isReceivedMBusCurrentRegisters() {
        return receivedMBusCurrentRegisters;
    }

    public boolean shouldRetryLoadProfile() {
        return (receivedLoadProfile == null);
    }

    public boolean shouldRetryEvents() {
        return (receivedEvents == null);
    }

    public boolean shouldRetryFirmwareUpgrade() {
        return (firmWareSucceeded == null);     //Should not retry if FW upgrade was ACK'ed or NACK'ed.
    }                                           //Only retry after timeouts.

    public boolean shouldRetryConnectCommand() {
        return (connectSucceeded == null);
    }

    public boolean shouldRetryDisplayConfigRequest() {
        return (displayConfigurationSucceeded == null);
    }

    public boolean shouldRetryLoadProfileConfiguration() {
        return (loadProfileConfigurationSucceeded == null);
    }

    public boolean shouldRetrySDMConfiguration() {
        return (sdmConfigurationSucceeded == null);
    }

    public boolean shouldRetryMaxDemandConfiguration() {
        return (maxDemandConfigurationSucceeded == null);
    }

    public boolean shouldRetryConsumptionLimitationConfiguration() {
        return (consumptionLimitationConfigurationSucceeded == null);
    }

    public boolean shouldRetryEmergencyConsumptionLimitationConfiguration() {
        return (emergencyConsumptionLimitationConfigurationSucceeded == null);
    }

    public boolean shouldRetryTariffConfiguration() {
        return (tariffConfigurationSucceeded == null);
    }

    public boolean shouldRetryDisconnectCommand() {
        return (disconnectSucceeded == null);
    }

    public boolean shouldRetryDisplayMessageRequest() {
        return (displayMessageSucceeded == null);
    }

    public Boolean getReceivedEvents() {
        return receivedEvents;
    }

    public Boolean getReceivedLoadProfile() {
        return receivedLoadProfile;
    }

    public Boolean getConnectSucceeded() {
        return connectSucceeded;
    }

    public Boolean getDisconnectSucceeded() {
        return disconnectSucceeded;
    }

    public Boolean getDisplayMessageSucceeded() {
        return displayMessageSucceeded;
    }

    public Boolean getDisplayConfigurationSucceeded() {
        return displayConfigurationSucceeded;
    }

    public Boolean getLoadProfileConfigurationSucceeded() {
        return loadProfileConfigurationSucceeded;
    }

    public Boolean getSDMConfigurationSucceeded() {
        return sdmConfigurationSucceeded;
    }

    public Boolean getMaxDemandConfigurationSucceeded() {
        return maxDemandConfigurationSucceeded;
    }

    public Boolean getConsumptionLimitationConfigurationSucceeded() {
        return consumptionLimitationConfigurationSucceeded;
    }

    public Boolean getEmergencyConsumptionLimitationConfigurationSucceeded() {
        return emergencyConsumptionLimitationConfigurationSucceeded;
    }

    public Boolean getTariffConfigurationSucceeded() {
        return tariffConfigurationSucceeded;
    }

    public Boolean getFirmWareSucceeded() {
        return firmWareSucceeded;
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

    public MBusCurrentReadings getMBCurrentReadings() {
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

    public TimeConfig getConfigTime() {
        if (configTime == null) {
            configTime = new TimeConfig(this);
        }
        return configTime;
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

    public ConfigurationAcknowledge getConfigurationAcknowledge() {
        if (configurationAcknowledge == null) {
            configurationAcknowledge = new ConfigurationAcknowledge(this);
        }
        return configurationAcknowledge;
    }

    public AutoPushConfig getAutoPushConfig() {
        if (autoPushConfig == null) {
            autoPushConfig = new AutoPushConfig(this);
        }
        return autoPushConfig;
    }

    private String getRetryDescription() {
        return getAce4000().getRetry() == -1 ? "" : " [Retry " + (getAce4000().getRetry() + 1) + "]";
    }

    /**
     * Send a request for full meter configuration
     *
     * @throws IOException when the communication fails
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
     * @throws IOException when the communication fails
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

    public void sendFirmwareUpgradeRequest(String path, int jarSize, int jadSize) throws IOException {
        log(Level.INFO, "Sending request do a firmware upgrade" + getRetryDescription());
        getFirmwareUpgrade().setPath(path);
        getFirmwareUpgrade().setJarSize(jarSize);
        getFirmwareUpgrade().setJadSize(jadSize);
        getFirmwareUpgrade().request();
        firmwareTrackingId = getTrackingID();        //Remember this tracking ID to see if the FW request is acknowledged by the meter
    }

    /**
     * Send a command to the meter to connect or disconnect the contactor.
     *
     * @param date when to execute the command. This date is optional.
     * @param cmd  connect or disconnect
     * @throws IOException when communication fails
     */
    public void sendContactorCommand(Date date, int cmd) throws IOException {
        getContactorControlCommand().setCommand(cmd);
        getContactorControlCommand().setDate(date);
        getContactorControlCommand().request();

        String commandDescription = "";
        switch (cmd) {
            case 0:
                connectTrackingId = getTrackingID();
                commandDescription = "(connect)";
                break;
            case 1:
                disconnectTrackingId = getTrackingID();
                commandDescription = "(disconnect)";
                break;
        }
        log(Level.INFO, "Sending a contactor control command " + commandDescription + getRetryDescription());
    }

    public void sendDisplayMessage(int mode, String message) throws IOException {
        if ((message != null) && !"".equals(message)) {
            log(Level.INFO, "Sending a display message [" + message + "]" + getRetryDescription());
        } else {
            log(Level.INFO, "Disabling the display message" + getRetryDescription());
        }
        getDisplayMessage().setMode(mode);
        getDisplayMessage().setMessage(message);
        getDisplayMessage().request();
        displayMessageTrackingId = getTrackingID();
    }

    public void sendDisplayConfigurationRequest(int resolution, String sequence, String originalSequence, int interval) throws IOException {
        log(Level.INFO, "Configuring the display settings, sequence = [" + originalSequence + "]" + getRetryDescription());
        getDisplayConfiguration().setResolutionCode(resolution);
        getDisplayConfiguration().setSequence(sequence);
        getDisplayConfiguration().setInterval(interval);
        getDisplayConfiguration().request();
        displayConfigurationTrackingId = getTrackingID();
    }

    public void sendLoadProfileConfiguration(int enable, int intervalCode, int maxNumberOfRecords) throws IOException {
        log(Level.INFO, "Sending request to configure the load profile data recording" + getRetryDescription());
        getLoadProfileConfiguration().setEnable(enable);
        getLoadProfileConfiguration().setInterval(intervalCode);
        getLoadProfileConfiguration().setMaxNumberOfRecords(maxNumberOfRecords);
        getLoadProfileConfiguration().request();
        loadProfileConfigurationTrackingId = getTrackingID();
    }

    public void sendSDMConfiguration(int billingEnable, int billingInterval, int billingNumber, int loadProfileEnable, int loadProfileInterval, int loadProfileNumber, int duration, Date date) throws IOException {
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
        sdmConfigurationTrackingId = getTrackingID();
    }

    public void sendMaxDemandConfiguration(int register, int numberOfSubIntervals, int subIntervalDuration) throws IOException {
        log(Level.INFO, "Sending request to configure maximum demand settings" + getRetryDescription());
        getMaxDemandConfiguration().setNumberOfSubIntervals(numberOfSubIntervals);
        getMaxDemandConfiguration().setSubIntervalDuration(subIntervalDuration);
        getMaxDemandConfiguration().setRegister(register);
        getMaxDemandConfiguration().request();
        maxDemandConfigurationTrackingId = getTrackingID();
    }

    public void sendConsumptionLimitationConfigurationRequest(Date date, int numberOfSubIntervals, int subIntervalDuration, int ovlRate, int thresholdTolerance, int thresholdSelection, List<String> switchingMomentsDP0, List<Integer> thresholdsDP0, List<Integer> unitsDP0, List<String> actionsDP0, List<String> switchingMomentsDP1, List<Integer> thresholdsDP1, List<Integer> unitsDP1, List<String> actionsDP1, List<Integer> weekProfile) throws IOException {
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
        consumptionLimitationConfigurationTrackingId = getTrackingID();
    }

    public void sendEmergencyConsumptionLimitationConfigurationRequest(int duration, int threshold, int unit, int overrideRate) throws IOException {
        log(Level.INFO, "Sending request to configure emergency consumption limitations" + getRetryDescription());
        getEmergencyConsumptionLimitationConfiguration().setDuration(duration);
        getEmergencyConsumptionLimitationConfiguration().setThreshold(threshold);
        getEmergencyConsumptionLimitationConfiguration().setUnit(unit);
        getEmergencyConsumptionLimitationConfiguration().setOvlRate(overrideRate);
        getEmergencyConsumptionLimitationConfiguration().request();
        emergencyConsumptionLimitationConfigurationTrackingId = getTrackingID();
    }

    public void sendTariffConfiguration(int number, int numberOfRates, int codeTableId) throws IOException {
        log(Level.INFO, "Sending request to configure tariff settings" + getRetryDescription());
        getTariffConfiguration().setTariffNumber(number);
        getTariffConfiguration().setNumberOfRates(numberOfRates);
        getTariffConfiguration().setCodeTableId(codeTableId);
        getTariffConfiguration().request();
        tariffConfigurationTrackingId = getTrackingID();
    }

    /**
     * Request all the load profile data
     *
     * @throws IOException when the communication fails
     */
    public void sendLoadProfileRequest() throws IOException {
        log(Level.INFO, "Sending profile data request (all data)" + getRetryDescription());
        getLoadProfile().request();
    }

    /**
     * Request the loadprofile data from a certain point in time
     *
     * @param from equals the the point in time
     * @throws IOException when the communication fails
     */
    public void sendLoadProfileRequest(Date from) throws IOException {
        log(Level.INFO, "Sending profile data request, from date = " + from.toString() + getRetryDescription());
        getLoadProfile().setFrom(from);
        getLoadProfile().request();
    }

    public void sendLoadProfileRequest(Date from, Date toDate) throws IOException {
        log(Level.INFO, "Sending profile data request, from date = " + from.toString() + ", to date = " + toDate.toString() + getRetryDescription());
        getLoadProfile().setFrom(from);
        getLoadProfile().setToDate(toDate);
        getLoadProfile().request();
    }

    /**
     * Request all the MBus billing data
     *
     * @throws IOException when the communication fails
     */
    public void sendMBusBillingDataRequest() throws IOException {
        log(Level.INFO, "Sending MBus billing data request (all data)" + getRetryDescription());
        getMBusBillingData().request();
        mBusBillingRegistersTrackingId = getTrackingID();
    }

    /**
     * Request the instant voltage and current registers
     *
     * @throws IOException when the communication fails
     */
    public void sendInstantVoltageAndCurrentRequest() throws IOException {
        log(Level.INFO, "Sending request for instantaneous voltage and current registers" + getRetryDescription());
        getInstantVoltAndCurrent().request();
    }

    /**
     * Request the MBus billing data from a certain point in time
     *
     * @param from equals the point in time
     * @throws IOException when the communication fails
     */
    public void sendMBusBillingDataRequest(Date from) throws IOException {
        log(Level.INFO, "Sending MBus billing data request, from date = " + from.toString() + getRetryDescription());
        getMBusBillingData().setFrom(from);
        getMBusBillingData().request();
        mBusBillingRegistersTrackingId = getTrackingID();
    }

    /**
     * Request the MBus current registers
     *
     * @throws IOException when the communication fails
     */
    public void sendMBusCurrentRegistersRequest() throws IOException {
        log(Level.INFO, "Sending MBus current registers request" + getRetryDescription());
        getMBCurrentReadings().request();
        mBusCurrentRegistersTrackingId = getTrackingID();
    }

    /**
     * Request all billing data from the E-meter
     *
     * @throws IOException when the communication fails
     */
    public void sendBDRequest() throws IOException {
        log(Level.INFO, "Sending billing data (all) request" + getRetryDescription());
        getBillingData().request();
    }

    /**
     * Request the billing data from the E-meter from a certain point in time
     *
     * @param from equals the point in time
     * @throws IOException when the communication fails
     */
    public void sendBDRequest(Date from) throws IOException {
        log(Level.INFO, "Sending billing data request, from date = " + from.toString() + getRetryDescription());
        getBillingData().setFrom(from);
        getBillingData().request();
    }

    /**
     * Request the current registers
     *
     * @throws IOException when the communication fails
     */
    public void sendCurrentRegisterRequest() throws IOException {
        log(Level.INFO, "Sending current registers request" + getRetryDescription());
        getCurrentReadings().request();
    }

    /**
     * Send the E-meters billingdata  configuration
     *
     * @param enabled   - billingdata is enabled/disabled
     * @param intervals - interval in seconds between two records
     * @param numbOfInt - number of records to store
     * @throws IOException when the communication fails
     */
    public void sendBDConfig(int enabled, int intervals, int numbOfInt) throws IOException {
        log(Level.INFO, "Sending billing data configuration (for e-meter) request" + getRetryDescription());
        getBillingConfig().setEnabled(enabled);
        getBillingConfig().setInterval(intervals);
        getBillingConfig().setNumOfRecs(numbOfInt);
        getBillingConfig().request();
    }

    /**
     * Force the meter time to the system time
     *
     * @throws IOException when the communication fails
     */
    public void sendForceTime() throws IOException {
        log(Level.INFO, "Sending force time request, keeping the DST and meter time zone in mind" + getRetryDescription());
        getForceTime().request();
    }

    /**
     * Send a request to read the events
     *
     * @throws IOException when the communication fails
     */
    public void sendEventRequest() throws IOException {
        log(Level.INFO, "Sending request to read the events" + getRetryDescription());
        getEventData().request();
    }

    /**
     * Sync the meter time to the system time
     *
     * @param meterTime   used in the time sync message
     * @param receiveTime used in the time sync message
     * @throws IOException when the communication fails
     */
    public void sendSyncTime(long meterTime, long receiveTime) throws IOException {
        if (isRequestsAllowed()) {
            log(Level.INFO, "Sending time sync request");
            getSyncTime().setMeterTime(meterTime);
            getSyncTime().setReceiveTime(receiveTime);
            getSyncTime().request();
        }
    }

    /**
     * Send the time sync configuration
     *
     * @param diff  - maximum allowed time difference for timesync to take place (in seconds)
     * @param trip  - maximum SNTP trip time in seconds
     * @param retry - maximum number of clock sync retries allowed
     * @throws IOException when the communication fails
     */
    public void sendTimeConfig(int diff, int trip, int retry) throws IOException {
        log(Level.INFO, "Sending time configuration request" + getRetryDescription());
        getConfigTime().setDiff(diff);
        getConfigTime().setTrip(trip);
        getConfigTime().setRetry(retry);
        getConfigTime().request();
    }

    /**
     * Send an acknowledgment with the current (not incremented) tracking ID
     *
     * @throws IOException when the communication fails
     */
    public void sendAcknowledge() throws IOException {
        log(Level.INFO, "Sending acknowledge with tracking ID [" + getTrackingID() + "]");
        getAcknowledge().setTrackingId(getTrackingID());
        getAcknowledge().request();
    }

    public void sendConfigurationAcknowledge() throws IOException {
        log(Level.INFO, "Sending configuration acknowledge with tracking ID [" + getTrackingID() + "]");
        getAcknowledge().setTrackingId(getTrackingID());
        getConfigurationAcknowledge().request();
    }

    /**
     * Request the firmware versions of the meter
     *
     * @throws IOException when the communication fails
     */
    public void sendFirmwareRequest() throws IOException {
        log(Level.INFO, "Sending firmware version request" + getRetryDescription());
        getFirmwareVersion().request();
    }

    public ACE4000 getAce4000() {
        return ace4000;
    }

    /**
     * Parse the received XML to the corresponding object
     *
     * @param xml - the received MeterXML string
     * @throws ParserConfigurationException
     * @throws SAXException                 when the xml parsing fails
     * @throws IOException                  when the communication fails
     * @throws BusinessException
     */
    public void parseXML(String xml) throws ParserConfigurationException, SAXException, IOException, BusinessException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            Element topElement = document.getDocumentElement();
            parseElements(topElement);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new ParserConfigurationException("Failed to make a new builder from the documentBuilderFactory" + e.getMessage() + "(Received xml: " + xml + ")");
        } catch (SAXException e) {
            e.printStackTrace();
            throw new SAXException(e.getMessage() + "(Received xml: " + xml + ")");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage() + "(Received xml: " + xml + ")");
        } catch (DOMException e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage() + "(Received xml: " + xml + ")");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage() + "(Received xml: " + xml + ")");
        } catch (BusinessException e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage() + "(Received xml: " + xml + ")");
        }
    }

    /**
     * These trackingID's are kept to check if certain messages are ACK'ed.
     * They should be reset after every message attempt.
     */
    public void resetTrackingIDs() {
        firmwareTrackingId = -2;
        connectTrackingId = -2;
        disconnectTrackingId = -2;
        displayMessageTrackingId = -2;
        displayConfigurationTrackingId = -2;
        loadProfileConfigurationTrackingId = -2;
        sdmConfigurationTrackingId = -2;
        maxDemandConfigurationTrackingId = -2;
        consumptionLimitationConfigurationTrackingId = -2;
        emergencyConsumptionLimitationConfigurationTrackingId = -2;
        tariffConfigurationTrackingId = -2;
    }

    private void updateStatus(boolean status) {
        if (getTrackingID() == firmwareTrackingId) {
            firmWareSucceeded = status;
        }
        if (getTrackingID() == connectTrackingId) {
            connectSucceeded = status;
        }
        if (getTrackingID() == disconnectTrackingId) {
            disconnectSucceeded = status;
        }
    }

    private void updateMBusRequestStatus() {
        if (getTrackingID() == mBusBillingRegistersTrackingId) {
            receivedMBusBillingRegisters = false;
            log(Level.WARNING, "Meter doesn't support slave devices, can't request MBus billing data");
            mBusBillingRegistersTrackingId = -2;
        }
        if (getTrackingID() == mBusCurrentRegistersTrackingId) {
            receivedMBusCurrentRegisters = false;
            log(Level.WARNING, "Meter doesn't support slave devices, can't request MBus current readings");
            mBusCurrentRegistersTrackingId = -2;
        }
    }

    /**
     * A configuration acknowledgement with the right tracking ID confirms the display message success
     */
    private void updateConfigurationStatus(boolean status) throws BusinessException, SQLException {
        if (getTrackingID() == displayMessageTrackingId) {
            displayMessageSucceeded = status;
        }
        if (getTrackingID() == displayConfigurationTrackingId) {
            displayConfigurationSucceeded = status;
        }
        if (getTrackingID() == loadProfileConfigurationTrackingId) {
            loadProfileConfigurationSucceeded = status;
        }
        if (getTrackingID() == sdmConfigurationTrackingId) {
            sdmConfigurationSucceeded = status;
        }
        if (getTrackingID() == maxDemandConfigurationTrackingId) {
            maxDemandConfigurationSucceeded = status;
        }
        if (getTrackingID() == consumptionLimitationConfigurationTrackingId) {
            consumptionLimitationConfigurationSucceeded = status;
        }
        if (getTrackingID() == emergencyConsumptionLimitationConfigurationTrackingId) {
            emergencyConsumptionLimitationConfigurationSucceeded = status;
        }
        if (getTrackingID() == tariffConfigurationTrackingId) {
            tariffConfigurationSucceeded = status;
        }
        getAce4000().setMessageResults(false);
    }

    private void parseElements(Element element) throws IOException, SQLException, BusinessException {

        String nodeName = element.getNodeName();

        try {
            if (nodeName.equalsIgnoreCase(XMLTags.MPUSH)) {
                NodeList nodes = element.getElementsByTagName(XMLTags.METERDATA);
                Element md = (Element) nodes.item(0);

                if (md.getNodeName().equalsIgnoreCase(XMLTags.METERDATA)) {
                    NodeList mdNodeList = md.getChildNodes();

                    for (int i = 0; i < mdNodeList.getLength(); i++) {
                        Element mdElement = (Element) mdNodeList.item(i);
                        if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.SERIALNUMBER)) {
                            getAce4000().setPushedSerialNumber(mdElement.getTextContent());
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.TRACKER)) {
                            setTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));    // add the radius because we receive hex
                            sendAck = true;               //Every received trackingId must be ACK'ed
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.ACKNOWLEDGE)) {
                            setTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));
                            updateStatus(true);
                            log(Level.INFO, "Received an acknowledge with tracking ID [" + getTrackingID() + "]");
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.CONFIGACK)) {
                            setTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));
                            updateConfigurationStatus(true);
                            log(Level.INFO, "Received a configuration acknowledge with tracking ID [" + getTrackingID() + "]");
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.NACK)) {
                            getNegativeAcknowledge().parse(mdElement);
                            log(Level.INFO, "Received a negative acknowledgement, reason: " + getNegativeAcknowledge().getReasonDescription());
                            updateStatus(false);
                            updateMBusRequestStatus();
                            updateConfigurationStatus(false);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.REJECT)) {
                            getReject().parse(mdElement);
                            updateStatus(false);
                            updateConfigurationStatus(false);
                            log(Level.INFO, "Message was rejected, reason: " + getReject().getReasonDescription());
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.LOADPR)) {
                            log(Level.INFO, "Received a loadProfile element.");
                            getLoadProfile().parse(mdElement);
                            receivedLoadProfile = true;
                            getAce4000().setMessageResults(false);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.LOADPRABS)) {
                            log(Level.INFO, "Received a loadProfile element.");
                            getLoadProfile().parse(mdElement);
                            receivedLoadProfile = true;
                            getAce4000().setMessageResults(false);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.MBUSBILLINGDATA)) {
                            log(Level.INFO, "Received MBus billing data.");
                            getMBusBillingData().setSlaveSerialNumber(ace4000.getPushedSerialNumber());
                            getMBusBillingData().parse(mdElement);
                            receivedMBusBillingRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.EVENT)) {
                            log(Level.INFO, "Received events");
                            getEventData().parse(mdElement);
                            receivedEvents = true;
                            getAce4000().setMessageResults(false);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.POWERFAIL)) {
                            log(Level.INFO, "Received power fail log");
                            getPowerFailLog().parse(mdElement);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.INSTVC)) {
                            log(Level.INFO, "Received instantaneous registers");
                            getInstantVoltAndCurrent().parse(mdElement);
                            receivedInstantRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.ANNOUNCE)) {
                            log(Level.INFO, "Received a device announcement.");
                            getAnnouncement().parse(mdElement);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.CURREADING)) {
                            log(Level.INFO, "Received current readings from meter.");
                            getCurrentReadings().parse(mdElement);
                            receivedCurrentRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.MBUSCREADING)) {
                            log(Level.INFO, "Received current readings from MBus meter.");
                            getMBCurrentReadings().setSlaveSerialNumber(ace4000.getPushedSerialNumber());
                            getMBCurrentReadings().parse(mdElement);
                            receivedMBusCurrentRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.RESFIRMWARE)) {
                            log(Level.INFO, "Received firmware versions.");
                            getFirmwareVersion().parse(mdElement);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.BILLDATA)) {
                            log(Level.INFO, "Received billing data from meter.");
                            getBillingData().parse(mdElement);
                            receivedBillingRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.CONFIGURATION)) {
                            log(Level.INFO, "Received configuration from meter.");
                            getFullMeterConfig().parse(mdElement);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.METERTIME)) {
                            log(Level.INFO, "Received timing parameters.");
                            getReadTime().parse(mdElement);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.MAXDEMAND)) {
                            log(Level.INFO, "Received maximum demand registers.");
                            getMaximumDemandRegisters().parse(mdElement);
                            receivedMaxDemandRegisters = true;
                        }
                    }
                } else {
                    throw new ApplicationException("Unknown tag found in xml response: " + nodes.item(0).getNodeName());
                }
            } else {
                throw new ApplicationException("Unknown tag found in xml response: " + element.getNodeName());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        } catch (DOMException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } catch (BusinessException e) {
            e.printStackTrace();
            throw e;
        }
        if (sendAck && requestsAllowed) {
            sendAcknowledge();
            sendAck = false;
        }
    }

    protected void log(Level level, String msg) {
        getAce4000().getLogger().log(level, msg);
    }

    protected int getTrackingID() {
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
        TimeZone timeZone = getAce4000().getDeviceTimeZone();
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

        Calendar meter = Calendar.getInstance(getAce4000().getDeviceTimeZone());

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

    /**
     * Collection of all received events.
     * Note that an announce message is also regarded as an event
     *
     * @return all events
     */
    public List<MeterEvent> getMeterEvents() {
        if (meterEvents == null) {
            meterEvents = new ArrayList<MeterEvent>();
        }
        return meterEvents;
    }

    /**
     * Returns all master registers: current readings, billing registers, maximum demand registers and instantaneous registers.
     *
     * @return master registers
     */
    public MeterReadingData getAllMasterRegisters() {
        MeterReadingData mrd;
        mrd = getCurrentReadings().getMrd();

        for (RegisterValue registerValue : getBillingData().getMrd().getRegisterValues()) {
            mrd.add(registerValue);
        }
        for (RegisterValue registerValue : getMaximumDemandRegisters().getMdr().getRegisterValues()) {
            mrd.add(registerValue);
        }
        for (RegisterValue registerValue : getInstantVoltAndCurrent().getMrd().getRegisterValues()) {
            mrd.add(registerValue);
        }
        return mrd;
    }

    /**
     * Returns registers for a slave: current readings and billing registers
     *
     * @param serialNumber the serial number identifying the slave meter
     * @return slave registers
     */
    public MeterReadingData getAllMBusRegisters(String serialNumber) {            //Fetch all current and billing registers
        MeterReadingData mrd;
        mrd = getMBCurrentReadings().getMrdPerSlave(serialNumber);
        if (getMBusBillingData().getMrdPerSlave(serialNumber) != null) {
            for (RegisterValue registerValue : getMBusBillingData().getMrdPerSlave(serialNumber).getRegisterValues()) {
                mrd.add(registerValue);
            }
        }
        return mrd;
    }

    /**
     * Return the registers for ALL slaves
     *
     * @return all slave registers
     */
    public MeterReadingData getAllMBusRegisters() {
        MeterReadingData mrd;
        mrd = getMBCurrentReadings().getMrd();

        for (RegisterValue registerValue : getMBusBillingData().getMrd().getRegisterValues()) {
            mrd.add(registerValue);
        }
        return mrd;
    }

    /**
     * Checks if the registers were received.
     * If not, request the missing registers.
     *
     * @return if the registers have been received or not
     * @throws IOException communication error
     */
    public boolean requestAllMasterRegisters(Date from, List<Register> registers) throws IOException {
        boolean received = true;
        if (!isReceivedBillingRegisters() && shouldRequestBillingRegisters(registers)) {
            sendBDRequest(from);
            received = false;
        }
        if (!isReceivedCurrentRegisters() && shouldRequestCurrentRegisters(registers)) {
            sendCurrentRegisterRequest();
            received = false;
        }
        if (!isReceivedInstantRegisters() && shouldRequestInstantaneousRegisters(registers)) {
            sendInstantVoltageAndCurrentRequest();
            received = false;
        }
        return received;
    }

    private boolean shouldRequestInstantaneousRegisters(List<Register> registers) {
        List<Integer> allowedCFields = new ArrayList<Integer>();
        allowedCFields.add(31);
        allowedCFields.add(51);
        allowedCFields.add(71);
        allowedCFields.add(32);
        allowedCFields.add(52);
        allowedCFields.add(72);
        allowedCFields.add(21);
        allowedCFields.add(41);
        allowedCFields.add(61);
        allowedCFields.add(23);
        allowedCFields.add(43);
        allowedCFields.add(63);
        allowedCFields.add(29);
        allowedCFields.add(49);
        allowedCFields.add(69);
        allowedCFields.add(85);
        allowedCFields.add(86);
        allowedCFields.add(87);

        for (Register register : registers) {
            ObisCode obisCode = register.getRegisterSpec().getDeviceObisCode();
            if (allowedCFields.contains(obisCode.getC()) && obisCode.getA() == 1 && obisCode.getB() == 0 && obisCode.getD() == 7 && obisCode.getE() == 0 && obisCode.getF() == 255) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldRequestCurrentRegisters(List<Register> registers) {
        for (Register register : registers) {
            ObisCode obisCode = register.getRegisterSpec().getDeviceObisCode();
            if (obisCode.getA() == 1 && obisCode.getB() == 0 && (obisCode.getC() == 1 || obisCode.getC() == 2) && obisCode.getD() == 8 && obisCode.getF() == 255) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if billing registers are defined on the RTU in EiServer.
     *
     * @param registers list of all registers defined on the RTU
     * @return true or false
     */
    private boolean shouldRequestBillingRegisters(List<Register> registers) {
        for (Register register : registers) {
            if (isBillingRegister(register)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBillingRegister(Register register) {
        return register.getRegisterSpec().getDeviceObisCode().getF() != 255;
    }

    /**
     * Checks if slave registers were received.
     * If not, request the missing registers.
     *
     * @return if the registers have been received or not
     * @throws IOException communication error
     */
    public boolean requestAllSlaveRegisters(Date from, HashMap<String, Device> slaves) throws IOException {
        List<com.energyict.mdw.amr.Register> allSlaveRegisters = new ArrayList<Register>();
        for (Device slave : slaves.values()) {
            allSlaveRegisters.addAll(slave.getRegisters());
        }

        boolean received = true;
        if (isReceivedMBusBillingRegisters() == null && shouldRequestBillingRegisters(allSlaveRegisters)) {
            sendMBusBillingDataRequest(from);
            received = false;
        }
        if (isReceivedMBusCurrentRegisters() == null && shouldRequestMBusCurrentRegisters(slaves)) {
            sendMBusCurrentRegistersRequest();
            received = false;
        }
        return received;
    }

    /**
     * Checks if there's more than just billing registers defined on the slave RTU.
     *
     * @param slaves list of slave RTU's
     * @return true or false
     */
    private boolean shouldRequestMBusCurrentRegisters(HashMap<String, Device> slaves) {
        for (Device slave : slaves.values()) {
            int numberOfBillingRegisters = 0;
            for (Register register : slave.getRegisters()) {
                numberOfBillingRegisters += isBillingRegister(register) ? 1 : 0;
            }
            if (slave.getRegisters().size() > numberOfBillingRegisters) {
                return true;
            }
        }
        return false;
    }
}