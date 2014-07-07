package com.energyict.protocolimplv2.ace4000;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport;
import com.energyict.protocolimplv2.ace4000.objects.ObjectFactory;
import com.energyict.protocolimplv2.ace4000.requests.ReadLoadProfile;
import com.energyict.protocolimplv2.ace4000.requests.ReadMBusRegisters;
import com.energyict.protocolimplv2.ace4000.requests.ReadMeterEvents;
import com.energyict.protocolimplv2.ace4000.requests.ReadRegisters;
import com.energyict.protocolimplv2.ace4000.requests.SetTime;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.protocols.mdc.protocoltasks.ACE4000DeviceProtocolDialect;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private ACE4000MessageExecutor messageExecutor = null;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        messageExecutor = new ACE4000MessageExecutor(this);
        setAce4000Connection(new ACE4000Connection(comChannel, this, false));
    }

    public ACE4000MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            messageExecutor = new ACE4000MessageExecutor(this);
        }
        return messageExecutor;
    }

    @Override
    public String getProtocolDescription() {
        return "Actaris ACE4000";
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
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        CollectedLoadProfileConfiguration loadProfileConfiguration;
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            if (isMaster(loadProfileReader.getMeterSerialNumber())) {     //Master device
                ObisCode profileObisCode = loadProfileReader.getProfileObisCode();
                if (profileObisCode.equals(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE)) {//Only one LP is supported
                    loadProfileConfiguration = this.newDeviceLoadProfileConfiguration(profileObisCode, getSerialNumber(), true);
                } else {
                    loadProfileConfiguration = this.newDeviceLoadProfileConfiguration(profileObisCode, getSerialNumber(), false);
                }
                result.add(loadProfileConfiguration);
            } else {//Slave doesn't support
                result.add(this.newDeviceLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), getSerialNumber(), false));
            }
        }
        return result;
    }

    private CollectedLoadProfileConfiguration newDeviceLoadProfileConfiguration(ObisCode profileObisCode, String serialNumber, boolean supported) {
        return this.getCollectedDataFactory().createCollectedLoadProfileConfiguration(profileObisCode, serialNumber, supported);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfiles) {
            if (isMaster(loadProfileReader.getMeterSerialNumber())) {//Master device
                ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(this);
                result.addAll(readLoadProfileRequest.request(loadProfileReader));
            } else {//Slave device
                CollectedLoadProfile collectedLoadProfile =
                        this.createCollectedLoadProfile(
                                new LoadProfileIdentifierByObisCodeAndDevice(
                                        loadProfileReader.getProfileObisCode(),
                                        new DeviceIdentifierBySerialNumber(
                                                loadProfileReader.getMeterSerialNumber())));
                collectedLoadProfile.setFailureInformation(
                        ResultType.NotSupported,
                        Bus.getIssueService().newIssueCollector().
                                addProblem("MBus slave device doesn't support load profiles"));
                result.add(collectedLoadProfile);
            }
        }
        return result;
    }

    private CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return this.getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
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
    public List<DeviceMessageSpec> getSupportedMessages() {
        return null;
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
        ACE4000DeviceProtocolDialect gprsDialect = new ACE4000DeviceProtocolDialect();
        ArrayList<DeviceProtocolDialect> dialects = new ArrayList<>();
        dialects.add(gprsDialect);
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        this.copyProperties(dialectProperties);    //Does a set, not an add
    }

    @Override
    public void setTime(Date timeToSet) {
        SetTime setTimeRequest = new SetTime(this);
        setTimeRequest.request(timeToSet);
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
            ReadMBusRegisters readMBusRegistersRequest = new ReadMBusRegisters(this);
            result.addAll(readMBusRegistersRequest.request(registers));
        }

        //Read master registers
        ReadRegisters readRegistersRequest = new ReadRegisters(this);
        result.addAll(readRegistersRequest.request(registers));
        return result;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        if (!objectFactory.getAllSlaveSerialNumbers().isEmpty()) {
            //Requesting MBus registers to have an idea which MBus devices are connected :)
            ReadMBusRegisters readMBusRegistersRequest = new ReadMBusRegisters(this);
            readMBusRegistersRequest.request(new ArrayList<OfflineRegister>());
        }

        DeviceIdentifierBySerialNumber deviceIdentifier = new DeviceIdentifierBySerialNumber(offlineDevice.getSerialNumber());
        final CollectedTopology deviceTopology = this.createCollectedTopology(deviceIdentifier);
        for (String slaveSerialNumber : objectFactory.getAllSlaveSerialNumbers()) {
            deviceTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(slaveSerialNumber));
        }
        return deviceTopology;
    }

    private CollectedTopology createCollectedTopology(DeviceIdentifierBySerialNumber deviceIdentifier) {
        return this.getCollectedDataFactory().createCollectedTopology(deviceIdentifier);
    }

    public ObjectFactory getObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new ObjectFactory(this);
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
        if (isMaster(logBookReader.getMeterSerialNumber())) {
            ReadMeterEvents readMeterEventsRequest = new ReadMeterEvents(this);
            return readMeterEventsRequest.request(logBookReader.getLogBookIdentifier());
        } else {
            List<CollectedLogBook> result = new ArrayList<>();
            CollectedLogBook deviceLogBook = this.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            deviceLogBook.setFailureInformation(ResultType.NotSupported, Bus.getIssueService().newIssueCollector().addProblem("MBus slave device doesn't support events"));
            result.add(deviceLogBook);
            return result;
        }
    }

    private CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier) {
        return this.getCollectedDataFactory().createCollectedLogBook(logBookIdentifier);
    }

    private boolean isMaster(String serialNumber) {
        return offlineDevice.getSerialNumber().equalsIgnoreCase(serialNumber);
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
}