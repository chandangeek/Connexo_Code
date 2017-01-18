package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.UnableToCreateConnectionType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocolimplv2.securitysupport.DlmsSecuritySupport;
import com.energyict.protocols.impl.channels.ConnectionTypeRule;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private Logger logger = Logger.getLogger(SDKDeviceProtocol.class.getSimpleName());

    private final ProtocolPluggableService protocolPluggableService;
    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;

    /**
     * The {@link OfflineDevice} that holds all <i>necessary</i> information to perform the relevant ComTasks for this <i>session</i>
     */
    private OfflineDevice offlineDevice;
    /**
     * The ComChannel that will be used to read/write.
     * Actual reading/writing needs to be performed on this object as logging/communicationStatistics are calculated based on
     * the calls you make on this.
     */
    private ComChannel comChannel;
    /**
     * Will group this protocols' security features.
     * As an example the {@link DlmsSecuritySupport} component is used
     */
    private DeviceProtocolSecurityCapabilities deviceProtocolSecurityCapabilities;
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

    private final String delayAfterRequest = "DelayAfterRequest";

    @Inject
    public SDKDeviceProtocol(
            ProtocolPluggableService protocolPluggableService, Thesaurus thesaurus, PropertySpecService propertySpecService,
            IdentificationService identificationService, CollectedDataFactory collectedDataFactory,
            DlmsSecuritySupport dlmsSecuritySupport) {
        super();
        this.protocolPluggableService = protocolPluggableService;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.deviceProtocolSecurityCapabilities = dlmsSecuritySupport;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.comChannel = comChannel;
        this.logger.log(Level.INFO, "Initializing DeviceProtocol for Device with serialNumber " + this.offlineDevice.getSerialNumber());
        simulateRealCommunicationIfApplicable();
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
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
        simulateRealCommunicationIfApplicable();
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.values());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(
                this.propertySpecService
                        .booleanSpec()
                        .named("defaultOptionalProperty", SDKTranslationKeys.DEFAULT_OPTIONAL_PROPERTY)
                        .fromThesaurus(this.thesaurus)
                        .finish());
        // codetables not supported
        // optionalProperties.add(propertySpecService.referencePropertySpec("SDKCodeTableProperty", false, FactoryIds.CODE));
        optionalProperties.add(
                this.propertySpecService
                        .obisCodeSpec()
                        .named(SDKTranslationKeys.SDKOBISCODEPROPERTY)
                        .fromThesaurus(this.thesaurus)
                        .addValues(
                                ObisCode.fromString("1.0.1.8.0.255"),
                                ObisCode.fromString("1.0.1.8.1.255"),
                                ObisCode.fromString("1.0.1.8.2.255"),
                                ObisCode.fromString("1.0.2.8.0.255"),
                                ObisCode.fromString("1.0.2.8.1.255"),
                                ObisCode.fromString("1.0.2.8.2.255"))
                        .markExhaustive()
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(SDKTranslationKeys.SDKBIGDECIMALWITHDEFAULT)
                        .fromThesaurus(this.thesaurus)
                        .setDefaultValue(new BigDecimal("666.156"))
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .specForValuesOf(new DateAndTimeFactory())
                        .named(SDKTranslationKeys.SDKDATETIMEPROPERTY)
                        .fromThesaurus(this.thesaurus)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .temporalAmountSpec()
                        .named(delayAfterRequest, SDKTranslationKeys.DELAY_AFTER_REQUEST)
                        .fromThesaurus(this.thesaurus)
                        .setDefaultValue(TimeDuration.NONE)
                        .finish());
        return optionalProperties;
    }

    @Override
    public void logOn() {
        String logOn = "DeviceProtocol logOn!";
        this.logger.log(Level.INFO, logOn);
        this.comChannel.write(logOn.getBytes());
        simulateRealCommunicationIfApplicable();
    }

    @Override
    public void daisyChainedLogOn() {
        String logOn = "DeviceProtocol daisyChained logOn!";
        this.logger.log(Level.INFO, logOn);
        this.comChannel.write(logOn.getBytes());
        simulateRealCommunicationIfApplicable();
    }

    @Override
    public void logOff() {
        String logOff = "DeviceProtocol logOff!";
        this.logger.log(Level.INFO, logOff);
        this.comChannel.write(logOff.getBytes());
        simulateRealCommunicationIfApplicable();
    }

    @Override
    public void daisyChainedLogOff() {
        String logOff = "DeviceProtocol daisyChained logOff!";
        this.logger.log(Level.INFO, logOff);
        this.comChannel.write(logOff.getBytes());
        simulateRealCommunicationIfApplicable();
    }

    @Override
    public String getSerialNumber() {
        this.logger.log(Level.INFO, "Getting the serialNumber of the device, actually just returning the one we got from the OfflineDevice");
        return this.offlineDevice.getSerialNumber();
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceProtocolCache = deviceProtocolCache;
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return this.deviceProtocolCache;
    }

    @Override
    public void setTime(Date timeToSet) {
        if (getTimeDeviationPropertyForWrite().getSeconds() == 0) {
            this.logger.log(Level.INFO, "Setting the time of the device to " + timeToSet);
            this.comChannel.write(timeToSet.toString().getBytes());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeToSet);
            calendar.add(Calendar.SECOND, getTimeDeviationPropertyForWrite().getSeconds());
            this.logger.log(Level.INFO, "Setting the time of the device to " + calendar.getTime() + ". " +
                    "This is the time added with the deviation property value of " + getTimeDeviationPropertyForWrite().getSeconds() + " seconds");
            this.comChannel.write(calendar.getTime().toString().getBytes());
        }
        simulateRealCommunicationIfApplicable();
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>();
        // by default all loadProfileReaders are supported, only if the corresponding ObisCodeProperty matches, we mark it as not supported
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            this.logger.log(Level.INFO, "Fetching loadProfile configuration for loadProfile with ObisCode " + loadProfileReader.getProfileObisCode());
            CollectedLoadProfileConfiguration loadProfileConfiguration = this.collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getDeviceIdentifier());
            if (!loadProfileReader.getProfileObisCode().equals(getIgnoredObisCode())) {
                loadProfileConfiguration.setChannelInfos(loadProfileReader.getChannelInfos());
            } else {
                this.logger.log(Level.INFO, "Marking loadProfile as not supported due to the value of the " + SDKLoadProfileDialectProperties.ActualFields.NOT_SUPPORTED_LOAD_PROFILE.propertySpecName() + " property");
                loadProfileConfiguration.setSupportedByMeter(false);
            }
        }
        simulateRealCommunicationIfApplicable();
        return loadProfileConfigurations;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        //TODO
        simulateRealCommunicationIfApplicable();
        return Collections.emptyList();
    }

    @Override
    public Date getTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, getTimeDeviationPropertyForRead().getSeconds());
        Date timeToReturn = cal.getTime();
        this.logger.info("Returning the based on the deviationProperty " + timeToReturn);
        simulateRealCommunicationIfApplicable();
        return timeToReturn;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        //TODO
        simulateRealCommunicationIfApplicable();
        return Collections.emptyList();
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.of(
                DeviceMessageId.ACTIVITY_CALENDER_SEND,
                DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME,
                DeviceMessageId.CONTACTOR_ARM,
                DeviceMessageId.CONTACTOR_CLOSE,
                DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE,
                DeviceMessageId.CONTACTOR_OPEN,
                DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER,
                DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE,
                DeviceMessageId.FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList collectedMessageList = collectedDataFactory.createCollectedMessageList(pendingMessages);
        pendingMessages.stream().forEach(offlineDeviceMessage -> {
            CollectedMessage collectedMessage = collectedDataFactory.createCollectedMessage(offlineDeviceMessage.getIdentifier());
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            collectedMessageList.addCollectedMessages(collectedMessage);
        });
        simulateRealCommunicationIfApplicable();
        return collectedMessageList;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return executePendingMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(
                new SDKLoadProfileProtocolDialect(this.thesaurus, this.propertySpecService),
                new SDKStandardProtocolDialect(this.thesaurus, this.propertySpecService),
                new SDKTimeProtocolDialect(this.thesaurus, this.propertySpecService),
                new SDKTopologyTaskProtocolDialect(this.thesaurus, this.propertySpecService),
                new SDKFirmwareProtocolDialect(this.thesaurus, this.propertySpecService),
                new SDKCalendarProtocolDialect(this.thesaurus, this.propertySpecService),
                new SDKBreakerProtocolDialect(this.thesaurus, this.propertySpecService)
        );
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        this.logger.log(Level.INFO, "Adding the deviceProtocolDialect properties to the DeviceProtocol instance.");
        this.typedProperties.setAllProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.logger.log(Level.INFO, "Adding the deviceProtocolSecurity properties to the DeviceProtocol instance.");
        this.deviceProtocolSecurityPropertySet = deviceProtocolSecurityPropertySet;
    }

    @Override
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        return this.deviceProtocolSecurityCapabilities.getCustomPropertySet();
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
        simulateRealCommunicationIfApplicable();
        return Collections.emptyList();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        final CollectedTopology collectedTopology = this.collectedDataFactory.createCollectedTopology(this.offlineDevice.getDeviceIdentifier());
        if (!"".equals(getSlaveOneSerialNumber())) {
            collectedTopology.addSlaveDevice(this.identificationService.createDeviceIdentifierBySerialNumber(getSlaveOneSerialNumber()));
        }
        if (!"".equals(getSlaveTwoSerialNumber())) {
            collectedTopology.addSlaveDevice(this.identificationService.createDeviceIdentifierBySerialNumber(getSlaveTwoSerialNumber()));
        }
        simulateRealCommunicationIfApplicable();
        return collectedTopology;
    }

    @Override
    public String getProtocolDescription() {
        return "EICT SDK DeviceProtocol";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.logger.log(Level.INFO, "Adding the properties to the DeviceProtocol instance.");
        this.typedProperties.setAllProperties(properties);
    }

    private ObisCode getIgnoredObisCode() {
        return (ObisCode) this.typedProperties.getProperty(SDKLoadProfileDialectProperties.ActualFields.NOT_SUPPORTED_LOAD_PROFILE.propertySpecName(), ObisCode.fromString("0.0.0.0.0.0"));
    }

    private TimeDuration getTimeDeviationPropertyForRead() {
        return (TimeDuration) this.typedProperties.getProperty(SDKTimeDialectProperties.ActualFields.CLOCK_OFFSET_WHEN_WRITING.propertySpecName(), new TimeDuration(0));
    }

    private TimeDuration getTimeDeviationPropertyForWrite() {
        return (TimeDuration) this.typedProperties.getProperty(SDKTimeDialectProperties.ActualFields.CLOCK_OFFSET_WHEN_READING.propertySpecName(), new TimeDuration(0));
    }

    private String getSlaveOneSerialNumber() {
        return (String) this.typedProperties.getProperty(SDKTopologyTaskProtocolDialect.slaveOneSerialNumberPropertyName, "");
    }

    private String getSlaveTwoSerialNumber() {
        return (String) this.typedProperties.getProperty(SDKTopologyTaskProtocolDialect.slaveTwoSerialNumberPropertyName, "");
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> connectionTypes = new ArrayList<>();
        for (ConnectionTypeRule connectionTypeRule : ConnectionTypeRule.values()) {
            try {
                connectionTypes.add(this.protocolPluggableService.createConnectionType(connectionTypeRule.getProtocolTypeClass().getName()));
            } catch (UnableToCreateConnectionType e) {
                e.printStackTrace(System.err);
            }
        }
        return connectionTypes;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = this.collectedDataFactory.createFirmwareVersionsCollectedData(offlineDevice.getDeviceIdentifier());
        firmwareVersionsCollectedData.setActiveMeterFirmwareVersion((String) this.typedProperties.getProperty(SDKFirmwareDialectProperties.ActualFields.ACTIVE_METER_FIRMWARE_VERSION.propertySpecName(), ""));
        firmwareVersionsCollectedData.setPassiveMeterFirmwareVersion((String) this.typedProperties.getProperty(SDKFirmwareDialectProperties.ActualFields.PASSIVE_METER_FIRMWARE_VERSION.propertySpecName(), ""));
        firmwareVersionsCollectedData.setActiveCommunicationFirmwareVersion((String) this.typedProperties.getProperty(SDKFirmwareDialectProperties.ActualFields.ACTIVE_COMMUNICATION_FIRMWARE_VERSION.propertySpecName(), ""));
        firmwareVersionsCollectedData.setPassiveCommunicationFirmwareVersion((String) this.typedProperties.getProperty(SDKFirmwareDialectProperties.ActualFields.PASSIVE_COMMUNICATION_FIRMWARE_VERSION.propertySpecName(), ""));
        simulateRealCommunicationIfApplicable();
        return firmwareVersionsCollectedData;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        CollectedCalendar collectedCalendar = this.collectedDataFactory.createCalendarCollectedData(this.offlineDevice.getDeviceIdentifier());
        collectedCalendar.setActiveCalendar((String) this.typedProperties.getProperty(SDKCalendarDialectProperties.ActualFields.ACTIVE_CALENDAR_NAME.propertySpecName(), ""));
        collectedCalendar.setPassiveCalendar((String) this.typedProperties.getProperty(SDKCalendarDialectProperties.ActualFields.PASSIVE_CALENDAR_NAME.propertySpecName(), ""));
        this.simulateRealCommunicationIfApplicable();
        return collectedCalendar;
    }

    private void simulateRealCommunicationIfApplicable(){
        TimeDuration delayAfterRequest = getDelayAfterRequest();
        if (!delayAfterRequest.isEmpty()) {
            try {
                logger.info("Simulating real communication, waiting for " + delayAfterRequest + " ...");
                Thread.sleep(delayAfterRequest.getMilliSeconds());
            } catch (InterruptedException e) {
                logger.severe("Something really horrible occurred during the simulation of real communication ...");
                logger.severe(e.getMessage());
            }
        }
    }

    private TimeDuration getDelayAfterRequest(){
        return (TimeDuration) this.typedProperties.getProperty(delayAfterRequest, TimeDuration.NONE);
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        CollectedBreakerStatus breakerStatusCollectedData = collectedDataFactory.createBreakerStatusCollectedData(offlineDevice.getDeviceIdentifier());
        String breakerStatus = (String) this.typedProperties.getProperty(SDKBreakerDialectProperties.ActualFields.BREAKER_STATUS.propertySpecName(), "CONNECTED");
        breakerStatusCollectedData.setBreakerStatus(BreakerStatus.valueOf(breakerStatus.toUpperCase()));
        simulateRealCommunicationIfApplicable();
        return breakerStatusCollectedData;
    }
}