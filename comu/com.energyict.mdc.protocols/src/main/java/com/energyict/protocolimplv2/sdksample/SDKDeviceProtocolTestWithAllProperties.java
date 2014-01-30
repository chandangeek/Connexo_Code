package com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.common.*;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.*;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.security.DlmsSecuritySupport;

import java.math.BigDecimal;
import java.util.*;
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
public class SDKDeviceProtocolTestWithAllProperties implements DeviceProtocol {

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

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.deviceProtocolSecurityCapabilities = new DlmsSecuritySupport();
        this.deviceProtocolSecurityCapabilities.setPropertySpecService(propertySpecService);
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
    public PropertySpec getPropertySpec (String name) {
        for (PropertySpec propertySpec : this.getPropertySpecs()) {
            if (name.equals(propertySpec.getName())) {
                return propertySpec;
            }
        }
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().stringPropertySpec("SDKStringProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().stringPropertySpec("SDKStringPropertyWithDefault", "Test"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().stringPropertySpecWithValues("SDKStringPropertyWithValues", "value 1", "value 2", "value 3", "value 4"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().stringPropertySpecWithValuesAndDefaultValue("SDKStringPropertyWithValuesAndDefault", "value 3", "value 1", "value 2", "value 4", "value 5"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().largeStringPropertySpec("SDKLargeStringProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().hexStringPropertySpec("SDKHexStringProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().passwordPropertySpec("SDKPasswordProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec("SDKBigDecimalProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec("SDKBigDecimalWithDefault", new BigDecimal("666.156")));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues("SDKBigDecimalWithValues", new BigDecimal("0"), new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().boundedDecimalPropertySpec("SDKBoundedDecimal", new BigDecimal(2), new BigDecimal(10)));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().positiveDecimalPropertySpec("SDKPositiveDecimalProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().booleanPropertySpec("SDKBooleanProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().notNullableBooleanPropertySpec("SDKNotNullableBoolean"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().datePropertySpec("SDKDateProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().timeOfDayPropertySpec("SDKTimeOfDayProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().dateTimePropertySpec("SDKDateTimeProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().timeDurationPropertySpec("SDKTimeDurationProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().timeDurationPropertySpec("SDKTimeDurationPropertyWithDefault", new TimeDuration(3,3)));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().timeDurationPropertySpecWithValues("SDKTimeDurationPropertyWithValues", new TimeDuration(4,5), new TimeDuration(5,5), new TimeDuration(3,3)));

        //optionalProperties.add(OptionalPropertySpecFactory.newInstance().timeZoneInUseReferencePropertySpec("SDKTimeZoneInUseProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().obisCodePropertySpecWithValues("SDKObisCodeProperty",
                        ObisCode.fromString("1.0.1.8.0.255"),
                        ObisCode.fromString("1.0.1.8.1.255"),
                        ObisCode.fromString("1.0.1.8.2.255"),
                        ObisCode.fromString("1.0.2.8.0.255"),
                        ObisCode.fromString("1.0.2.8.1.255"),
                        ObisCode.fromString("1.0.2.8.2.255")));

        optionalProperties.add(OptionalPropertySpecFactory.newInstance().referencePropertySpec("SDKCodeTableProperty", this.findFactory(FactoryIds.CODE)));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().referencePropertySpec("SDKUserFileReferenceProperty", this.findFactory(FactoryIds.USERFILE)));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().referencePropertySpec("SDKLookupProperty", this.findFactory(FactoryIds.LOOKUP)));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().referencePropertySpec("SDKLoadProfileTypeProperty", this.findFactory(FactoryIds.LOADPROFILE_TYPE)));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().referencePropertySpec("SDKLoadProfileProperty", this.findFactory(FactoryIds.LOADPROFILE)));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().ean13PropertySpec("SDKEan13Property"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().ean18PropertySpec("SDKEan18Property"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().encryptedStringPropertySpec("SDKEncryptedStringProperty"));
        optionalProperties.add(OptionalPropertySpecFactory.newInstance().spatialCoordinatesPropertySpec("SDKSpatialCoordinatesProperty"));


        return optionalProperties;
    }

    private IdBusinessObjectFactory findFactory (FactoryIds id) {
        return (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(id.id());
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
        CollectedDataFactory collectedDataFactory = this.getCollectedDataFactory();
        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>();
        // by default all loadProfileReaders are supported, only if the corresponding ObisCodeProperty matches, we mark it as not supported
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            this.logger.log(Level.INFO, "Fetching loadProfile configuration for loadProfile with ObisCode " + loadProfileReader.getProfileObisCode());
            CollectedLoadProfileConfiguration loadProfileConfiguration = collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
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
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        //TODO
        return null;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";  //Todo change body of implemented methods use File | Settings | File Templates.
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
        return Collections.emptyList();
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
        final CollectedTopology collectedTopology = this.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierBySerialNumber(this.offlineDevice.getSerialNumber()));
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
        return "EICT SDK DeviceProtocol with all properties";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
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
        // Todo: call the Enum that holds all known connection type classes
        return Collections.emptyList();
    }

    private CollectedDataFactory getCollectedDataFactory() {
        List<CollectedDataFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(CollectedDataFactory.class);
        if (factories.isEmpty()) {
            throw CommunicationException.missingModuleException(CollectedDataFactory.class);
        }
        else {
            return factories.get(0);
        }
    }

}
