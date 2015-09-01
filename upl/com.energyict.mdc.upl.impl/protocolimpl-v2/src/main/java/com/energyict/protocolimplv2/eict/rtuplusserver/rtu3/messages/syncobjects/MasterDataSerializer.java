package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecPossibleValues;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.protocol.security.SecurityPropertySet;
import com.energyict.mdc.protocol.tasks.*;
import com.energyict.mdc.tasks.*;
import com.energyict.mdw.amr.RegisterGroup;
import com.energyict.mdw.amr.RegisterSpec;
import com.energyict.mdw.core.*;
import com.energyict.mdwswing.decorators.mdc.NextExecutionSpecsShadowDecorator;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.RTU3;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.properties.RTU3ConfigurationSupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import org.codehaus.jackson.map.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class that takes a group of slave devices and returns a JSon serialized version of all their devicetypes, tasks & master data.
 * Note that every device config is in fact considered as a new unique device type, in the Beacon model.
 * It's name is DevicTypeName_ConfigName, the ID is the one of the config (which is unique in the config context).
 * <p/>
 * The result can then be used by the message executor.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 15:41
 */
public class MasterDataSerializer {

    public static final ObisCode FIXED_SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");

    private static final int NO_SCHEDULE = -1;

    public static String serializeMasterDataForOneConfig(Object messageAttribute) {

        final DeviceConfiguration deviceConfiguration = mw().getDeviceConfigurationFactory().find(((BigDecimal) messageAttribute).intValue());
        if (deviceConfiguration == null) {
            throw invalidFormatException("'Device configuration ID'", messageAttribute.toString(), "ID should reference a unique device configuration");
        }

        final AllMasterData allMasterData = new AllMasterData();

        //Use the CLIENT_MAC_ADDRESS of the first device of that config. Or the default value if there's no device available.
        Device slaveDevice = null;
        final List<Device> devices = mw().getDeviceFactory().findByConfiguration(deviceConfiguration);
        if (!devices.isEmpty()) {
            slaveDevice = devices.get(0);
        }

        //Use the dialect properties of the RTU3 gateway of a device that is of the given configuration
        Device gatewayDevice = null;
        for (Device device : devices) {
            final Device gateway = device.getGateway();
            if (gateway != null && gateway.getDeviceProtocolPluggableClass().getJavaClassName().equals(RTU3.class.getName())) {
                gatewayDevice = gateway;
                break;
            }
        }

        addDeviceConfiguration(gatewayDevice, allMasterData, slaveDevice, deviceConfiguration);

        return jsonSerialize(allMasterData);

    }

    //TODO error handling in case of no comtasks, no security, no nothing
    public static String serializeMasterData(Object messageAttribute) {
        final Device masterDevice = mw().getDeviceFactory().find(((BigDecimal) messageAttribute).intValue());
        if (masterDevice == null) {
            throw invalidFormatException("'DC device ID'", messageAttribute.toString(), "ID should reference a unique device");
        }

        final AllMasterData allMasterData = new AllMasterData();
        for (Device device : masterDevice.getDownstreamDevices()) {

            //Add all information about the device type config. (only once per device type config)
            final DeviceConfiguration deviceConfiguration = device.getConfiguration();
            if (!deviceTypeAlreadyExists(allMasterData.getDeviceTypes(), deviceConfiguration)) {
                addDeviceConfiguration(masterDevice, allMasterData, device, deviceConfiguration);
            }
        }

        return jsonSerialize(allMasterData);
    }

