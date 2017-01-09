package com.energyict.protocolimplv2.ace4000;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocolimplv2.ace4000.objects.ObjectFactory;
import com.energyict.protocolimplv2.ace4000.requests.ReadFirmwareVersion;
import com.energyict.protocolimplv2.ace4000.requests.ReadLoadProfile;
import com.energyict.protocolimplv2.ace4000.requests.ReadMBusRegisters;
import com.energyict.protocolimplv2.ace4000.requests.ReadMeterEvents;
import com.energyict.protocolimplv2.ace4000.requests.ReadRegisters;
import com.energyict.protocolimplv2.ace4000.requests.SetTime;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 6/11/12
 * Time: 16:46
 * Author: khe
 */
public class ACE4000Outbound extends ACE4000 implements DeviceProtocol {

    private final Clock clock;
    private final IssueService issueService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private OfflineDevice offlineDevice;
    private DeviceProtocolCache deviceCache;
    private Logger logger;
    private Long cachedMeterTimeDifference = null;
    private ACE4000MessageExecutor messageExecutor = null;

    @Inject
    public ACE4000Outbound(Clock clock, PropertySpecService propertySpecService, IssueService issueService,
                           MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService,
                           CollectedDataFactory collectedDataFactory, MeteringService meteringService, Thesaurus thesaurus) {
        super(propertySpecService, identificationService, thesaurus);
        this.clock = clock;
        this.issueService = issueService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        messageExecutor = new ACE4000MessageExecutor(this, clock, issueService);
        setAce4000Connection(new ACE4000Connection(comChannel, this, false));
    }

