package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.tasks.ACE4000DeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
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
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.ace4000.messages.ACE4000Messaging;
import com.energyict.protocolimplv2.ace4000.objects.ObjectFactory;
import com.energyict.protocolimplv2.ace4000.requests.ReadFirmwareVersion;
import com.energyict.protocolimplv2.ace4000.requests.ReadLoadProfile;
import com.energyict.protocolimplv2.ace4000.requests.ReadMBusRegisters;
import com.energyict.protocolimplv2.ace4000.requests.ReadMeterEvents;
import com.energyict.protocolimplv2.ace4000.requests.ReadRegisters;
import com.energyict.protocolimplv2.ace4000.requests.SetTime;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.mdc.identifiers.LoadProfileIdentifierById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 6/11/12
 * Time: 16:46
 * Author: khe
 */
public class ACE4000Outbound extends ACE4000 implements DeviceProtocol {

    private OfflineDevice offlineDevice;
    private DeviceProtocolCache deviceCache;
    private Logger logger;
    private Long cachedMeterTimeDifference = null;
    private DeviceProtocolSecurityPropertySet securityProperties;
    private ACE4000Messaging messageProtocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final NlsService nlsService;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;

    public ACE4000Outbound(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor) {
        super(propertySpecService);
        this.nlsService = nlsService;
        this.converter = converter;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.calendarExtractor = calendarExtractor;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        setAce4000Connection(new ACE4000Connection(comChannel, this, false));
    }

    @Override
    public String getSerialNumber() {
        //Return the configured serial number for the basic check task.
        //We already know that the serial number is correct because the inbound session successfully identified a device in EIServer, leading up to this outbound session.
        return getConfiguredSerialNumber();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierById(getOfflineDevice().getId());
    }

    @Override
    public String getProtocolDescription() {
        return "Actaris ACE4000 MeterXML";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-12-06 13:29:39 +0100 (Tue, 06 Dec 2016)$";
    }

