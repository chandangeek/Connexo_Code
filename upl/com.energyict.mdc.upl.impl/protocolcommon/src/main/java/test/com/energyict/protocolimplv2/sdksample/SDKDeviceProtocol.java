package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.security.DlmsSecuritySupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation of a DeviceProtocol which serves as a <i>guiding</i>
 * protocol for implementors
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 13:55
 */
public class SDKDeviceProtocol implements DeviceProtocol {

    private Logger logger = Logger.getLogger(SDKDeviceProtocol.class.getSimpleName());

    private final String defaultOptionalProperty = "defaultOptionalProperty";

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
    private DeviceProtocolSecurityCapabilities deviceProtocolSecurityCapabilities = new DlmsSecuritySupport();
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
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(PropertySpecFactory.booleanPropertySpec(defaultOptionalProperty));
        return optionalProperties;
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
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceProtocolCache = deviceProtocolCache;
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return this.deviceProtocolCache;
    }

    @Override
    public void setTime(Date timeToSet) {
        if(getTimeDeviationPropertyForWrite().getSeconds() == 0){
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
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>();
        // by default all loadProfileReaders are supported, only if the corresponding ObisCodeProperty matches, we mark it as not supported
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            this.logger.log(Level.INFO, "Fetching loadProfile configuration for loadProfile with ObisCode " + loadProfileReader.getProfileObisCode());
            CollectedLoadProfileConfiguration loadProfileConfiguration = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
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
        cal.add(Calendar.SECOND, getTimeDeviationPropertyForRead().getSeconds());
        Date timeToReturn = cal.getTime();
        this.logger.info("Returning the based on the deviationProperty " + timeToReturn);
        return timeToReturn;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        //TODO
        return Collections.emptyList();
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.<DeviceMessageSpec>asList(
                ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND,
                ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME,
                ContactorDeviceMessage.CONTACTOR_ARM,
                ContactorDeviceMessage.CONTACTOR_CLOSE,
                ContactorDeviceMessage.CONTACTOR_OPEN,
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE,
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE,
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_ACTIVATE,
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_URL_AND_ACTIVATE);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        //TODO
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        //TODO
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return "";  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(
                new SDKLoadProfileProtocolDialectProperties(),
                new SDKStandardDeviceProtocolDialectProperties(),
                new SDKTimeDeviceProtocolDialectProperties(),
                new SDKTopologyTaskProtocolDialectProperties());
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
    public List<PropertySpec> getSecurityProperties() {
        return this.deviceProtocolSecurityCapabilities.getSecurityProperties();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return this.deviceProtocolSecurityCapabilities.getSecurityRelationTypeName();
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
    public PropertySpec getSecurityPropertySpec(String name) {
        return this.deviceProtocolSecurityCapabilities.getSecurityPropertySpec(name);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        final CollectedTopology collectedTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(this.offlineDevice.getId()));
        if(!getSlaveOneSerialNumber().equals("")){
            collectedTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(getSlaveOneSerialNumber()));
        }
        if(!getSlaveTwoSerialNumber().equals("")){
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
    public void addProperties(TypedProperties properties) {
        this.logger.log(Level.INFO, "Adding the properties to the DeviceProtocol instance.");
        this.typedProperties.setAllProperties(properties);
    }

    private ObisCode getIgnoredObisCode() {
        return (ObisCode) this.typedProperties.getProperty(SDKLoadProfileProtocolDialectProperties.notSupportedLoadProfileObisCodePropertyName, ObisCode.fromString("0.0.0.0.0.0"));
    }

    private TimeDuration getTimeDeviationPropertyForRead() {
        return (TimeDuration) this.typedProperties.getProperty(SDKTimeDeviceProtocolDialectProperties.clockOffsetToReadPropertyName, new TimeDuration(0));
    }

    private TimeDuration getTimeDeviationPropertyForWrite() {
        return (TimeDuration) this.typedProperties.getProperty(SDKTimeDeviceProtocolDialectProperties.clockOffsetToWritePropertyName, new TimeDuration(0));
    }

    private String getSlaveOneSerialNumber(){
        return (String) this.typedProperties.getProperty(SDKTopologyTaskProtocolDialectProperties.slaveOneSerialNumberPropertyName, "");
    }

    private String getSlaveTwoSerialNumber(){
        return (String) this.typedProperties.getProperty(SDKTopologyTaskProtocolDialectProperties.slaveTwoSerialNumberPropertyName, "");
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return MdcInterfaceProvider.instance.get().getMdcInterface().getManager().getConnectionTypeFactory().findAll();
    }
}
