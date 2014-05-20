package com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.BigDecimalFactory;
import com.energyict.mdc.dynamic.BooleanFactory;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.Ean13Factory;
import com.energyict.mdc.dynamic.Ean18Factory;
import com.energyict.mdc.dynamic.EncryptedStringFactory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.LargeStringFactory;
import com.energyict.mdc.dynamic.PasswordFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.SpatialCoordinatesFactory;
import com.energyict.mdc.dynamic.StringFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.dynamic.TimeOfDayFactory;
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
import com.energyict.protocols.mdc.services.impl.Bus;

import java.math.BigDecimal;
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
        PropertySpecService propertySpecService = Bus.getPropertySpecService();
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKStringProperty", false, new StringFactory()));
        optionalProperties.add(propertySpecService.stringPropertySpec("SDKStringPropertyWithDefault", false, "Test"));
        optionalProperties.add(propertySpecService.stringPropertySpecWithValues("SDKStringPropertyWithValues", false, "value 1", "value 2", "value 3", "value 4"));
        optionalProperties.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue("SDKStringPropertyWithValuesAndDefault", false, "value 3", "value 1", "value 2", "value 4", "value 5"));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKLargeStringProperty", false, new LargeStringFactory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKHexStringProperty", false, new HexStringFactory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKPasswordProperty", false, new PasswordFactory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKBigDecimalProperty", false, new BigDecimalFactory()));
        optionalProperties.add(propertySpecService.bigDecimalPropertySpec("SDKBigDecimalWithDefault", false, new BigDecimal("666.156")));
        optionalProperties.add(
                propertySpecService.bigDecimalPropertySpecWithValues(
                        "SDKBigDecimalWithValues",
                        false,
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        new BigDecimal("2"),
                        new BigDecimal("3")));
        optionalProperties.add(propertySpecService.boundedDecimalPropertySpec("SDKBoundedDecimal", false, new BigDecimal(2), new BigDecimal(10)));
        optionalProperties.add(propertySpecService.positiveDecimalPropertySpec("SDKPositiveDecimalProperty", false));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKBooleanProperty", false, new BooleanFactory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKDateProperty", false, new DateFactory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKTimeOfDayProperty", false, new TimeOfDayFactory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKDateTimeProperty", false, new DateAndTimeFactory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKTimeDurationProperty", false, new TimeDurationValueFactory()));

        optionalProperties.add(
                propertySpecService.obisCodePropertySpecWithValues(
                        "SDKObisCodeProperty",
                        false,
                        ObisCode.fromString("1.0.1.8.0.255"),
                        ObisCode.fromString("1.0.1.8.1.255"),
                        ObisCode.fromString("1.0.1.8.2.255"),
                        ObisCode.fromString("1.0.2.8.0.255"),
                        ObisCode.fromString("1.0.2.8.1.255"),
                        ObisCode.fromString("1.0.2.8.2.255")));

        optionalProperties.add(propertySpecService.referencePropertySpec("SDKCodeTableProperty", false, FactoryIds.CODE));
        optionalProperties.add(propertySpecService.referencePropertySpec("SDKUserFileReferenceProperty", false, FactoryIds.USERFILE));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKEan13Property", false, new Ean13Factory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKEan18Property", false, new Ean18Factory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKEncryptedStringProperty", false, new EncryptedStringFactory()));
        optionalProperties.add(propertySpecService.basicPropertySpec("SDKSpatialCoordinatesProperty", false, new SpatialCoordinatesFactory()));

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
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

}