    @Override
    public String getConfiguredSerialNumber() {
        return getOfflineDevice().getSerialNumber();
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            if (isMaster(loadProfileReader.getMeterSerialNumber())) {     //Master device
                ObisCode profileObisCode = loadProfileReader.getProfileObisCode();
                CollectedLoadProfileConfiguration config = this.collectedDataFactory.createCollectedLoadProfileConfiguration(profileObisCode, getConfiguredSerialNumber());
                if (!profileObisCode.equals(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE)) {                        //Only one LP is supported
                    config.setSupportedByMeter(false);
                } else {
                    List<OfflineLoadProfile> offlineLoadProfiles = getOfflineDevice().getAllOfflineLoadProfiles();
                    if (offlineLoadProfiles != null && !offlineLoadProfiles.isEmpty()) {
                        OfflineLoadProfile offlineLoadProfile = getOfflineLoadProfile(offlineLoadProfiles, DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);
                        long profileInterval = Temporals.toMilliSeconds(offlineLoadProfile.getInterval());
                        Date toDate = new Date();
                        Date fromDate = new Date(toDate.getTime() - (2 * profileInterval)); // get the last interval from date
                        ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(this, fromDate, toDate, issueFactory);
                        List<CollectedLoadProfile> collectedLoadProfiles = readLoadProfileRequest.request(loadProfileReader);
                        if (collectedLoadProfiles != null && !collectedLoadProfiles.isEmpty()) {
                            config.setChannelInfos(collectedLoadProfiles.get(0).getChannelInfo());
                        } else { // if we are not able to read the channelInfos from device then return the ones configured in EIMaster and skip validation of channelInfos
                            config.setChannelInfos(loadProfileReader.getChannelInfos());
                        }
                        getObjectFactory().resetLoadProfile();
                    }
                }
                result.add(config);
            } else {
                //Slave doesn't support
                CollectedLoadProfileConfiguration slaveConfig = this.collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), getConfiguredSerialNumber());
                slaveConfig.setSupportedByMeter(false);
                result.add(slaveConfig);
            }
        }
        return result;
    }

    private OfflineLoadProfile getOfflineLoadProfile(List<OfflineLoadProfile> offlineLoadProfiles, ObisCode genericLoadProfileObiscode) {
        for (OfflineLoadProfile offlineLoadProfile : offlineLoadProfiles) {
            if (offlineLoadProfile.getObisCode().equals(genericLoadProfileObiscode)) {
                return offlineLoadProfile;
            }
        }
        return null;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfiles) {
            if (isMaster(loadProfileReader.getMeterSerialNumber())) {   //Master device
                ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(this, issueFactory);
                result.addAll(readLoadProfileRequest.request(loadProfileReader));
            } else {    //Slave device
                CollectedLoadProfile collectedLoadProfile = collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode(), offlineDevice.getDeviceIdentifier()));
                Issue warning = this.issueFactory.createWarning(loadProfileReader, "loadProfileXIssue", loadProfileReader.getProfileObisCode(), "MBus slave device doesn't support load profiles");
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, warning);
                result.add(collectedLoadProfile);
            }
        }
        return result;
    }

    public Long getCachedMeterTimeDifference() {
        return cachedMeterTimeDifference;
    }

    public void setCachedMeterTimeDifference(Long cachedMeterTimeDifference) {
        this.cachedMeterTimeDifference = cachedMeterTimeDifference;
    }

    @Override
    public Date getTime() {
        if (cachedMeterTimeDifference == null || cachedMeterTimeDifference == 0) {    //null means it has not been received
            return new Date();  //Can't request the device time
        } else {
            return getObjectFactory().convertMeterDateToSystemDate((System.currentTimeMillis() - cachedMeterTimeDifference) / 1000);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        SetTime setTimeRequest = new SetTime(this);
        setTimeRequest.request(timeToSet);
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return this.deviceCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceCache = deviceProtocolCache;     //Unused, always empty
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessageProtocol().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageProtocol().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageProtocol().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return getMessageProtocol().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return getMessageProtocol().prepareMessageContext(device, offlineDevice, deviceMessage);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new ACE4000DeviceProtocolDialect(this.getPropertySpecService(), nlsService));
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(
                UPLPropertySpecFactory
                    .specBuilder(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, false, PropertyTranslationKeys.V2_ACE4000_CALL_HOME_ID, this.getPropertySpecService()::stringSpec)
                    .finish());
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.getProperties().setAllProperties(properties);
    }

    @Override
    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        getProperties().setAllProperties(dialectProperties);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<>();

        boolean requestMBusRegisters = false;
        for (OfflineRegister register : registers) {
            if (!isMaster(register.getSerialNumber())) {
                requestMBusRegisters = true;
                break;
            }
        }

        //Read MBus slave registers
        if (requestMBusRegisters) {
            ReadMBusRegisters readMBusRegistersRequest = new ReadMBusRegisters(this, collectedDataFactory, issueFactory);
            result.addAll(readMBusRegistersRequest.request(registers));
        }

        //Read master registers
        ReadRegisters readRegistersRequest = new ReadRegisters(this, collectedDataFactory, issueFactory);
        result.addAll(readRegistersRequest.request(registers));
        return result;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        if (!objectFactory.getAllSlaveSerialNumbers().isEmpty()) {
            //Requesting MBus registers to have an idea which MBus devices are connected :)
            ReadMBusRegisters readMBusRegistersRequest = new ReadMBusRegisters(this, collectedDataFactory, issueFactory);
            readMBusRegistersRequest.request(new ArrayList<>());
        }

        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(offlineDevice.getId());
        final CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(deviceIdentifier);
        for (String slaveSerialNumber : objectFactory.getAllSlaveSerialNumbers()) {
            deviceTopology.addSlaveDevice(new DialHomeIdDeviceIdentifier(slaveSerialNumber));
        }
        return deviceTopology;
    }

    public ObjectFactory getObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new ObjectFactory(this, collectedDataFactory);
            objectFactory.setInbound(false);
        }
        return objectFactory;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    public NlsService getNlsService() {
        return nlsService;
    }

    @Override
    public TimeZone getTimeZone() {
        Object timeZoneProperty = offlineDevice.getAllProperties().getProperty("TimeZone");
        if (timeZoneProperty == null) {
            return TimeZone.getDefault();
        } else {
            return TimeZone.getTimeZone((String) timeZoneProperty);
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        List<DeviceProtocolCapabilities> capabilities = new ArrayList<>();
        capabilities.add(DeviceProtocolCapabilities.PROTOCOL_MASTER);
        capabilities.add(DeviceProtocolCapabilities.PROTOCOL_SESSION);
        return capabilities;
    }

    /**
     * Only one LogbookReader should be configured
     */
    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        LogBookReader logBookReader = logBooks.get(0);
        if (isMaster(logBookReader.getMeterSerialNumber())) {
            ReadMeterEvents readMeterEventsRequest = new ReadMeterEvents(this, issueFactory);
            return readMeterEventsRequest.request(logBookReader);
        } else {
            List<CollectedLogBook> result = new ArrayList<>();
            CollectedLogBook deviceLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            deviceLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), "MBus slave device doesn't support events"));
            result.add(deviceLogBook);
            return result;
        }
    }

    private boolean isMaster(String serialNumber) {
        return offlineDevice.getSerialNumber().equalsIgnoreCase(serialNumber);
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    //No log on / log off needed...
    @Override
    public void logOn() {
    }

    @Override
    public void daisyChainedLogOn() {
    }

    @Override
    public void logOff() {
    }

    @Override
    public void daisyChainedLogOff() {
    }

    @Override
    public void terminate() {
    }

    /**
     * GPRS communication has no security.
     * The security set can contain a password, this is to be used for SMS communication.
     */
    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        securityProperties = deviceProtocolSecurityPropertySet;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.singletonList(new InboundIpConnectionType());
    }

    public ACE4000Messaging getMessageProtocol() {
        if (this.messageProtocol == null) {
            this.messageProtocol = new ACE4000Messaging(this, collectedDataFactory, issueFactory, this.getPropertySpecService(), this.nlsService, this.converter, calendarExtractor);
        }
        return messageProtocol;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return new ReadFirmwareVersion(this, issueFactory).request(getDeviceIdentifier());
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return this.collectedDataFactory.createBreakerStatusCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public CollectedCreditAmount getCreditAmount() {
        return this.collectedDataFactory.createCreditAmountCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return this.collectedDataFactory.createCalendarCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }
}