    private static void addDeviceConfiguration(Device masterDevice, AllMasterData allMasterData, Device device, DeviceConfiguration deviceConfiguration) {
        final int deviceTypeConfigId = deviceConfiguration.getId();
        final String deviceTypeName = deviceConfiguration.getDeviceType().getName() + "_" + deviceConfiguration.getName();   //DevicTypeName_ConfigName

        final RTU3ProtocolConfiguration protocolConfiguration = getProtocolConfiguration(deviceConfiguration, masterDevice, deviceConfiguration.getDeviceType());
        final List<RTU3Schedulable> schedulables = getSchedulables(deviceConfiguration);
        final RTU3ClockSyncConfiguration clockSyncConfiguration = getClockSyncConfiguration(deviceConfiguration);

        //Use the security set of the first task to read out the serial number.. doesn't really matter
        final RTU3MeterSerialConfiguration meterSerialConfiguration = new RTU3MeterSerialConfiguration(FIXED_SERIAL_NUMBER_OBISCODE, schedulables.get(0).getClientTypeId());

        final RTU3DeviceType rtu3DeviceType = new RTU3DeviceType(deviceTypeConfigId, deviceTypeName, meterSerialConfiguration, protocolConfiguration, schedulables, clockSyncConfiguration);
        allMasterData.getDeviceTypes().add(rtu3DeviceType);

        //Now add all information about the comtasks (get from configuration level, so it's the same for every device of the same device type)
        for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getCommunicationConfiguration().getEnabledComTasks()) {

            //Only sync tasks & schedules for meter data. Don't sync basic check, messages,...
            if (isMeterDataTask(comTaskEnablement, schedulables)) {
                //Don't add the security set again if it's already there (based on EIServer database ID)
                if (!clientTypeAlreadyExists(allMasterData.getClientTypes(), getClientTypeId(comTaskEnablement))) {
                    final RTU3ClientType clientType = getClientType(device, comTaskEnablement);
                    allMasterData.getClientTypes().add(clientType);
                }

                //Don't add a schedule if one already exists that has exactly the same name (e.g. 'every day at 00:00')
                final NextExecutionSpecs nextExecutionSpecs = comTaskEnablement.getNextExecutionSpecs();
                final long scheduleId = getScheduleId(nextExecutionSpecs);
                if (scheduleId != NO_SCHEDULE && !scheduleAlreadyExists(allMasterData.getSchedules(), nextExecutionSpecs)) {
                    final RTU3Schedule rtu3Schedule = new RTU3Schedule(scheduleId, getScheduleName(nextExecutionSpecs), CronTabStyleConverter.convert(nextExecutionSpecs));
                    allMasterData.getSchedules().add(rtu3Schedule);
                }
            }
        }
    }

    private static boolean isMeterDataTask(ComTaskEnablement comTaskEnablement, List<RTU3Schedulable> schedulables) {
        for (RTU3Schedulable schedulable : schedulables) {
            if (schedulable.getComTaskEnablement().getId() == comTaskEnablement.getId())
                return true;
        }
        return false;
    }

    public static String serializeMeterDetails(Object messageAttribute) {
        final Device masterDevice = mw().getDeviceFactory().find(((BigDecimal) messageAttribute).intValue());
        if (masterDevice == null) {
            throw invalidFormatException("'DC device ID'", messageAttribute.toString(), "ID should reference a unique device");
        }

        final List<Device> downstreamDevices = masterDevice.getDownstreamDevices();
        final RTU3MeterDetails[] result = new RTU3MeterDetails[downstreamDevices.size()];

        for (int index = 0; index < downstreamDevices.size(); index++) {
            Device device = downstreamDevices.get(index);

            //The meter details for every slave device (MAC address, timezone, security keys, device type ID)
            final RTU3MeterDetails meterDetails = createMeterDetails(device, masterDevice);
            result[index] = meterDetails;
        }
        return jsonSerialize(result);
    }

    private static RTU3MeterDetails createMeterDetails(Device device, Device masterDevice) {
        final String callHomeId = device.getProtocolProperties().getStringProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
        if (callHomeId == null || callHomeId.length() != 16) {
            throw invalidFormatException(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, (callHomeId == null ? "null" : callHomeId), "Should be 16 hex characters");
        }
        try {
            ProtocolTools.getBytesFromHexString(callHomeId, "");
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw invalidFormatException(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, callHomeId, "Should be 16 hex characters");
        }

        final int deviceTypeId = device.getConfigurationId();   //The ID of the config, instead of the device type. Since every new config represents a unique device type in the Beacon model

        String deviceTimeZone = DlmsProtocolProperties.DEFAULT_TIMEZONE;
        final TimeZoneInUse timeZoneInUse = device.getProtocolProperties().<TimeZoneInUse>getTypedProperty(DlmsProtocolProperties.TIMEZONE);
        if (timeZoneInUse != null && timeZoneInUse.getTimeZone() != null) {
            deviceTimeZone = timeZoneInUse.getTimeZone().getID();
        }

        //The master key is a general property on the Beacon DC device
        final byte[] masterKey = parseKey(RTU3ConfigurationSupport.MASTER_KEY, masterDevice.getProtocolProperties().getStringProperty(RTU3ConfigurationSupport.MASTER_KEY));

        //Get the DLMS keys from the device. If they are empty, an empty OctetString will be sent to the beacon.
        final byte[] password = getSecurityKey(device, SecurityPropertySpecName.PASSWORD.toString());
        final byte[] ak = getSecurityKey(device, SecurityPropertySpecName.AUTHENTICATION_KEY.toString());
        final byte[] ek = getSecurityKey(device, SecurityPropertySpecName.ENCRYPTION_KEY.toString());

        final String wrappedPassword = password == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(password, masterKey), "");
        final String wrappedAK = ak == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(ak, masterKey), "");
        final String wrappedEK = ek == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(ek, masterKey), "");

        return new RTU3MeterDetails(callHomeId, deviceTypeId, deviceTimeZone, device.getSerialNumber(), wrappedPassword, wrappedAK, wrappedEK);
    }

    private static MeteringWarehouse mw() {
        final MeteringWarehouse mw = MeteringWarehouse.getCurrent();
        if (mw == null) {
            MeteringWarehouse.createBatchContext();
            return MeteringWarehouse.getCurrent();
        } else {
            return mw;
        }
    }

    private static String jsonSerialize(Object object) {
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, object);
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
        return writer.toString();
    }

    private static boolean scheduleAlreadyExists(List<RTU3Schedule> schedules, NextExecutionSpecs nextExecutionSpecs) {
        for (RTU3Schedule schedule : schedules) {
            if (schedule.getName().equals(getScheduleName(nextExecutionSpecs))) {
                return true;
            }
        }
        return false;
    }

    private static String getScheduleName(NextExecutionSpecs nextExecutionSpecs) {
        if (nextExecutionSpecs == null) {
            return null;
        } else if (nextExecutionSpecs.getTemporalExpression() != null) {
            return new NextExecutionSpecsShadowDecorator(nextExecutionSpecs.getShadow()).toString();
        } else if (nextExecutionSpecs.getDialCalendar() != null) {
            throw MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException("Comtask schedule", "Read schedule with ID " + String.valueOf(nextExecutionSpecs.getDialCalendar().getId()), "A read schedule (dial calendar) is not supported by this message");
        } else {
            return null;
        }
    }

    private static RTU3ClockSyncConfiguration getClockSyncConfiguration(DeviceConfiguration deviceConfiguration) {
        boolean setClock = false;
        int min = 0;
        int max = 0xFFFF;

        for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getCommunicationConfiguration().getEnabledComTasks()) {
            for (ProtocolTask protocolTask : comTaskEnablement.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof ClockTask) {
                    setClock = true;
                    min = ((ClockTask) protocolTask).getMinimumClockDifference().getSeconds();
                    max = ((ClockTask) protocolTask).getMaximumClockDifference().getSeconds();
                    break;
                }
            }
            if (setClock) {
                break;
            }
        }
        return new RTU3ClockSyncConfiguration(setClock, min, max);
    }

    /**
     * Gather scheduling info by iterating over the comtask enablements (on config level), this will be the same for all devices of the same device type & config.
     */
    private static List<RTU3Schedulable> getSchedulables(DeviceConfiguration deviceConfiguration) {
        List<RTU3Schedulable> schedulables = new ArrayList<>();
        for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getCommunicationConfiguration().getEnabledComTasks()) {
            final long scheduleId = getScheduleId(comTaskEnablement.getNextExecutionSpecs());

            //Don't add the task if it has no schedule
            if (scheduleId != NO_SCHEDULE) {
                final int logicalDeviceId = 1;  //Linky and AM540 devices always use 1 as logical device
                int clientTypeId = getClientTypeId(comTaskEnablement);

                ArrayList<ObisCode> loadProfileObisCodes = getLoadProfileObisCodesForComTask(deviceConfiguration, comTaskEnablement);
                ArrayList<ObisCode> registerObisCodes = getRegisterObisCodesForComTask(deviceConfiguration, comTaskEnablement);
                ArrayList<ObisCode> logBookObisCodes = getLogBookObisCodesForComTask(deviceConfiguration, comTaskEnablement);

                if (isReadMeterDataTask(loadProfileObisCodes, registerObisCodes, logBookObisCodes)) {
                    final RTU3Schedulable schedulable = new RTU3Schedulable(comTaskEnablement, scheduleId, logicalDeviceId, clientTypeId, loadProfileObisCodes, registerObisCodes, logBookObisCodes);
                    schedulables.add(schedulable);
                }
            }
        }
        return schedulables;
    }

    private static boolean isReadMeterDataTask(ArrayList<ObisCode> loadProfileObisCodes, ArrayList<ObisCode> registerObisCodes, ArrayList<ObisCode> logBookObisCodes) {
        return !(loadProfileObisCodes.isEmpty() && registerObisCodes.isEmpty() && logBookObisCodes.isEmpty());
    }

    private static int getClientTypeId(ComTaskEnablement comTaskEnablement) {
        return comTaskEnablement.getSecurityPropertySet().getId();
    }

    /**
     * Return the hash code of the spec name.
     * We want 2 specs that have exactly the same name (e.g. 'every day at 00:00') to have the same ID.
     */
    private static long getScheduleId(NextExecutionSpecs nextExecutionSpecs) {
        final String scheduleName = getScheduleName(nextExecutionSpecs);
        if (scheduleName == null) {
            return NO_SCHEDULE;
        } else {
            return scheduleName.hashCode() & 0xFFFFFFFFL;   //Make sure the hash is a positive number
        }
    }

    private static ArrayList<ObisCode> getLogBookObisCodesForComTask(DeviceConfiguration deviceConfiguration, ComTaskEnablement comTaskEnablement) {
        Set<ObisCode> logBookObisCodes = new HashSet<>();
        if (((ServerComTask) comTaskEnablement.getComTask()).isConfiguredToCollectEvents()) {        //We can safely cast to the Server interface here, the format method (and everything related to messages) is only called from the EIServer framework
            for (ProtocolTask protocolTask : comTaskEnablement.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof LogBooksTask) {
                    final List<LogBookType> logBookTypes = ((LogBooksTask) protocolTask).getLogBookTypes();
                    if (logBookTypes.isEmpty()) {
                        for (LogBookSpec logBook : deviceConfiguration.getLogBookSpecs()) {
                            logBookObisCodes.add(logBook.getDeviceObisCode());
                        }
                    } else {
                        for (LogBookType logBookType : logBookTypes) {
                            logBookObisCodes.add(logBookType.getObisCode());
                        }
                    }
                }
            }
        }
        return new ArrayList<>(logBookObisCodes);
    }

    private static ArrayList<ObisCode> getRegisterObisCodesForComTask(DeviceConfiguration deviceConfiguration, ComTaskEnablement comTaskEnablement) {
        Set<ObisCode> registerObisCodes = new HashSet<>();
        if (((ServerComTask) comTaskEnablement.getComTask()).isConfiguredToCollectRegisterData()) {      //We can safely cast to the Server interface here, the format method (and everything related to messages) is only called from the EIServer framework
            for (ProtocolTask protocolTask : comTaskEnablement.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof RegistersTask) {
                    final List<RegisterGroup> registerGroups = ((RegistersTask) protocolTask).getRegisterGroups();
                    if (registerGroups.isEmpty()) {
                        for (RegisterSpec register : deviceConfiguration.getRegisterSpecs()) {
                            registerObisCodes.add(register.getDeviceObisCode());
                        }
                    } else {
                        for (RegisterSpec register : deviceConfiguration.getRegisterSpecs()) {
                            for (RegisterGroup registerGroup : registerGroups) {
                                if (register.getRegisterMapping().getRtuRegisterGroup().getId() == registerGroup.getId()) {
                                    registerObisCodes.add(register.getDeviceObisCode());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(registerObisCodes);
    }

    private static ArrayList<ObisCode> getLoadProfileObisCodesForComTask(DeviceConfiguration deviceConfiguration, ComTaskEnablement comTaskEnablement) {
        Set<ObisCode> loadProfileObisCodes = new HashSet<>();
        if (((ServerComTask) comTaskEnablement.getComTask()).isConfiguredToCollectLoadProfileData()) {   //We can safely cast to the Server interface here, the format method (and everything related to messages) is only called from the EIServer framework
            for (ProtocolTask protocolTask : comTaskEnablement.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof LoadProfilesTask) {
                    final List<LoadProfileType> loadProfileTypes = ((LoadProfilesTask) protocolTask).getLoadProfileTypes();
                    if (loadProfileTypes.isEmpty()) {
                        for (LoadProfileSpec loadProfile : deviceConfiguration.getLoadProfileSpecs()) {
                            loadProfileObisCodes.add(loadProfile.getDeviceObisCode());
                        }
                    } else {
                        for (LoadProfileType loadProfileType : loadProfileTypes) {
                            loadProfileObisCodes.add(loadProfileType.getObisCode());
                        }
                    }
                }
            }
        }
        return new ArrayList<>(loadProfileObisCodes);
    }

    private static RTU3ClientType getClientType(Device device, ComTaskEnablement comTaskEnablement) {
        final int clientTypeId = getClientTypeId(comTaskEnablement);
        final SecurityPropertySet securityPropertySet = comTaskEnablement.getSecurityPropertySet();

        List<SecurityProperty> securityProperties = new ArrayList<>();
        if (device != null) {
            securityProperties = device.getProtocolSecurityProperties(securityPropertySet);
        }
        BigDecimal clientMacAddress = null;

        //Find the configured value for clientMacAddress
        for (SecurityProperty securityProperty : securityProperties) {
            if (securityProperty.getName().equals(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString())) {
                clientMacAddress = (BigDecimal) securityProperty.getValue();
                break;
            }
        }

        //If it's not configured, use the default clientMacAddress (or 1 if there's no default)
        if (clientMacAddress == null) {
            for (PropertySpec propertySpec : securityPropertySet.getPropertySpecs()) {
                if (propertySpec.getName().equals(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString())) {
                    final PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
                    if (possibleValues != null) {
                        clientMacAddress = (BigDecimal) possibleValues.getDefault();
                        break;
                    }
                }
            }

            if (clientMacAddress == null) {
                clientMacAddress = BigDecimal.ONE;
            }
        }

        return new RTU3ClientType(clientTypeId, clientMacAddress.intValue(), securityPropertySet.getAuthenticationDeviceAccessLevelId(), securityPropertySet.getEncryptionDeviceAccessLevelId());
    }

    /**
     * The protocol java class name and the properties (both general & dialect).
     * Note that the properties are fetched from config level, so they are the same for every device that has the same device type.
     */
    private static RTU3ProtocolConfiguration getProtocolConfiguration(DeviceConfiguration deviceConfiguration, Device masterDevice, DeviceType deviceType) {
        final String javaClassName = deviceType.getDeviceProtocolPluggableClass().getJavaClassName();

        TypedProperties allProperties = TypedProperties.empty();

        //General props & pluggable class props, from the configuration of the slave device
        allProperties.setAllProperties(deviceConfiguration.getProperties());

        //These properties will be used to read out the connected e-meters (down stream). The actual e-meter only has 1 logical device, it's ID is 1.
        allProperties.setProperty(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, BigDecimal.ONE);
        allProperties.setProperty(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, BigDecimal.ONE);
        allProperties.setProperty(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
        allProperties.setProperty(MeterProtocol.NODEID, BigDecimal.ONE);

        //No need to send the PSK property
        allProperties.removeProperty(G3Properties.PSK);

        //Use dialect properties of the master device configuration, since the physical slave doesn't have dialect properties
        List<ProtocolDialectConfigurationProperties> protocolDialectConfigurationPropertiesList = new ArrayList<>();
        if (masterDevice != null) {
            protocolDialectConfigurationPropertiesList = masterDevice.getConfiguration().getCommunicationConfiguration().getProtocolDialectConfigurationPropertiesList();
        }

        //Look for the dialect with name 'GatewayTcpDlmsDialect'
        for (ProtocolDialectConfigurationProperties protocolDialectProperties : protocolDialectConfigurationPropertiesList) {
            if (protocolDialectProperties.getDeviceProtocolDialectName().equals(DeviceProtocolDialectNameEnum.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName())) {
                allProperties.setAllProperties(protocolDialectProperties.getTypedProperties());
                break;
            }
        }

        addDefaultDialectValuesIfNecessary(allProperties);

        //Add the name of the gateway dialect. The downstream protocol talks directly to the e-meter, just like when using the gateway functionality of the beacon.
        allProperties.setProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME, DeviceProtocolDialectNameEnum.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());

        return new RTU3ProtocolConfiguration(javaClassName, allProperties);
    }

    /**
     * For all properties who are not yet specified - but for which a default value exist - the default value will be added.
     * Note that we specifically use the gateway TCP dialect of the RTU3 protocol for this.
     */
    private static void addDefaultDialectValuesIfNecessary(TypedProperties dialectProperties) {
        DeviceProtocolDialect theActualDialect = new GatewayTcpDeviceProtocolDialect();

        for (PropertySpec propertySpec : theActualDialect.getOptionalProperties()) {
            if (!dialectProperties.hasValueFor(propertySpec.getName()) && propertySpec.getPossibleValues() != null) {
                dialectProperties.setProperty(propertySpec.getName(), propertySpec.getPossibleValues().getDefault());
            }
        }
        for (PropertySpec propertySpec : theActualDialect.getRequiredProperties()) {
            if (!dialectProperties.hasValueFor(propertySpec.getName()) && (propertySpec.getPossibleValues() != null)) {
                dialectProperties.setProperty(propertySpec.getName(), propertySpec.getPossibleValues().getDefault());
            }
        }
    }

    private static boolean deviceTypeAlreadyExists(List<RTU3DeviceType> rtu3DeviceTypes, DeviceConfiguration deviceConfiguration) {
        for (RTU3DeviceType rtu3DeviceType : rtu3DeviceTypes) {
            if (rtu3DeviceType.getId() == deviceConfiguration.getId()) {
                return true;
            }
        }
        return false;
    }

    private static boolean clientTypeAlreadyExists(List<RTU3ClientType> clientTypes, int clientTypeId) {
        for (RTU3ClientType clientType : clientTypes) {
            if (clientType.getId() == clientTypeId) {
                return true;
            }
        }
        return false;
    }

    private static byte[] wrap(byte[] key, byte[] masterKey) {
        final Key keyToWrap = new SecretKeySpec(key, "AES");
        final Key kek = new SecretKeySpec(masterKey, "AES");
        try {
            final Cipher aesWrap = Cipher.getInstance("AESWrap");
            aesWrap.init(Cipher.WRAP_MODE, kek);
            return aesWrap.wrap(keyToWrap);
        } catch (GeneralSecurityException e) {
            throw MdcManager.getComServerExceptionFactory().createDataEncryptionException(e);
        }
    }

    /**
     * Iterate over every defined security set to find a certain security property.
     * If it's not defined on any security set, return null.
     */
    private static byte[] getSecurityKey(Device device, String propertyName) {
        for (SecurityPropertySet securityPropertySet : device.getConfiguration().getCommunicationConfiguration().getSecurityPropertySets()) {
            final List<SecurityProperty> securityProperties = device.getProtocolSecurityProperties(securityPropertySet);
            for (SecurityProperty securityProperty : securityProperties) {
                if (securityProperty.getName().equals(propertyName)) {
                    final String propertyValue = (String) securityProperty.getValue();
                    if (propertyName.equals(SecurityPropertySpecName.PASSWORD.toString())) {
                        return parseASCIIPassword(propertyName, propertyValue);
                    } else {
                        return parseKey(propertyName, propertyValue);
                    }
                }
            }
        }
        return null;
    }

    private static byte[] parseKey(String propertyName, String propertyValue) {
        if (propertyValue == null || propertyValue.length() != 32) {
            throw invalidFormatException(propertyName, "(hidden)", "Should be 32 hex characters");
        }
        try {
            return ProtocolTools.getBytesFromHexString(propertyValue, "");
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw invalidFormatException(propertyName, "(hidden)", "Should be 32 hex characters");
        }
    }

    private static byte[] parseASCIIPassword(String propertyName, String propertyValue) {
        if (propertyValue == null || (propertyValue.length() % 8) != 0) {
            throw invalidFormatException(propertyName, "(hidden)", "Should be a multiple of 8 ASCII characters");
        }
        try {
            return propertyValue.getBytes();
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw invalidFormatException(propertyName, "(hidden)", "Should be a multiple of 8 ASCII characters");
        }
    }

    private static ComServerExecutionException invalidFormatException(String propertyName, String propertyValue, String message) {
        return MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException(propertyName, propertyValue, message);
    }
}