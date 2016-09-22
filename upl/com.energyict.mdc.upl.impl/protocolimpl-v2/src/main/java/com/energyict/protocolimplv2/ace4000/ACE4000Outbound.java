package com.energyict.protocolimplv2.ace4000;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
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
import com.energyict.mdw.offline.OfflineLoadProfile;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.ace4000.messages.ACE4000Messaging;
import com.energyict.protocolimplv2.ace4000.objects.ObjectFactory;
import com.energyict.protocolimplv2.ace4000.requests.*;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;

import java.util.*;
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

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        setAce4000Connection(new ACE4000Connection(comChannel, this, false));
    }

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

    public String getVersion() {
        return "$Date: 2016-06-29 13:42:57 +0200 (Wed, 29 Jun 2016)$";
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
                CollectedLoadProfileConfiguration config = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(profileObisCode, getConfiguredSerialNumber());
                if (!profileObisCode.equals(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE)) {                        //Only one LP is supported
                    config.setSupportedByMeter(false);
                } else {
                    List<OfflineLoadProfile> offlineLoadProfiles = getOfflineDevice().getAllOfflineLoadProfiles();
                    if (offlineLoadProfiles != null && offlineLoadProfiles.size() > 0) {
                        OfflineLoadProfile offlineLoadProfile = getOfflineLoadProfile(offlineLoadProfiles, DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);
                        long profileInterval = offlineLoadProfile.getInterval().getMilliSeconds();
                        Date toDate = new Date();
                        Date fromDate = new Date(toDate.getTime() - (2 * profileInterval)); // get the last interval from date
                        ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(this, fromDate, toDate);
                        List<CollectedLoadProfile> collectedLoadProfiles = readLoadProfileRequest.request(loadProfileReader);
                        if (collectedLoadProfiles != null && collectedLoadProfiles.size() > 0) {
                            config.setChannelInfos(collectedLoadProfiles.get(0).getChannelInfo());
                        } else { // if we are not able to read the channelInfos from device then return the ones configured in EIMaster and skip validation of channelInfos
                            config.setChannelInfos(loadProfileReader.getChannelInfos());
                        }
                        getObjectFactory().resetLoadProfile();
                    }
                }
                result.add(config);
            } else {                                                                                    //Slave doesn't support
                CollectedLoadProfileConfiguration slaveConfig = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), getConfiguredSerialNumber());
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
        List<CollectedLoadProfile> result = new ArrayList<CollectedLoadProfile>();
        for (LoadProfileReader loadProfileReader : loadProfiles) {
            if (isMaster(loadProfileReader.getMeterSerialNumber())) {   //Master device
                ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(this);
                result.addAll(readLoadProfileRequest.request(loadProfileReader));
            } else {    //Slave device
                CollectedLoadProfile collectedLoadProfile = CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode()));
                Issue<LoadProfileReader> warning = MdcManager.getIssueFactory().createWarning(loadProfileReader, "loadProfileXIssue", loadProfileReader.getProfileObisCode(), "MBus slave device doesn't support load profiles");
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getMessageProtocol().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return getMessageProtocol().prepareMessageContext(offlineDevice, deviceMessage);
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
        return logger;
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
            return readMeterEventsRequest.request(logBookReader);
        } else {
            List<CollectedLogBook> result = new ArrayList<>();
            CollectedLogBook deviceLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            deviceLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), "MBus slave device doesn't support events"));
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
        ArrayList<ConnectionType> connectionTypes = new ArrayList<>();
        connectionTypes.add(new InboundIpConnectionType());
        return connectionTypes;
    }

    public ACE4000Messaging getMessageProtocol() {
        if (this.messageProtocol == null) {
            this.messageProtocol = new ACE4000Messaging(this);
        }
        return messageProtocol;
    }
}