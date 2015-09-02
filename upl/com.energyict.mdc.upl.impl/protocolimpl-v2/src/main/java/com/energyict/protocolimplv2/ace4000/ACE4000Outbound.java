package com.energyict.protocolimplv2.ace4000;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedDataFactoryProvider;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.tasks.ACE4000DeviceProtocolDialect;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.ace4000.objects.ObjectFactory;
import com.energyict.protocolimplv2.ace4000.requests.ReadLoadProfile;
import com.energyict.protocolimplv2.ace4000.requests.ReadMBusRegisters;
import com.energyict.protocolimplv2.ace4000.requests.ReadMeterEvents;
import com.energyict.protocolimplv2.ace4000.requests.ReadRegisters;
import com.energyict.protocolimplv2.ace4000.requests.SetTime;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;

import java.util.ArrayList;
import java.util.Collections;
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
    private DeviceProtocolSecurityPropertySet securityProperties;

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
        return "Actaris ACE4000 MeterXML";
    }

    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            if (isMaster(loadProfileReader.getMeterSerialNumber())) {     //Master device
                ObisCode profileObisCode = loadProfileReader.getProfileObisCode();
                CollectedLoadProfileConfiguration config = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(profileObisCode, getSerialNumber());
                if (!profileObisCode.equals(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE)) {                        //Only one LP is supported
                    config.setSupportedByMeter(false);
                }
                result.add(config);
            } else {                                                                                    //Slave doesn't support
                CollectedLoadProfileConfiguration slaveConfig = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), getSerialNumber());
                slaveConfig.setSupportedByMeter(false);
                result.add(slaveConfig);
            }
        }
        return result;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> result = new ArrayList<CollectedLoadProfile>();
        for (LoadProfileReader loadProfileReader : loadProfiles) {
            if (isMaster(loadProfileReader.getMeterSerialNumber())) {   //Master device
                ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(this);
                result.addAll(readLoadProfileRequest.request(loadProfileReader));
            } else {    //Slave device
                CollectedLoadProfile collectedLoadProfile = CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode()));
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning("MBus slave device doesn't support load profiles"));
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
        ArrayList<DeviceProtocolDialect> dialects = new ArrayList<DeviceProtocolDialect>();
        dialects.add(gprsDialect);
        return dialects;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(PropertySpecFactory.stringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME));
        return optionalProperties;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        addProperties(dialectProperties);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<CollectedRegister>();

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

        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(offlineDevice.getId());
        final CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(deviceIdentifier);
        for (String slaveSerialNumber : objectFactory.getAllSlaveSerialNumbers()) {
            deviceTopology.addSlaveDevice(new DialHomeIdDeviceIdentifier(slaveSerialNumber));
        }
        return deviceTopology;
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
        List<DeviceProtocolCapabilities> capabilities = new ArrayList<DeviceProtocolCapabilities>();
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
            CollectedLogBook deviceLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            deviceLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning("MBus slave device doesn't support events"));
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
        securityProperties = deviceProtocolSecurityPropertySet;
        //TODO use the password property for SMS communication
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        ArrayList<ConnectionType> connectionTypes = new ArrayList<>();
        connectionTypes.add(new InboundIpConnectionType());
        return connectionTypes;
    }
}