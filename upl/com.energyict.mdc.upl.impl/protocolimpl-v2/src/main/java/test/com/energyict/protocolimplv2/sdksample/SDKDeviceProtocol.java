package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.channels.EmptyConnectionType;
import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.channels.inbound.EIWebPlusConnectionType;
import com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.TcpIpPostDialConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioCaseModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioPEMPModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioPaknetModemConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.mdc.channels.sms.OutboundProximusSmsConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.security.DlmsSecuritySupport;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation of a DeviceProtocol which serves as a <i>guiding</i>
 * protocol for implementors
 * <p>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 13:55
 */
public class SDKDeviceProtocol implements DeviceProtocol {

    private static final String DEFAULT_OPTIONAL_PROPERTY_NAME = "defaultOptionalProperty";
    private static final String DELAY_AFTER_REQUEST_PROPERTY_NAME = "DelayAfterRequest";

    protected final CollectedDataFactory collectedDataFactory;
    protected final PropertySpecService propertySpecService;
    protected final NlsService nlsService;
    protected final Converter converter;
    /**
     * Will group this protocols' security features.
     * As an example the {@link DlmsSecuritySupport} component is used
     */
    private final DeviceProtocolSecurityCapabilities deviceProtocolSecurityCapabilities;
    protected Logger logger = Logger.getLogger(SDKDeviceProtocol.class.getSimpleName());
    /**
     * The {@link OfflineDevice} that holds all <i>necessary</i> information to perform the relevant ComTasks for this <i>session</i>
     */
    protected OfflineDevice offlineDevice;
    /**
     * The ComChannel that will be used to read/write.
     * Actual reading/writing needs to be performed on this object as logging/communicationStatistics are calculated based on
     * the calls you make on this.
     */
    private ComChannel comChannel;
    /**
     * Will hold the cache object of the Device related to this protocol
     */
    private DeviceProtocolCache deviceProtocolCache;
    /**
     * Keeps track of all the protocol properties <b>AND</b> the current deviceProtocolDialectProperties
     */
    private TypedProperties typedProperties = TypedProperties.empty();
    /**
     * The securityPropertySet that will be used for this session
     */
    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;

    public SDKDeviceProtocol(CollectedDataFactory collectedDataFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super();
        this.collectedDataFactory = collectedDataFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.deviceProtocolSecurityCapabilities = new DlmsSecuritySupport(propertySpecService);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.comChannel = comChannel;
        this.logger.log(Level.INFO, "Initializing DeviceProtocol for Device with serialNumber " + this.offlineDevice.getSerialNumber());
    }

