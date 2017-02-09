package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecPossibleValues;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.Clock;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.protocol.security.SecurityPropertySet;
import com.energyict.mdc.protocol.tasks.*;
import com.energyict.mdc.tasks.*;
import com.energyict.mdw.amr.RegisterGroup;
import com.energyict.mdw.amr.RegisterMapping;
import com.energyict.mdw.amr.RegisterSpec;
import com.energyict.mdw.core.*;
import com.energyict.mdwswing.decorators.mdc.NextExecutionSpecsShadowDecorator;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Helper class that takes a group of slave devices and returns a JSon serialized version of all their devicetypes, tasks & master data.
 * Note that every device config is in fact considered as a new unique device type, in the Beacon model.
 * Its name is DevicTypeName_ConfigName, the ID is the one of the config (which is unique in the config context).
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
    private static Logger logger;

    /**
     * Return the serialized description of all master data (scheduling info, obiscodes, etc) for a given config.
     */
    public String serializeMasterDataForOneConfig(int configId) {

        final DeviceConfiguration deviceConfiguration = getMeteringWarehouse().getDeviceConfigurationFactory().find(configId);
        if (deviceConfiguration == null) {
            throw invalidFormatException("'Device configuration ID'", String.valueOf(configId), "ID should reference a unique device configuration");
        }

        final AllMasterData allMasterData = new AllMasterData();

        //Use the CLIENT_MAC_ADDRESS of the first device of that config. Or the default value if there's no device available.
        Device slaveDevice = null;
        final List<Device> devices = getMeteringWarehouse().getDeviceFactory().findByConfiguration(deviceConfiguration);
        if (!devices.isEmpty()) {
            slaveDevice = devices.get(0);
        }

        //Use the dialect properties of the Beacon3100 gateway of a device that is of the given configuration
        Device gatewayDevice = null;
        for (Device device : devices) {
            final Device gateway = device.getGateway();
            if (gateway != null && gateway.getDeviceProtocolPluggableClass().getJavaClassName().equals(Beacon3100.class.getName())) {
                gatewayDevice = gateway;
                break;
            }
        }

        addDeviceConfiguration(gatewayDevice, allMasterData, slaveDevice, deviceConfiguration);

        return jsonSerialize(allMasterData);

    }

    /**
     * Return the serialized description of all master data (scheduling info, obiscodes, etc) for the configs of all slave meters that are linked to a given device
     */
    public String serializeMasterData(int deviceId) {
        final Device masterDevice = getMeteringWarehouse().getDeviceFactory().find(deviceId);
        if (masterDevice == null) {
            throw invalidFormatException("'DC device ID'", String.valueOf(deviceId), "ID should reference a unique device");
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

    private void addDeviceConfiguration(Device masterDevice, AllMasterData allMasterData, Device device, DeviceConfiguration deviceConfiguration) {
        final int deviceTypeConfigId = deviceConfiguration.getId();
        final String deviceTypeName = deviceConfiguration.getDeviceType().getName() + "_" + deviceConfiguration.getName();   //DevicTypeName_ConfigName

        final Beacon3100ProtocolConfiguration protocolConfiguration = getProtocolConfiguration(deviceConfiguration, masterDevice, deviceConfiguration.getDeviceType());
        final List<Beacon3100Schedulable> schedulables = getSchedulables(deviceConfiguration, allMasterData);
        if (schedulables.isEmpty()) {
            getLogger().warning("Comtask enablements on device configuration with ID " + deviceConfiguration.getId() +"are empty. Device configuration should have at least one comtask enablement that reads out meter data.");
        } else {

            final Beacon3100ClockSyncConfiguration clockSyncConfiguration = getClockSyncConfiguration(deviceConfiguration);

            //Use the security set of the first task to read out the serial number.. doesn't really matter
            final Beacon3100MeterSerialConfiguration meterSerialConfiguration = new Beacon3100MeterSerialConfiguration(FIXED_SERIAL_NUMBER_OBISCODE, schedulables.get(0).getClientTypeId());

            final Beacon3100DeviceType beacon3100DeviceType = new Beacon3100DeviceType(deviceTypeConfigId, deviceTypeName, meterSerialConfiguration, protocolConfiguration, schedulables, clockSyncConfiguration);
            allMasterData.getDeviceTypes().add(beacon3100DeviceType);

            final TimeZoneInUse beaconTimeZone = device.getProtocolProperties().getTypedProperty(DlmsProtocolProperties.TIMEZONE);
            final TimeZone localTimezone = device.getTimeZone();
            //Now add all information about the comtasks (get from configuration level, so it's the same for every device of the same device type)
            for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getCommunicationConfiguration().getEnabledComTasks()) {

                //Only sync tasks & schedules for meter data. Don't sync basic check, messages,...
                if (isMeterDataTask(comTaskEnablement, schedulables)) {
                    //Don't add the security set again if it's already there (based on EIServer database ID)
                    if (!clientTypeAlreadyExists(allMasterData.getClientTypes(), getClientTypeId(comTaskEnablement))) {
                        final Beacon3100ClientType clientType = getClientType(device, comTaskEnablement);
                        allMasterData.getClientTypes().add(clientType);
                    }

                    //Don't add a schedule if one already exists that has exactly the same name (e.g. 'every day at 00:00')
                    final NextExecutionSpecs nextExecutionSpecs = comTaskEnablement.getNextExecutionSpecs();
                    final long scheduleId = getScheduleId(nextExecutionSpecs);
                    if (scheduleId != NO_SCHEDULE && !scheduleAlreadyExists(allMasterData.getSchedules(), nextExecutionSpecs)) {
                        final Beacon3100Schedule beacon3100Schedule = new Beacon3100Schedule(scheduleId, getScheduleName(nextExecutionSpecs), CronTabStyleConverter.convert(nextExecutionSpecs, beaconTimeZone.getTimeZone(), localTimezone));
                        allMasterData.getSchedules().add(beacon3100Schedule);
                    }
                }
            }
        }

    }

    private boolean isMeterDataTask(ComTaskEnablement comTaskEnablement, List<Beacon3100Schedulable> schedulables) {
        for (Beacon3100Schedulable schedulable : schedulables) {
            if (schedulable.getComTaskEnablement().getId() == comTaskEnablement.getId())
                return true;
        }
        return false;
    }

    /**
     * Return the serialized description of all meter details of a given slave device
     */
    public String serializeMeterDetails(int deviceId) {
        final Device masterDevice = getMeteringWarehouse().getDeviceFactory().find(deviceId);
        if (masterDevice == null) {
            throw invalidFormatException("'DC device ID'", String.valueOf(deviceId), "ID should reference a unique device");
        }

        final List<Device> downstreamDevices = masterDevice.getDownstreamDevices();
        final Beacon3100MeterDetails[] result = new Beacon3100MeterDetails[downstreamDevices.size()];

        for (int index = 0; index < downstreamDevices.size(); index++) {
            Device device = downstreamDevices.get(index);

            //The meter details for every slave device (MAC address, timezone, security keys, device type ID)
            final Beacon3100MeterDetails meterDetails = createMeterDetails(device, masterDevice);
            result[index] = meterDetails;
        }
        return jsonSerialize(result);
    }


    private Beacon3100MeterDetails createMeterDetails(Device device, Device masterDevice) {
        final String callHomeId = parseCallHomeId(device);

        final int deviceTypeId = device.getConfigurationId();   //The ID of the config, instead of the device type. Since every new config represents a unique device type in the Beacon model

        String deviceTimeZone = DlmsProtocolProperties.DEFAULT_TIMEZONE;
        final TimeZoneInUse timeZoneInUse = device.getProtocolProperties().getTypedProperty(DlmsProtocolProperties.TIMEZONE);
        if (timeZoneInUse != null && timeZoneInUse.getTimeZone() != null) {
            deviceTimeZone = timeZoneInUse.getTimeZone().getID();
        }

        //The dlmsMeterKEK is a general property on the Beacon DC device
        final byte[] dlmsMeterKEK = parseKey(device.getId(), Beacon3100ConfigurationSupport.DLMS_METER_KEK, masterDevice.getProtocolProperties().getStringProperty(Beacon3100ConfigurationSupport.DLMS_METER_KEK));

        //Get the DLMS keys from the device. If they are empty, an empty OctetString will be sent to the beacon.
        final byte[] password = getSecurityKey(device, SecurityPropertySpecName.PASSWORD.toString());
        final byte[] ak = getSecurityKey(device, SecurityPropertySpecName.AUTHENTICATION_KEY.toString());
        final byte[] ek = getSecurityKey(device, SecurityPropertySpecName.ENCRYPTION_KEY.toString());

        final String wrappedPassword = password == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, password), "");
        final String wrappedAK = ak == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, ak), "");
        final String wrappedEK = ek == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, ek), "");

        return new Beacon3100MeterDetails(callHomeId, deviceTypeId, deviceTimeZone, device.getSerialNumber(), createClientDetails(device, dlmsMeterKEK), wrappedPassword, wrappedAK, wrappedEK);
    }

    private List<Beacon3100ClientDetails> createClientDetails(Device device, byte[] dlmsMeterKEK){
        int clientId = 1; //default
        byte[] password = null;
        byte[] hlsPassword = null;
        byte[] ak = null;
        byte[] ek = null;
        List<Beacon3100ClientDetails> clientDetails = new ArrayList<>();
        final long initialFrameCounter = device.getProtocolProperties().getTypedProperty(AM540ConfigurationSupport.INITIAL_FRAME_COUNTER, BigDecimal.valueOf(-1)).longValue();

        for (SecurityPropertySet securityPropertySet : device.getConfiguration().getCommunicationConfiguration().getSecurityPropertySets()) {
            for (SecurityProperty protocolSecurityProperty : device.getProtocolSecurityProperties(securityPropertySet)) {

                if (protocolSecurityProperty.getName().equals(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString())) {
                    clientId = ((BigDecimal) protocolSecurityProperty.getValue()).intValue();
                } else if (protocolSecurityProperty.getName().equals(SecurityPropertySpecName.PASSWORD.toString())) {
                    if(securityPropertySet.getAuthenticationDeviceAccessLevelId() >= 3){
                        hlsPassword = parseASCIIPassword(device.getId(), protocolSecurityProperty.getName(), (String) protocolSecurityProperty.getValue());
                    } else {
                        password = parseASCIIPassword(device.getId(), protocolSecurityProperty.getName(), (String) protocolSecurityProperty.getValue());
                    }
                } else if (protocolSecurityProperty.getName().equals(SecurityPropertySpecName.AUTHENTICATION_KEY.toString())) {
                    ak = parseKey(device.getId(), protocolSecurityProperty.getName(), (String) protocolSecurityProperty.getValue());
                } else if (protocolSecurityProperty.getName().equals(SecurityPropertySpecName.ENCRYPTION_KEY.toString())) {
                    ek = parseKey(device.getId(), protocolSecurityProperty.getName(), (String) protocolSecurityProperty.getValue());
                }
            }
            //Get the DLMS keys from the device. If they are empty, an empty OctetString will be sent to the beacon.
            final String wrappedPassword = password == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, password), "");
            final String wrappedHLSPassword = hlsPassword == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, hlsPassword), "");
            final String wrappedAK = ak == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, ak), "");
            final String wrappedEK = ek == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, ek), "");
            clientDetails.add(new Beacon3100ClientDetails(clientId, new Beacon3100ConnectionDetails(wrappedPassword, wrappedHLSPassword, wrappedAK, wrappedEK, initialFrameCounter)));
        }

        return  clientDetails;
    }

    /**
     * AES wrap the given key with the given master key (KEK).
     * This method is overridden in the crypto-protocols for EVN, where the wrapping is done by the HSM.
     */
    public byte[] wrap(byte[] masterKey, byte[] key) {
        return ProtocolTools.aesWrap(key, masterKey);
    }

    public String parseCallHomeId(Device device) {
        final String callHomeId = device.getProtocolProperties().getStringProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
        if (callHomeId == null) {
            throw missingProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
        }
        if (callHomeId.length() != 16) {
            throw invalidFormatException(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, callHomeId, "Should be 16 hex characters");
        }
        try {
            ProtocolTools.getBytesFromHexString(callHomeId, "");
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw invalidFormatException(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, callHomeId, "Should be 16 hex characters");
        }
        return callHomeId;
    }

    private MeteringWarehouse getMeteringWarehouse() {
        final MeteringWarehouse meteringWarehouse = MeteringWarehouse.getCurrent();
        if (meteringWarehouse == null) {
            MeteringWarehouse.createBatchContext();
            return MeteringWarehouse.getCurrent();
        } else {
            return meteringWarehouse;
        }
    }

    public static String jsonSerialize(Object object) {
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, object);
        } catch (IOException e) {
            throw DataParseException.generalParseException(e);
        }
        return writer.toString();
    }

    private boolean scheduleAlreadyExists(List<Beacon3100Schedule> schedules, NextExecutionSpecs nextExecutionSpecs) {
        for (Beacon3100Schedule schedule : schedules) {
            if (schedule.getName().equals(getScheduleName(nextExecutionSpecs))) {
                return true;
            }
        }
        return false;
    }

    private String getScheduleName(NextExecutionSpecs nextExecutionSpecs) {
        if (nextExecutionSpecs == null) {
            return null;
        } else if (nextExecutionSpecs.getTemporalExpression() != null) {
            return new NextExecutionSpecsShadowDecorator(nextExecutionSpecs.getShadow()).toString();
        } else if (nextExecutionSpecs.getDialCalendar() != null) {
            throw DeviceConfigurationException.invalidPropertyFormat("Comtask schedule", "Read schedule with ID " + String.valueOf(nextExecutionSpecs.getDialCalendar().getId()), "A read schedule (dial calendar) is not supported by this message");
        } else {
            return null;
        }
    }

    private Beacon3100ClockSyncConfiguration getClockSyncConfiguration(DeviceConfiguration deviceConfiguration) {
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
        return new Beacon3100ClockSyncConfiguration(setClock, min, max);
    }

    /**
     * Gather scheduling info by iterating over the comtask enablements (on config level), this will be the same for all devices of the same device type & config.
     */
    private List<Beacon3100Schedulable> getSchedulables(DeviceConfiguration deviceConfiguration, AllMasterData allMasterData) {
        List<Beacon3100Schedulable> schedulables = new ArrayList<>();
        for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getCommunicationConfiguration().getEnabledComTasks()) {
            final long scheduleId = getScheduleId(comTaskEnablement.getNextExecutionSpecs());

            //Don't add the task if it has no schedule
            if (scheduleId != NO_SCHEDULE) {
                final int logicalDeviceId = 1;  //Linky and AM540 devices always use 1 as logical device
                int clientTypeId = getClientTypeId(comTaskEnablement);

                List<ObisCode> loadProfileObisCodes = getLoadProfileObisCodesForComTask(deviceConfiguration, comTaskEnablement);
                List<ObisCode> registerObisCodes = getRegisterObisCodesForComTask(deviceConfiguration, comTaskEnablement);
                List<ObisCode> logBookObisCodes = getLogBookObisCodesForComTask(deviceConfiguration, comTaskEnablement);

                if (isConfiguredToCollectLoadProfileData(comTaskEnablement) && loadProfileObisCodes.isEmpty()) {
                    allMasterData.getWarningKeys().add("emptyLoadProfileComTask");
                    allMasterData.getWarningArguments().add(comTaskEnablement.getComTask().getName());
                }
                if (isConfiguredToCollectEvents(comTaskEnablement) && logBookObisCodes.isEmpty()) {
                    allMasterData.getWarningKeys().add("emptyLogbookComTask");
                    allMasterData.getWarningArguments().add(comTaskEnablement.getComTask().getName());
                }
                if (isConfiguredToCollectRegisterData(comTaskEnablement) && registerObisCodes.isEmpty()) {
                    allMasterData.getWarningKeys().add("emptyRegisterComTask");
                    allMasterData.getWarningArguments().add(comTaskEnablement.getComTask().getName());
                }

                if (isReadMeterDataTask(loadProfileObisCodes, registerObisCodes, logBookObisCodes)) {
                    final Beacon3100Schedulable schedulable = new Beacon3100Schedulable(comTaskEnablement, scheduleId, logicalDeviceId, clientTypeId, loadProfileObisCodes, registerObisCodes, logBookObisCodes);
                    schedulables.add(schedulable);
                }
            }
        }
        return schedulables;
    }

    /**
     * We can safely cast to the Server interface here, the format method (and everything related to messages) is only called from the EIServer framework
     */
    private boolean isConfiguredToCollectLoadProfileData(ComTaskEnablement comTaskEnablement) {
        return ((ServerComTask) comTaskEnablement.getComTask()).isConfiguredToCollectLoadProfileData();
    }

    private boolean isReadMeterDataTask(List<ObisCode> loadProfileObisCodes, List<ObisCode> registerObisCodes, List<ObisCode> logBookObisCodes) {
        return !(loadProfileObisCodes.isEmpty() && registerObisCodes.isEmpty() && logBookObisCodes.isEmpty());
    }

    private int getClientTypeId(ComTaskEnablement comTaskEnablement) {
        return comTaskEnablement.getSecurityPropertySet().getId();
    }

    /**
     * Return the hash code of the spec name.
     * We want 2 specs that have exactly the same name (e.g. 'every day at 00:00') to have the same ID.
     */
    private long getScheduleId(NextExecutionSpecs nextExecutionSpecs) {
        final String scheduleName = getScheduleName(nextExecutionSpecs);
        if (scheduleName == null) {
            return NO_SCHEDULE;
        } else {
            return scheduleName.hashCode() & 0xFFFFFFFFL;   //Make sure the hash is a positive number
        }
    }

    private List<ObisCode> getLogBookObisCodesForComTask(DeviceConfiguration deviceConfiguration, ComTaskEnablement comTaskEnablement) {
        Set<ObisCode> logBookObisCodes = new HashSet<>();
        if (isConfiguredToCollectEvents(comTaskEnablement)) {
            for (ProtocolTask protocolTask : comTaskEnablement.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof LogBooksTask) {
                    final List<LogBookType> logBookTypes = ((LogBooksTask) protocolTask).getLogBookTypes();
                    if (logBookTypes.isEmpty()) {
                    //if no specific logbook type is specified in logbook protocol task then use the logbook specification from device configuration
                        for (LogBookSpec logBook : deviceConfiguration.getLogBookSpecs()) {
                            logBookObisCodes.add(logBook.getDeviceObisCode());
                        }
                    } else {
                    //if we have specific logbook types defined in logbook protocol task then add only then add only logbook types obiscodes
                    // that are present in both device configuration and protocol task configuration

                        for (LogBookSpec logBook : deviceConfiguration.getLogBookSpecs()) {
                            if(logBookTypes.contains(logBook.getLogBookType())){
                                logBookObisCodes.add(logBook.getDeviceObisCode());
                            }
                        }
                    }
                }
            }

        }
        return new ArrayList<>(logBookObisCodes);
    }

    /**
     * We can safely cast to the Server interface here, the format method (and everything related to messages) is only called from the EIServer framework
     */
    private boolean isConfiguredToCollectEvents(ComTaskEnablement comTaskEnablement) {
        return ((ServerComTask) comTaskEnablement.getComTask()).isConfiguredToCollectEvents();
    }

    private List<ObisCode> getRegisterObisCodesForComTask(DeviceConfiguration deviceConfiguration, ComTaskEnablement comTaskEnablement) {
        Set<ObisCode> registerObisCodes = new HashSet<>();
        if (isConfiguredToCollectRegisterData(comTaskEnablement)) {
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
                                RegisterMapping registerMapping = register.getRegisterMapping();
                                if (registerMapping != null) {
                                    RegisterGroup rtuRegisterGroup = registerMapping.getRtuRegisterGroup();
                                    if (rtuRegisterGroup != null) {
                                        if (rtuRegisterGroup.getId() == registerGroup.getId()) {
                                            registerObisCodes.add(register.getDeviceObisCode());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return filterOutUnwantedRegisterObisCodes(registerObisCodes);
    }

    /**
     * We can safely cast to the Server interface here, the format method (and everything related to messages) is only called from the EIServer framework
     */
    private boolean isConfiguredToCollectRegisterData(ComTaskEnablement comTaskEnablement) {
        return ((ServerComTask) comTaskEnablement.getComTask()).isConfiguredToCollectRegisterData();
    }

    private List<ObisCode> filterOutUnwantedRegisterObisCodes(Set<ObisCode> obisCodes) {
        Iterator<ObisCode> iterator = obisCodes.iterator();
        while (iterator.hasNext()) {
            ObisCode obisCode = iterator.next();
            if (obisCode.equals(Clock.getDefaultObisCode()) || obisCode.equals(FIXED_SERIAL_NUMBER_OBISCODE)) {
                getLogger().warning("Filtering out register with obiscode '" + obisCode.toString() + "', it is already present in the mirror logical device by default.");
                iterator.remove();
            }
        }
        return new ArrayList<>(obisCodes);
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(MasterDataSerializer.class.getName());
        }
        return logger;
    }

    private List<ObisCode> getLoadProfileObisCodesForComTask(DeviceConfiguration deviceConfiguration, ComTaskEnablement comTaskEnablement) {
        Set<ObisCode> loadProfileObisCodes = new HashSet<>();
        if (isConfiguredToCollectLoadProfileData(comTaskEnablement)) {
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

    private Beacon3100ClientType getClientType(Device device, ComTaskEnablement comTaskEnablement) {
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
        final int securitySuite = 0;//TODO: get the security suite in use

        return new Beacon3100ClientType(clientTypeId, clientMacAddress.intValue(), securitySuite, securityPropertySet.getAuthenticationDeviceAccessLevelId(), securityPropertySet.getEncryptionDeviceAccessLevelId());
    }

    /**
     * The protocol java class name and the properties (both general & dialect).
     * Note that the properties are fetched from config level, so they are the same for every device that has the same device type.
     */
    private Beacon3100ProtocolConfiguration getProtocolConfiguration(DeviceConfiguration deviceConfiguration, Device masterDevice, DeviceType deviceType) {
        final String javaClassName = getJavaClassName(deviceType);

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

        return new Beacon3100ProtocolConfiguration(javaClassName, allProperties);
    }

    /**
     * Subclasses can override this implementation
     */
    protected String getJavaClassName(DeviceType deviceType) {
        return deviceType.getDeviceProtocolPluggableClass().getJavaClassName();
    }

    /**
     * For all properties who are not yet specified - but for which a default value exist - the default value will be added.
     * Note that we specifically use the gateway TCP dialect of the Beacon3100 protocol for this.
     */
    private void addDefaultDialectValuesIfNecessary(TypedProperties dialectProperties) {
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

    private boolean deviceTypeAlreadyExists(List<Beacon3100DeviceType> beacon3100DeviceTypes, DeviceConfiguration deviceConfiguration) {
        for (Beacon3100DeviceType beacon3100DeviceType : beacon3100DeviceTypes) {
            if (beacon3100DeviceType.getId() == deviceConfiguration.getId()) {
                return true;
            }
        }
        return false;
    }

    private boolean clientTypeAlreadyExists(List<Beacon3100ClientType> clientTypes, int clientTypeId) {
        for (Beacon3100ClientType clientType : clientTypes) {
            if (clientType.getId() == clientTypeId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterate over every defined security set to find a certain security property.
     * If it's not defined on any security set, return null.
     */
    public byte[] getSecurityKey(Device device, String propertyName) {
        return getSecurityKey(device, propertyName, null);
    }

    /**
     * Iterate over the security sets that have the given clientMacAddress to find a certain security property.
     * If the given clientMacAddress is null, iterate over all security sets.
     * If the requested property is not defined on any security set, return null.
     */
    public byte[] getSecurityKey(Device device, String propertyName, Integer clientMacAddress) {
        List<SecurityPropertySet> securitySets = new ArrayList<>();
        for (SecurityPropertySet securityPropertySet : device.getConfiguration().getCommunicationConfiguration().getSecurityPropertySets()) {
            if (clientMacAddress == null) {
                securitySets.add(securityPropertySet);
            } else {
                for (SecurityProperty protocolSecurityProperty : device.getProtocolSecurityProperties(securityPropertySet)) {
                    //Only add this security set if it is for the given clientMacAddress
                    if (protocolSecurityProperty.getName().equals(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString()) &&
                            ((BigDecimal) protocolSecurityProperty.getValue()).intValue() == clientMacAddress) {
                        securitySets.add(securityPropertySet);
                    }
                }
            }
        }

        for (SecurityPropertySet securityPropertySet : securitySets) {
            final List<SecurityProperty> securityProperties = device.getProtocolSecurityProperties(securityPropertySet);
            for (SecurityProperty securityProperty : securityProperties) {
                if (securityProperty.getName().equals(propertyName)) {
                    final String propertyValue = (String) securityProperty.getValue();
                    if (propertyName.equals(SecurityPropertySpecName.PASSWORD.toString())) {
                        return parseASCIIPassword(device.getId(), propertyName, propertyValue);
                    } else {
                        return parseKey(device.getId(), propertyName, propertyValue);
                    }
                }
            }
        }
        return null;
    }

    public byte[] parseKey(int offlineDeviceId, String propertyName, String propertyValue) {
        if (propertyValue == null) {
            throw missingProperty(propertyName);
        }
        if (propertyValue.length() != 32) {
            throw invalidFormatException(propertyName, "(hidden) of the device with id:"  + offlineDeviceId, " should have 32 hex characters.");
        }
        try {
            return ProtocolTools.getBytesFromHexString(propertyValue, "");
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw invalidFormatException(propertyName, "(hidden) of the device with id:"  + offlineDeviceId, " should have 32 hex characters.");
        }
    }

    protected byte[] parseASCIIPassword(int offlineDeviceId, String propertyName, String propertyValue) {
        if (propertyValue == null) {
            throw missingProperty(propertyName);
        }
        if ((propertyValue.length() % 8) != 0) {
            throw invalidFormatException(propertyName, "(hidden) of the device with id:"  + offlineDeviceId, " should be a multiple of 8 ASCII characters.");
        }
        try {
            return propertyValue.getBytes();
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw invalidFormatException(propertyName, "(hidden) of the device with id:"  + offlineDeviceId, " should be a multiple of 8 ASCII characters.");
        }
    }

    protected ProtocolRuntimeException invalidFormatException(String propertyName, String propertyValue, String message) {
        return DeviceConfigurationException.invalidPropertyFormat(propertyName, propertyValue, message);
    }

    protected ProtocolRuntimeException missingProperty(String propertyName) {
        return DeviceConfigurationException.missingProperty(propertyName);
    }


}