    public ACE4000MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            messageExecutor = new ACE4000MessageExecutor(this, clock, issueService);
        }
        return messageExecutor;
    }

    @Override
    public String getProtocolDescription() {
        return "Actaris ACE4000 MeterXML";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    public String getVersion() {
        return "$Date: 2014-09-25 10:05:52 +0200 (Thu, 25 Sep 2014) $";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        CollectedLoadProfileConfiguration loadProfileConfiguration;
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            if (isMaster(loadProfileReader.getDeviceIdentifier())) {     //Master device
                ObisCode profileObisCode = loadProfileReader.getProfileObisCode();
                if (profileObisCode.equals(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE)) {//Only one LP is supported
                    loadProfileConfiguration = this.newDeviceLoadProfileConfiguration(profileObisCode, loadProfileReader.getDeviceIdentifier(), true);
                } else {
                    loadProfileConfiguration = this.newDeviceLoadProfileConfiguration(profileObisCode, loadProfileReader.getDeviceIdentifier(), false);
                }
                result.add(loadProfileConfiguration);
            } else {//Slave doesn't support
                result.add(this.newDeviceLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getDeviceIdentifier(), false));
            }
        }
        return result;
    }

    private CollectedLoadProfileConfiguration newDeviceLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier, boolean supported) {
        return this.collectedDataFactory.createCollectedLoadProfileConfiguration(profileObisCode, deviceIdentifier, supported);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfiles) {
            if (isMaster(loadProfileReader.getDeviceIdentifier())) {//Master device
                ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(this, issueService);
                result.addAll(readLoadProfileRequest.request(loadProfileReader));
            } else {//Slave device
                CollectedLoadProfile collectedLoadProfile =
                        this.createCollectedLoadProfile(
                                loadProfileReader.getLoadProfileIdentifier());
                collectedLoadProfile.setFailureInformation(
                        ResultType.NotSupported,
                        this.issueService.newIssueCollector().addProblem("MBus slave device doesn't support load profiles"));
                result.add(collectedLoadProfile);
            }
        }
        return result;
    }

    private CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return this.collectedDataFactory.createCollectedLoadProfile(loadProfileIdentifier);
    }

    public void setCachedMeterTimeDifference(Long cachedMeterTimeDifference) {
        this.cachedMeterTimeDifference = cachedMeterTimeDifference;
    }

    public Long getCachedMeterTimeDifference() {
        return cachedMeterTimeDifference;
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
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceCache = deviceProtocolCache;     //Unused, always empty
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return this.deviceCache;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.noneOf(DeviceMessageId.class);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            //TODO how to get message entry and content from DeviceMessageShadow?
            MessageResult messageResult = messageExecutor.executeMessage(new MessageEntry("", ""));
        }
        return null;    //TODO return message results
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new ACE4000DeviceProtocolDialect(this.getThesaurus(), this.getPropertySpecService()));
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        this.copyProperties(dialectProperties);    //Does a set, not an add
    }

    @Override
    public void setTime(Date timeToSet) {
        SetTime setTimeRequest = new SetTime(this, issueService);
        setTimeRequest.request(timeToSet);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<>();

        boolean requestMBusRegisters = false;
        for (OfflineRegister register : registers) {
            if (!isMaster(register.getDeviceIdentifier())) {
                requestMBusRegisters = true;
                break;
            }
        }

        //Read MBus slave registers
        if (requestMBusRegisters) {
            ReadMBusRegisters readMBusRegistersRequest = new ReadMBusRegisters(this, issueService, collectedDataFactory);
            result.addAll(readMBusRegistersRequest.request(registers));
        }

        //Read master registers
        ReadRegisters readRegistersRequest = new ReadRegisters(this, issueService, collectedDataFactory);
        result.addAll(readRegistersRequest.request(registers));
        return result;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        if (!objectFactory.getAllSlaveSerialNumbers().isEmpty()) {
            //Requesting MBus registers to have an idea which MBus devices are connected :)
            ReadMBusRegisters readMBusRegistersRequest = new ReadMBusRegisters(this, issueService, collectedDataFactory);
            readMBusRegistersRequest.request(new ArrayList<>());
        }

        final CollectedTopology deviceTopology = this.createCollectedTopology(offlineDevice.getDeviceIdentifier());
        for (String slaveSerialNumber : objectFactory.getAllSlaveSerialNumbers()) {
            deviceTopology.addSlaveDevice(this.identificationService.createDeviceIdentifierByCallHomeId(slaveSerialNumber));
        }
        return deviceTopology;
    }

    private CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier) {
        return this.collectedDataFactory.createCollectedTopology(deviceIdentifier);
    }

    public ObjectFactory getObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new ObjectFactory(this, this.readingTypeUtilService, identificationService, collectedDataFactory, meteringService);
            objectFactory.setInbound(false);
        }
        return objectFactory;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger; //TODO temporary, replace with provided logger
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
        if (isMaster(logBookReader.getDeviceIdentifier())) {
            ReadMeterEvents readMeterEventsRequest = new ReadMeterEvents(this, issueService);
            return readMeterEventsRequest.request(logBookReader.getLogBookIdentifier());
        } else {
            List<CollectedLogBook> result = new ArrayList<>();
            CollectedLogBook deviceLogBook = this.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            deviceLogBook.setFailureInformation(
                    ResultType.NotSupported,
                    this.issueService.newIssueCollector().addProblem("MBus slave device doesn't support events"));
            result.add(deviceLogBook);
            return result;
        }
    }

    private CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier) {
        return this.collectedDataFactory.createCollectedLogBook(logBookIdentifier);
    }

    private boolean isMaster(DeviceIdentifier deviceIdentifier) {
        return offlineDevice.getDeviceIdentifier().getIdentifier().equals(deviceIdentifier.getIdentifier());
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    //No log on / log off needed...
    public void logOn() {
    }

    public void daisyChainedLogOn() {
    }

    public void logOff() {
    }

    public void daisyChainedLogOff() {
    }

    public void terminate() {
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        //TODO use the password property for SMS communication
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return new ArrayList<>();
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return new ReadFirmwareVersion(this, issueService).request(getDeviceIdentifier());
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return collectedDataFactory.createBreakerStatusCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return this.collectedDataFactory.createCalendarCollectedData(this.offlineDevice.getDeviceIdentifier());
    }

}