    @Override
    public void terminate() {
        /*
        The order of disconnecting a session is:
        - logOff or daisyChainedLogOff (depending on the following ComTasks)
        - terminate
        - physically closing the connection
         */
        this.logger.log(Level.INFO, "Terminating DeviceProtocol SESSION");
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.values());
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        PropertySpec defaultOptional = UPLPropertySpecFactory.specBuilder(DEFAULT_OPTIONAL_PROPERTY_NAME, false, PropertyTranslationKeys.SDKSAMPLE_DEFAULT_OPTIONAL_PROPERTY, this.propertySpecService::booleanSpec).finish();
        PropertySpec delayAfterRequest = UPLPropertySpecFactory.specBuilder(DELAY_AFTER_REQUEST_PROPERTY_NAME, false, PropertyTranslationKeys.SDKSAMPLE_DELAY_AFTER_REQUEST_PROPERTY, this.propertySpecService::durationSpec).finish();
        return Arrays.asList(defaultOptional, delayAfterRequest);
    }

    @Override
    public void logOn() {
        String logOn = "DeviceProtocol logOn!";
        this.logger.log(Level.INFO, logOn);
        this.comChannel.write(logOn.getBytes());
    }

    @Override
    public void daisyChainedLogOn() {
        String logOn = "DeviceProtocol daisyChained logOn!";
        this.logger.log(Level.INFO, logOn);
        this.comChannel.write(logOn.getBytes());
    }

    @Override
    public void logOff() {
        String logOff = "DeviceProtocol logOff!";
        this.logger.log(Level.INFO, logOff);
        this.comChannel.write(logOff.getBytes());
    }

    @Override
    public void daisyChainedLogOff() {
        String logOff = "DeviceProtocol daisyChained logOff!";
        this.logger.log(Level.INFO, logOff);
        this.comChannel.write(logOff.getBytes());
    }

    @Override
    public String getSerialNumber() {
        this.logger.log(Level.INFO, "Getting the serialNumber of the device, actually just returning the one we got from the OfflineDevice");
        return this.offlineDevice.getSerialNumber();
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return this.deviceProtocolCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceProtocolCache = deviceProtocolCache;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>();
        // by default all loadProfileReaders are supported, only if the corresponding ObisCodeProperty matches, we mark it as not supported
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            this.logger.log(Level.INFO, "Fetching loadProfile configuration for loadProfile with ObisCode " + loadProfileReader.getProfileObisCode());
            CollectedLoadProfileConfiguration loadProfileConfiguration = this.collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
            if (!loadProfileReader.getProfileObisCode().equals(getIgnoredObisCode())) {
                loadProfileConfiguration.setChannelInfos(loadProfileReader.getChannelInfos());
            } else {
                this.logger.log(Level.INFO, "Marking loadProfile as not supported due to the value of the " + SDKLoadProfileProtocolDialectProperties.notSupportedLoadProfileObisCodePropertyName + " property");
                loadProfileConfiguration.setSupportedByMeter(false);
            }
        }
        return loadProfileConfigurations;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        //TODO
        return Collections.emptyList();
    }

    @Override
    public Date getTime() {
        Calendar cal = Calendar.getInstance();
        int timeDeviationPropertyForRead = (int) Temporals.toSeconds(getTimeDeviationPropertyForRead());
        cal.add(Calendar.SECOND, timeDeviationPropertyForRead);
        Date timeToReturn = cal.getTime();
        this.logger.info("Returning the based on the deviationProperty " + timeToReturn);
        return timeToReturn;
    }

    @Override
    public void setTime(Date timeToSet) {
        int timeDeviationForWrite = (int) Temporals.toSeconds(getTimeDeviationPropertyForWrite());
        if (timeDeviationForWrite == 0) {
            this.logger.log(Level.INFO, "Setting the time of the device to " + timeToSet);
            this.comChannel.write(timeToSet.toString().getBytes());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeToSet);
            calendar.add(Calendar.SECOND, timeDeviationForWrite);
            this.logger.log(
                    Level.INFO,
                    () -> "Setting the time of the device to " + calendar.getTime() + ". " + "This is the time added with the deviation property value of " + timeDeviationForWrite + " seconds");
            this.comChannel.write(calendar.getTime().toString().getBytes());
        }
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> collectedLogBooks = new ArrayList<>();
        simulateRealCommunicationIfApplicable();
        logBooks.forEach(logBookReader -> {
            CollectedLogBook collectedLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            Instant powerDownDate = Instant.now().minusSeconds(13);
            Instant powerUpDate = powerDownDate.plusSeconds(2);
            MeterProtocolEvent powerDown =
                    new MeterProtocolEvent(
                            Date.from(powerDownDate),
                            1,
                            13,
                            EndDeviceEventTypeMapping.POWERDOWN.getEventType(),
                            "The power went down for the SDK protocol",
                            1,
                            1);
            powerDown.addAdditionalInformation("Voltage", "0V");
            powerDown.addAdditionalInformation("Current", "0A");
            powerDown.addAdditionalInformation("Max. power", "35461W");
            powerDown.addAdditionalInformation("Reason", "Testing purpose");
            MeterProtocolEvent powerUp =
                    new MeterProtocolEvent(
                            Date.from(powerUpDate),
                            2,
                            17,
                            EndDeviceEventTypeMapping.POWERUP.getEventType(),
                            "The power went back up",
                            1,
                            2);
            powerUp.addAdditionalInformation("Voltage", "231V");
            powerUp.addAdditionalInformation("Current", "2.61A");
            powerUp.addAdditionalInformation("Max. power", "35461W");
            powerUp.addAdditionalInformation("Reason", "Testing purpose");
            collectedLogBook.setCollectedMeterEvents(Arrays.asList(powerDown, powerUp, getAlarmEvent()));
            collectedLogBooks.add(collectedLogBook);
        });
        return collectedLogBooks;
    }

    private void simulateRealCommunicationIfApplicable() {
        getDelayAfterRequest().ifPresent(delayAfterRequest -> {
            logger.info("Simulating real communication, waiting for " + delayAfterRequest + " ...");
            try {
                Thread.sleep(delayAfterRequest.toMillis());
            } catch (InterruptedException e) {
                logger.severe("Something really horrible went wrong during the simulation of real communication ...");
                logger.severe(e.getMessage());
            }
        });
    }

    private boolean checkMatchingEvent(String eventTypeMask, String inputEvent) {
        String testVal;
        String regexVal;
        if (eventTypeMask.contains("*")) {
            testVal = inputEvent;
            regexVal = escape(eventTypeMask).replaceAll("\\*", "\\\\d+");     //Replace the * wildcards with proper regex wildcard
        } else {
            return eventTypeMask.equals(inputEvent);
        }
        return testVal.matches(regexVal);
    }

    private String escape(String cimCode) {
        return cimCode.replaceAll("\\.", "\\\\.");
    }

    private MeterProtocolEvent getAlarmEvent() {
        String TAMPER_RAISE_ON_EVENT_MASK = "*.12.*.257";
        String TAMPER_CLEARING_EVENT_MASK = "*.12.*.219";
        Instant timeStamp = Instant.now();
        MeterProtocolEvent event =
                new MeterProtocolEvent(
                        Date.from(timeStamp),
                        0,
                        6,
                        EndDeviceEventTypeMapping.OTHER.getEventType(),
                        "A random event has occured",
                        1,
                        3);

        String alarmEventType = getDeviceAlarmEventType();
        if (checkMatchingEvent(TAMPER_RAISE_ON_EVENT_MASK, alarmEventType)) {
            event =
                    new MeterProtocolEvent(
                            Date.from(timeStamp),
                            23,
                            7,
                            EndDeviceEventTypeMapping.TAMPER.getEventType(),
                            "A device tamper has occured",
                            1,
                            3);
        } else if (checkMatchingEvent(TAMPER_CLEARING_EVENT_MASK, alarmEventType)) {
            event =
                    new MeterProtocolEvent(
                            Date.from(timeStamp),
                            65,
                            8,
                            EndDeviceEventTypeMapping.TAMPER_CLEARED.getEventType(),
                            "A device tamper has occured",
                            1,
                            3);
        } else {
            //just fall through
        }
        event.addAdditionalInformation("Reason", "Testing purpose");
        return event;
    }

    private Optional<Duration> getDelayAfterRequest() {
        return Optional.ofNullable((Duration) this.typedProperties.getProperty(DELAY_AFTER_REQUEST_PROPERTY_NAME));
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.<DeviceMessageSpec>asList(
                ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND.get(this.propertySpecService, this.nlsService, this.converter),
                ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME.get(this.propertySpecService, this.nlsService, this.converter),
                ContactorDeviceMessage.CONTACTOR_ARM.get(this.propertySpecService, this.nlsService, this.converter),
                ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.propertySpecService, this.nlsService, this.converter),
                ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter),
                ContactorDeviceMessage.CONTACTOR_OPEN.get(this.propertySpecService, this.nlsService, this.converter),
                ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_ACTIVATE.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_URL_AND_ACTIVATE.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return "";  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(
                new SDKLoadProfileProtocolDialectProperties(propertySpecService, nlsService),
                new SDKStandardDeviceProtocolDialectProperties(propertySpecService, nlsService),
                new SDKTimeDeviceProtocolDialectProperties(propertySpecService, nlsService),
                new SDKTopologyTaskProtocolDialectProperties(propertySpecService, nlsService),
                new SDKFirmwareTaskProtocolDialectProperties(propertySpecService, nlsService),
                new SDKCalendarTaskProtocolDialectProperties(propertySpecService, nlsService),
                new SDKBreakerTaskProtocolDialectProperties(propertySpecService, nlsService),
                new SDKDeviceAlarmProtocolDialectProperties(propertySpecService, nlsService)
        );
    }

    @Override
    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        this.logger.log(Level.INFO, "Adding the deviceProtocolDialect properties to the DeviceProtocol instance.");
        this.typedProperties.setAllProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.logger.log(Level.INFO, "Adding the deviceProtocolSecurity properties to the DeviceProtocol instance.");
        this.deviceProtocolSecurityPropertySet = deviceProtocolSecurityPropertySet;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return this.deviceProtocolSecurityCapabilities.getSecurityProperties();
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return this.deviceProtocolSecurityCapabilities.getClientSecurityPropertySpec();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return this.deviceProtocolSecurityCapabilities.getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return this.deviceProtocolSecurityCapabilities.getEncryptionAccessLevels();
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
        return this.deviceProtocolSecurityCapabilities.getSecurityPropertySpec(name);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        final CollectedTopology collectedTopology = this.collectedDataFactory.createCollectedTopology(new DeviceIdentifierById(this.offlineDevice.getId()));
        if (!"".equals(getSlaveOneSerialNumber())) {
            collectedTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(getSlaveOneSerialNumber()));
        }
        if (!"".equals(getSlaveTwoSerialNumber())) {
            collectedTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(getSlaveTwoSerialNumber()));
        }
        return collectedTopology;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT SDK DeviceProtocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-06 14:27:09 +0100 (Fri, 06 Nov 2015) $";
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.logger.log(Level.INFO, "Adding the properties to the DeviceProtocol instance.");
        this.typedProperties.setAllProperties(TypedProperties.copyOf(properties));
    }

    private ObisCode getIgnoredObisCode() {
        return (ObisCode) this.typedProperties.getProperty(SDKLoadProfileProtocolDialectProperties.notSupportedLoadProfileObisCodePropertyName, ObisCode.fromString("0.0.0.0.0.0"));
    }

    private TemporalAmount getTimeDeviationPropertyForRead() {
        return this.typedProperties.getTypedProperty(SDKTimeDeviceProtocolDialectProperties.CLOCK_OFFSET_TO_READ_PROPERTY_NAME, Duration.ofSeconds(0));
    }

    private TemporalAmount getTimeDeviationPropertyForWrite() {
        return this.typedProperties.getTypedProperty(SDKTimeDeviceProtocolDialectProperties.CLOCK_OFFSET_TO_WRITE_PROPERTY_NAME, Duration.ofSeconds(0));
    }

    private String getDeviceAlarmEventType() {
        return this.typedProperties.getTypedProperty(SDKDeviceAlarmProtocolDialectProperties.DEVICE_ALARM_EVENT_TYPE_PROPERTY_NAME, SDKDeviceAlarmProtocolDialectProperties.DEFAULT_EVENT_TYPE_VALUE);
    }


    private String getSlaveOneSerialNumber() {
        return (String) this.typedProperties.getProperty(SDKTopologyTaskProtocolDialectProperties.slaveOneSerialNumberPropertyName, "");
    }

    private String getSlaveTwoSerialNumber() {
        return (String) this.typedProperties.getProperty(SDKTopologyTaskProtocolDialectProperties.slaveTwoSerialNumberPropertyName, "");
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        CollectedCalendar collectedCalendar = this.collectedDataFactory.createCalendarCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
        collectedCalendar.setActiveCalendar((String) this.typedProperties.getProperty(SDKCalendarTaskProtocolDialectProperties.activeCalendarName, ""));
        collectedCalendar.setPassiveCalendar((String) this.typedProperties.getProperty(SDKCalendarTaskProtocolDialectProperties.passiveCalendarName, ""));
        return collectedCalendar;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        CollectedBreakerStatus breakerStatusCollectedData = collectedDataFactory.createBreakerStatusCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
        String breakerStatus = (String) this.typedProperties.getProperty(SDKBreakerTaskProtocolDialectProperties.breakerStatus, BreakerStatus.CONNECTED.name());
        breakerStatusCollectedData.setBreakerStatus(BreakerStatus.valueOf(breakerStatus.toUpperCase()));
        return breakerStatusCollectedData;
    }

    @Override
    public CollectedCreditAmount getCreditAmount() {
        CollectedCreditAmount creditAmountCollectedData = collectedDataFactory.createCreditAmountCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
        BigDecimal creditAmount = (BigDecimal) this.typedProperties.getProperty(SDKCreditTaskProtocolDialectProperties.creditAmount, new BigDecimal(0));
        creditAmountCollectedData.setCreditAmount(creditAmount);
        String creditType = (String) this.typedProperties.getProperty(SDKCreditTaskProtocolDialectProperties.creditType, "");
        creditAmountCollectedData.setCreditType(creditType);
        return creditAmountCollectedData;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = this.collectedDataFactory.createFirmwareVersionsCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
        firmwareVersionsCollectedData.setActiveMeterFirmwareVersion((String) this.typedProperties.getProperty(SDKFirmwareTaskProtocolDialectProperties.activeMeterFirmwareVersion, ""));
        firmwareVersionsCollectedData.setPassiveMeterFirmwareVersion((String) this.typedProperties.getProperty(SDKFirmwareTaskProtocolDialectProperties.passiveMeterFirmwareVersion, ""));
        firmwareVersionsCollectedData.setActiveCommunicationFirmwareVersion((String) this.typedProperties.getProperty(SDKFirmwareTaskProtocolDialectProperties.activeCommunicationFirmwareVersion, ""));
        firmwareVersionsCollectedData.setPassiveCommunicationFirmwareVersion((String) this.typedProperties.getProperty(SDKFirmwareTaskProtocolDialectProperties.passiveCommunicationFirmwareVersion, ""));

        return firmwareVersionsCollectedData;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new EmptyConnectionType(),
                new OutboundProximusSmsConnectionType(propertySpecService),
                new CTRInboundDialHomeIdConnectionType(propertySpecService),
                new SioSerialConnectionType(propertySpecService),
                new SioOpticalConnectionType(propertySpecService),
                new SioAtModemConnectionType(propertySpecService),
                new SioCaseModemConnectionType(propertySpecService),
                new SioPaknetModemConnectionType(propertySpecService),
                new SioPEMPModemConnectionType(propertySpecService),
                new RxTxSerialConnectionType(propertySpecService),
                new RxTxAtModemConnectionType(propertySpecService),
                new RxTxOpticalConnectionType(propertySpecService),
                new InboundProximusSmsConnectionType(propertySpecService),
                new InboundIpConnectionType(),
                new OutboundUdpConnectionType(propertySpecService),
                new OutboundTcpIpConnectionType(propertySpecService),
                new TcpIpPostDialConnectionType(propertySpecService),
                new EIWebConnectionType(propertySpecService),
                new EIWebPlusConnectionType(propertySpecService)
        );
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }
}