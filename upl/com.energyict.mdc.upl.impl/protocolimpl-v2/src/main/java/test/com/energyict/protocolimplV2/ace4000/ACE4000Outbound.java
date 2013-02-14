package test.com.energyict.protocolimplV2.ace4000;

import com.energyict.comserver.issues.Problem;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.meterdata.identifiers.LoadProfileDataIdentifier;
import com.energyict.mdc.protocol.*;
import com.energyict.mdc.protocol.inbound.SerialNumberDeviceIdentifier;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.shadow.messages.DeviceMessageShadow;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.core.LoadProfileTypeFactory;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import test.com.energyict.mdc.tasks.ACE4000DeviceProtocolDialect;
import test.com.energyict.protocolimplV2.ace4000.objects.ObjectFactory;
import test.com.energyict.protocolimplV2.ace4000.requests.*;

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

    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<LoadProfileConfiguration> result = new ArrayList<LoadProfileConfiguration>();
        LoadProfileConfiguration loadProfileConfiguration;
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            if (isMaster(loadProfileReader.getMeterSerialNumber())) {     //Master device
                ObisCode profileObisCode = loadProfileReader.getProfileObisCode();
                if (profileObisCode.equals(LoadProfileTypeFactory.GENERIC_LOAD_PROFILE_OBISCODE)) {                        //Only one LP is supported
                    loadProfileConfiguration = new LoadProfileConfiguration(profileObisCode, getSerialNumber(), true);
                } else {
                    loadProfileConfiguration = new LoadProfileConfiguration(profileObisCode, getSerialNumber(), false);
                }
                result.add(loadProfileConfiguration);
            } else {                                                                                    //Slave doesn't support
                result.add(new LoadProfileConfiguration(loadProfileReader.getProfileObisCode(), getSerialNumber(), false));
            }
        }
        return result;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> result = new ArrayList<CollectedLoadProfile>();
        for (LoadProfileReader loadProfileReader : loadProfiles) {
            if (isMaster(loadProfileReader.getMeterSerialNumber())) {        //Master device
                ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(this);
                result.addAll(readLoadProfileRequest.request(loadProfileReader));
            } else {                                                                                       //Slave device
                DeviceLoadProfile collectedLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(loadProfileReader.getProfileObisCode(), new SerialNumberDeviceIdentifier(loadProfileReader.getMeterSerialNumber())));
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, new Problem("MBus slave device doesn't support load profiles"));
                result.add(collectedLoadProfile);
            }
        }
        return result;
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
    public List<DeviceMessageSpec> getSupportedStandardMessages() {
        return null;
    }

    @Override
    public CollectedMessage executePendingMessages(List<DeviceMessageShadow> pendingMessages) {
        for (DeviceMessageShadow pendingMessage : pendingMessages) {

            //TODO how to get message entry and content from DeviceMessageShadow?
            MessageResult messageResult = messageExecutor.executeMessage(new MessageEntry("", ""));

        }
        return null;    //TODO return message results
    }

    @Override
    public CollectedData updateSentMessages(List<DeviceMessageShadow> sentMessages) {
        return null;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        ACE4000DeviceProtocolDialect gprsDialect = new ACE4000DeviceProtocolDialect();
        ArrayList<DeviceProtocolDialect> dialects = new ArrayList<DeviceProtocolDialect>();
        dialects.add(gprsDialect);
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        addProperties(dialectProperties);    //Does a set, not an add
    }

    @Override
    public void setTime(Date timeToSet) {
        SetTime setTimeRequest = new SetTime(this);
        setTimeRequest.request(timeToSet);
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

        SerialNumberDeviceIdentifier deviceIdentifier = new SerialNumberDeviceIdentifier(offlineDevice.getSerialNumber());
        final DeviceTopology deviceTopology = new DeviceTopology(deviceIdentifier);
        for (String slaveSerialNumber : objectFactory.getAllSlaveSerialNumbers()) {
            deviceTopology.addSlaveDevice(new SerialNumberDeviceIdentifier(slaveSerialNumber));
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
            List<CollectedLogBook> result = new ArrayList<CollectedLogBook>();
            DeviceLogBook deviceLogBook = new DeviceLogBook(logBookReader.getLogBookIdentifier());
            deviceLogBook.setFailureInformation(ResultType.NotSupported, new Problem("MBus slave device doesn't support events"));
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
}