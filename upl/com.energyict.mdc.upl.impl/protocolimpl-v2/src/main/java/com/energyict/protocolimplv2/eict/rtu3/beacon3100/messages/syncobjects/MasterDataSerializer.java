package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.tasks.GatewayTcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor.Registers;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.Clock;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast.MulticastSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.DeviceProtocolDialect.Property.DEVICE_PROTOCOL_DIALECT;

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
    private final int PUBLIC_CLIENT_ID = 16;

    private static final long NO_SCHEDULE = -1;
    private static Logger logger;
    private Beacon3100Properties beacon3100Properties;

    private final ObjectMapperService objectMapperService;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final DeviceMasterDataExtractor extractor;

    public MasterDataSerializer(ObjectMapperService objectMapperService, PropertySpecService propertySpecService, DeviceMasterDataExtractor extractor, Beacon3100Properties beacon3100Properties, NlsService nlsService) {
        this.objectMapperService = objectMapperService;
        this.propertySpecService = propertySpecService;
        this.extractor = extractor;
        this.beacon3100Properties = beacon3100Properties;
        this.nlsService = nlsService;
    }

    public MulticastSerializer multicastSerializer() {
        return new MulticastSerializer(this.extractor, this);
    }

    /**
     * Return the serialized description of all master data (scheduling info, obiscodes, etc) for a given config.
     */
    public String serializeMasterDataForOneConfig(int configurationId, boolean readOldObisCodes) {
        final DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration =
                this.extractor
                        .configuration(configurationId)
                        .orElseThrow(() -> invalidFormatException("'Device configuration ID'", String.valueOf(configurationId), "ID should reference a unique device configuration"));

        final AllMasterData allMasterData = new AllMasterData();

        //Use the CLIENT_MAC_ADDRESS of the first device of that config. Or the default value if there's no device available.
        Device slaveDevice = null;
        final List<Device> devices = deviceConfiguration.devices();
        if (!devices.isEmpty()) {
            slaveDevice = devices.get(0);
        }

        //Use the dialect properties of the Beacon3100 gateway of a device that is of the given configuration
        Device gatewayDevice =
                devices
                        .stream()
                        .map(this.extractor::gateway)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(gateway -> getJavaClassName(gateway).equals(Beacon3100.class.getName()))
                        .findAny()
                        .orElse(null);

        if (slaveDevice == null) {
            throw invalidFormatException("'Device configuration ID'", String.valueOf(configurationId), " is not used by any slave device.");
        }

        addDeviceConfiguration(gatewayDevice, allMasterData, slaveDevice, deviceConfiguration, readOldObisCodes);

        return jsonSerialize(allMasterData);
    }

    protected String getJavaClassName(Device gateway) {
        return this.extractor.protocolJavaClassName(gateway);
    }

    /**
     * Return the serialized description of all master data (scheduling info, obiscodes, etc) for the configs of all slave meters that are linked to a given device
     */
    public String serializeMasterData(Device masterDevice, boolean readOldObiscodes) {
        final AllMasterData allMasterData = new AllMasterData();
        for (Device device : extractor.downstreamDevices(masterDevice)) {
            //Add all information about the device type config. (only once per device type config)
            final DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration = extractor.configuration(device);
            if (!this.deviceTypeAlreadyExists(allMasterData.getDeviceTypes(), deviceConfiguration)) {
                this.addDeviceConfiguration(masterDevice, allMasterData, device, deviceConfiguration, readOldObiscodes);
            }
        }
        return jsonSerialize(allMasterData);
    }

    private void addDeviceConfiguration(Device masterDevice, AllMasterData allMasterData, Device device, DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration, boolean readOldObiscodes) {
        final long deviceTypeConfigId = deviceConfiguration.id();
        final String deviceTypeName = deviceConfiguration.fullyQualifiedName("_");   // DeviceTypeName_ConfigurationName

        final Beacon3100ProtocolConfiguration protocolConfiguration = this.getProtocolConfiguration(deviceConfiguration, masterDevice);
        final List<Beacon3100Schedulable> schedulables = this.getSchedulables(device, deviceConfiguration, allMasterData, readOldObiscodes);
        if (schedulables.isEmpty()) {
            getLogger().warning("Comtask enablements on device configuration with ID " + deviceConfiguration.id() + "are empty. Device configuration should have at least one comtask enablement that reads out meter data.");
        } else {
            final Beacon3100ClockSyncConfiguration clockSyncConfiguration = this.getClockSyncConfiguration(device);

            //Use the security set of the first task to read out the serial number.. doesn't really matter
            final Beacon3100MeterSerialConfiguration meterSerialConfiguration = new Beacon3100MeterSerialConfiguration(FIXED_SERIAL_NUMBER_OBISCODE, schedulables.get(0).getClientTypeId());

            final Beacon3100DeviceType beacon3100DeviceType = new Beacon3100DeviceType(deviceTypeConfigId, deviceTypeName, meterSerialConfiguration, protocolConfiguration, schedulables, clockSyncConfiguration, readOldObiscodes);
            allMasterData.getDeviceTypes().add(beacon3100DeviceType);

            final TimeZone beaconTimeZone = this.extractor.protocolProperties(device).getTypedProperty(DlmsProtocolProperties.TIMEZONE);
            final TimeZone localTimeZone = this.extractor.timeZone(device);

            //Now add all information about the comtasks (get from configuration level, so it's the same for every device of the same device type)
            for (DeviceMasterDataExtractor.CommunicationTask enabledTask : this.extractor.enabledTasks(device)) {
                //Only sync tasks & schedules for meter data. Don't sync basic check, messages,...
                if (this.isMeterDataTask(enabledTask, schedulables)) {
                    //Don't add the security set again if it's already there (based on database ID)
                    if (!this.clientTypeAlreadyExists(allMasterData.getClientTypes(), this.getClientTypeId(enabledTask))) {
                        final Beacon3100ClientType clientType = this.getClientType(device, enabledTask);
                        allMasterData.getClientTypes().add(clientType);
                    }

                    //Don't add a schedule if one already exists that has exactly the same name (e.g. 'every day at 00:00')
                    final long scheduleId = this.getScheduleId(enabledTask);
                    if (scheduleId != NO_SCHEDULE && !this.scheduleAlreadyExists(allMasterData.getSchedules(), enabledTask)) {
                        final Beacon3100Schedule beacon3100Schedule =
                                new Beacon3100Schedule(
                                        scheduleId,
                                        getScheduleName(enabledTask),
                                        enabledTask.nextExecutionSpecs().get().toCronExpression(beaconTimeZone != null ? beaconTimeZone : localTimeZone, localTimeZone));
                        allMasterData.getSchedules().add(beacon3100Schedule);
                    }
                }
            }
        }

    }

    private boolean isMeterDataTask(DeviceMasterDataExtractor.CommunicationTask task, List<Beacon3100Schedulable> schedulables) {
        for (Beacon3100Schedulable schedulable : schedulables) {
            if (schedulable.getOriginalId() == task.id()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Return the serialized description of all meter details of a given slave device
     */
    public String serializeMeterDetails(Device masterDevice) {
        return jsonSerialize(
                this.extractor
                        .downstreamDevices(masterDevice)
                        .stream()
                        .map(device -> this.createMeterDetails(device, masterDevice))
                        .toArray(Beacon3100MeterDetails[]::new));
    }

    public Beacon3100MeterDetails[] getMeterDetails(int deviceId) {
        Optional<Device> masterDevice = extractor.find(deviceId);
        if (!masterDevice.isPresent()) {
            throw invalidFormatException("'DC device ID'", String.valueOf(deviceId), "ID should reference a unique device");
        }

        final List<Device> downstreamDevices = extractor.downstreamDevices(masterDevice.get());
        Beacon3100MeterDetails[] beacon3100MeterDetails = new Beacon3100MeterDetails[downstreamDevices.size()];
        for (int index = 0; index < downstreamDevices.size(); index++) {
            Device device = downstreamDevices.get(index);
            beacon3100MeterDetails[index] = createMeterDetails(device, masterDevice.get());
        }

        return beacon3100MeterDetails;
    }

    public List<Beacon3100DeviceType> getDeviceTypes(int deviceId, boolean readOldObisCodes) {
        Optional<Device> masterDevice = extractor.find(deviceId);
        if (!masterDevice.isPresent()) {
            throw invalidFormatException("'DC device ID'", String.valueOf(deviceId), "ID should reference a unique device");
        }

        final List<Device> downstreamDevices = extractor.downstreamDevices(masterDevice.get());
        final AllMasterData allMasterData = new AllMasterData();
        for (Device device : downstreamDevices) {
            //Add all information about the device type config. (only once per device type config)
            final DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration = extractor.configuration(device);
            if (!deviceTypeAlreadyExists(allMasterData.getDeviceTypes(), deviceConfiguration)) {
                addDeviceConfiguration(masterDevice.get(), allMasterData, device, deviceConfiguration, readOldObisCodes);
            }
        }
        return allMasterData.getDeviceTypes();
    }

    public Beacon3100MeterDetails getMeterDetails(int slaveDeviceId, int masterId) {
        Optional<Device> masterDevice = extractor.find(masterId);
        if (!masterDevice.isPresent()) {
            throw invalidFormatException("'DC device ID'", String.valueOf(masterId), "ID should reference a unique device");
        }
        Optional<Device> slaveDevice = extractor.find(slaveDeviceId);
        final List<Device> downstreamDevices = extractor.downstreamDevices(masterDevice.get());
        for (Device device : downstreamDevices) {
            if (extractor.id(slaveDevice.get()) == extractor.id(device)) {
                return createMeterDetails(device, masterDevice.get());
            }
        }

        return null;
    }

    private Beacon3100MeterDetails createMeterDetails(Device device, Device masterDevice) {

        //A comtask for the DC has a schedule and at least one obiscode.
        List<DeviceMasterDataExtractor.CommunicationTask> mirrorComTasks = new ArrayList<>();
        DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration = this.extractor.configuration(device);
        for (DeviceMasterDataExtractor.CommunicationTask communicationTask : this.extractor.enabledTasks(device)) {
            if (getScheduleName(communicationTask) != null) {
                List<SchedulableItem> loadProfileObisCodes = getLoadProfileObisCodesForComTask(deviceConfiguration, communicationTask);
                List<SchedulableItem> registerObisCodes = getRegisterObisCodesForComTask(deviceConfiguration, communicationTask);
                List<SchedulableItem> logBookObisCodes = getLogBookObisCodesForComTask(deviceConfiguration, communicationTask);
                if (isReadMeterDataTask(loadProfileObisCodes, registerObisCodes, logBookObisCodes)) {
                    mirrorComTasks.add(communicationTask);
                }
            }
        }

        final String callHomeId = this.parseCallHomeId(device);

        final long configurationId = this.extractor.configuration(device).id();     //The ID of the config, instead of the device type. Since every new config represents a unique device type in the Beacon model

        String deviceTimeZone = DlmsProtocolProperties.DEFAULT_TIMEZONE;
        final TimeZone timeZone = this.extractor.protocolProperties(device).getTypedProperty(DlmsProtocolProperties.TIMEZONE);
        if (timeZone != null) {
            deviceTimeZone = timeZone.getID();
        }

        //The dlmsMeterKEK is a general property on the Beacon DC device
        final byte[] dlmsMeterKEK = this.parseKey(masterDevice, Beacon3100ConfigurationSupport.DLMS_METER_KEK);

        //Get the DLMS keys from the device. If they are empty, an empty OctetString will be sent to the beacon.
        final byte[] password = getSecurityKey(device, SecurityPropertySpecTranslationKeys.PASSWORD.toString(), null, mirrorComTasks);
        final byte[] ak = getSecurityKey(device, SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), null, mirrorComTasks);
        final byte[] ek = getSecurityKey(device, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), null, mirrorComTasks);

        final String wrappedPassword = password == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, password), "");
        final String wrappedAK = ak == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, ak), "");
        final String wrappedEK = ek == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, ek), "");

        return new Beacon3100MeterDetails(
                callHomeId,
                configurationId,
                deviceTimeZone,
                this.extractor.serialNumber(device),
                createClientDetails(device, dlmsMeterKEK, mirrorComTasks),
                wrappedPassword,
                wrappedAK,
                wrappedEK);
    }

    private List<Beacon3100ClientDetails> createClientDetails(Device device, byte[] dlmsMeterKEK, List<DeviceMasterDataExtractor.CommunicationTask> mirrorComTasks) {
        byte[] password = null;
        byte[] hlsPassword = null;
        byte[] ak = null;
        byte[] ek = null;
        List<Beacon3100ClientDetails> clientDetails = new ArrayList<>();
        final long initialFrameCounter = this.extractor.protocolProperties(device).getTypedProperty(AM540ConfigurationSupport.INITIAL_FRAME_COUNTER, BigDecimal.valueOf(-1)).longValue();

        Set<DeviceMasterDataExtractor.SecurityPropertySet> securityPropertySets = new HashSet<>();
        for (DeviceMasterDataExtractor.CommunicationTask mirrorComTask : mirrorComTasks) {
            DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet = mirrorComTask.securityPropertySet();
            if (securityPropertySet != null) {
                securityPropertySets.add(securityPropertySet);
            }
        }

        for (DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet : securityPropertySets) {
            final int clientId = ((BigDecimal) securityPropertySet.client()).intValue();
            for (DeviceMasterDataExtractor.SecurityProperty protocolSecurityProperty : this.extractor.securityProperties(device, securityPropertySet)) {
                validateDlmsClient(clientId);
                if (protocolSecurityProperty.name().equals(SecurityPropertySpecTranslationKeys.PASSWORD.toString())) {
                    if (securityPropertySet.authenticationDeviceAccessLevelId() >= 3) {
                        hlsPassword = parseASCIIPassword(device, protocolSecurityProperty.name(), (String) protocolSecurityProperty.value());
                    } else {
                        password = parseASCIIPassword(device, protocolSecurityProperty.name(), (String) protocolSecurityProperty.value());
                    }
                } else if (protocolSecurityProperty.name().equals(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString())) {
                    ak = parseKey(device, protocolSecurityProperty.name(), (String) protocolSecurityProperty.value());
                } else if (protocolSecurityProperty.name().equals(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString())) {
                    ek = parseKey(device, protocolSecurityProperty.name(), (String) protocolSecurityProperty.value());
                }
            }
            //Get the DLMS keys from the device. If they are empty, an empty OctetString will be sent to the beacon.
            final String wrappedPassword = password == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, password), "");
            final String wrappedHLSPassword = hlsPassword == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, hlsPassword), "");
            final String wrappedAK = ak == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, ak), "");
            final String wrappedEK = ek == null ? "" : ProtocolTools.getHexStringFromBytes(wrap(dlmsMeterKEK, ek), "");
            clientDetails.add(new Beacon3100ClientDetails(clientId, new Beacon3100ConnectionDetails(wrappedPassword, wrappedHLSPassword, wrappedAK, wrappedEK, initialFrameCounter)));
        }

        return clientDetails;
    }

    /**
     * AES wrap the given key with the given master key (KEK).
     * This method is overridden in the crypto-protocols for EVN, where the wrapping is done by the HSM.
     */
    public byte[] wrap(byte[] masterKey, byte[] key) {
        return ProtocolTools.aesWrap(key, masterKey);
    }

    public String parseCallHomeId(Device device) {
        final String callHomeId = TypedProperties.copyOf(this.extractor.protocolProperties(device)).getStringProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
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

    public String jsonSerialize(Object object) {
        ObjectMapper mapper = this.objectMapperService.newJacksonMapper();

        SimpleModule module = new SimpleModule("ScheduleableItemSerializerModule", new Version(1, 0, 0, null));
        module.addSerializer(SchedulableItem.class, new ScheduleableItemDataSerializer());
        module.addDeserializer(SchedulableItem.class, new SchedulableItemDeserializer());
        mapper.registerModule(module);

        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, object);
        } catch (IOException e) {
            throw DataParseException.generalParseException(e);
        }
        return writer.toString();
    }

    private boolean scheduleAlreadyExists(List<Beacon3100Schedule> schedules, DeviceMasterDataExtractor.CommunicationTask task) {
        for (Beacon3100Schedule schedule : schedules) {
            if (schedule.getId() == getScheduleId(task)) {
                return true;
            }
        }
        return false;
    }

    private String getScheduleName(DeviceMasterDataExtractor.CommunicationTask communicationTask) {
        Optional<DeviceMasterDataExtractor.NextExecutionSpecs> nextExecutionSpecs = communicationTask.nextExecutionSpecs();
        if (nextExecutionSpecs.isPresent()) {
            return nextExecutionSpecs
                    .filter(specs -> specs.type().equals(DeviceMasterDataExtractor.SchedulingSpecificationType.TEMPORAL))
                    .map(DeviceMasterDataExtractor.NextExecutionSpecs::displayName)
                    .orElseThrow(() -> DeviceConfigurationException.invalidPropertyFormat("Comtask schedule", "Read schedule with ID", "A read schedule (dial calendar) is not supported by this message"));
        } else {
            // We don't want the exception if the task is not configured to be rescheduled
            return null;
        }
    }

    private Beacon3100ClockSyncConfiguration getClockSyncConfiguration(Device device) {
        for (DeviceMasterDataExtractor.CommunicationTask enabledTask : this.extractor.enabledTasks(device)) {
            for (DeviceMasterDataExtractor.ProtocolTask protocolTask : enabledTask.protocolTasks()) {
                if (protocolTask instanceof DeviceMasterDataExtractor.Clock) {
                    long min = ((DeviceMasterDataExtractor.Clock) protocolTask).minimumClockDifference().getSeconds();
                    long max = ((DeviceMasterDataExtractor.Clock) protocolTask).maximumClockDifference().getSeconds();
                    return new Beacon3100ClockSyncConfiguration(true, min, max);
                }
            }
        }
        return new Beacon3100ClockSyncConfiguration(false, 0, 0xFFFF);
    }

    /**
     * Gather scheduling info by iterating over the comtask enablements (on config level), this will be the same for all devices of the same device type & config.
     */
    private List<Beacon3100Schedulable> getSchedulables(Device device, DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration, AllMasterData allMasterData, boolean readOldObisCodes) {
        List<Beacon3100Schedulable> schedulables = new ArrayList<>();
        for (DeviceMasterDataExtractor.CommunicationTask enabledTask : this.extractor.enabledTasks(device)) {
            final long scheduleId = getScheduleId(enabledTask);

            //Don't add the task if it has no schedule
            if (scheduleId != NO_SCHEDULE) {
                final int logicalDeviceId = 1;  //Linky and AM540 devices always use 1 as logical device
                long clientTypeId = getClientTypeId(enabledTask);

                List<SchedulableItem> loadProfileObisCodes = this.getLoadProfileObisCodesForComTask(deviceConfiguration, enabledTask);
                List<SchedulableItem> registerObisCodes = this.getRegisterObisCodesForComTask(deviceConfiguration, enabledTask);
                List<SchedulableItem> logBookObisCodes = this.getLogBookObisCodesForComTask(deviceConfiguration, enabledTask);

                if (enabledTask.isConfiguredToCollectLoadProfileData() && loadProfileObisCodes.isEmpty()) {
                    allMasterData.getWarningKeys().add("emptyLoadProfileComTask");
                    allMasterData.getWarningArguments().add(enabledTask.name());
                }
                if (enabledTask.isConfiguredToCollectEvents() && logBookObisCodes.isEmpty()) {
                    allMasterData.getWarningKeys().add("emptyLogbookComTask");
                    allMasterData.getWarningArguments().add(enabledTask.name());
                }
                if (enabledTask.isConfiguredToCollectRegisterData() && registerObisCodes.isEmpty()) {
                    allMasterData.getWarningKeys().add("emptyRegisterComTask");
                    allMasterData.getWarningArguments().add(enabledTask.name());
                }

                if (isReadMeterDataTask(loadProfileObisCodes, registerObisCodes, logBookObisCodes)) {
                    final Beacon3100Schedulable schedulable;
                    if (readOldObisCodes) {
                        schedulable = new Beacon3100Schedulable(enabledTask.id(), scheduleId, logicalDeviceId, clientTypeId, loadProfileObisCodes, registerObisCodes, logBookObisCodes);
                    } else {
                        schedulable = new Beacon3100Schedulable(enabledTask.id(), scheduleId, logicalDeviceId, clientTypeId, loadProfileObisCodes, registerObisCodes, logBookObisCodes, false);
                    }
                    schedulables.add(schedulable);
                }
            }
        }
        return schedulables;
    }

    private boolean isReadMeterDataTask(List<SchedulableItem> loadProfileObisCodes, List<SchedulableItem> registerObisCodes, List<SchedulableItem> logBookObisCodes) {
        return !(loadProfileObisCodes.isEmpty() && registerObisCodes.isEmpty() && logBookObisCodes.isEmpty());
    }

    private long getClientTypeId(DeviceMasterDataExtractor.CommunicationTask comTaskEnablement) {
        return comTaskEnablement.securityPropertySet().id();
    }

    /**
     * Return the hash code of the spec name.
     * We want 2 specs that have exactly the same name (e.g. 'every day at 00:00') to have the same ID.
     */
    private long getScheduleId(DeviceMasterDataExtractor.CommunicationTask communicationTask) {
        return Optional.ofNullable(getScheduleName(communicationTask)).map(this::hashForScheduleName).orElse(NO_SCHEDULE);
    }

    private long hashForScheduleName(String scheduleName) {
        return Math.abs(scheduleName.hashCode());   //Make sure the hash is a positive number
    }

    private List<SchedulableItem> getLogBookObisCodesForComTask(DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration, DeviceMasterDataExtractor.CommunicationTask communicationTask) {
        Unsigned32 defaultBacklogSize = new Unsigned32(getProperties().getDefaultBacklogEventLogInSeconds());
        Set<SchedulableItem> logBookObisCodes = new HashSet<>();
        if (communicationTask.isConfiguredToCollectEvents()) {
            for (DeviceMasterDataExtractor.ProtocolTask protocolTask : communicationTask.protocolTasks()) {
                if (protocolTask instanceof DeviceMasterDataExtractor.LogBooks) {
                    final Collection<DeviceMasterDataExtractor.LogBookType> logBookTypes = ((DeviceMasterDataExtractor.LogBooks) protocolTask).types();
                    if (logBookTypes.isEmpty()) {
                        //if no specific logbook type is specified in logbook protocol task then use the logbook specification from device configuration
                        deviceConfiguration
                                .logBookSpecs()
                                .stream()
                                .map(DeviceMasterDataExtractor.LogBookSpec::deviceObisCode)
                                .map(obisCode -> new SchedulableItem(obisCode, defaultBacklogSize))
                                .forEach(logBookObisCodes::add);
                    } else {
                        /* if we have specific logbook types defined in logbook protocol task then add only then add only logbook types obiscodes
                         * that are present in both device configuration and protocol task configuration. */
                        deviceConfiguration
                                .logBookSpecs()
                                .stream()
                                .filter(each -> logBookTypes.contains(each.type()))
                                .map(DeviceMasterDataExtractor.LogBookSpec::deviceObisCode)
                                .map(obisCode -> new SchedulableItem(obisCode, defaultBacklogSize))
                                .forEach(logBookObisCodes::add);
                    }
                }
            }

        }
        return new ArrayList<>(logBookObisCodes);
    }

    private List<SchedulableItem> getRegisterObisCodesForComTask(DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration, DeviceMasterDataExtractor.CommunicationTask communicationTask) {
        Unsigned16 defaultBufferSize = new Unsigned16(getProperties().getDefaultBufferSizeRegisters());

        Set<SchedulableItem> registerObisCodes = new HashSet<>();
        if (communicationTask.isConfiguredToCollectRegisterData()) {
            for (DeviceMasterDataExtractor.ProtocolTask protocolTask : communicationTask.protocolTasks()) {
                if (protocolTask instanceof Registers) {
                    final Collection<DeviceMasterDataExtractor.RegisterGroup> registerGroups = ((Registers) protocolTask).groups();
                    if (registerGroups.isEmpty()) {
                        for (DeviceMasterDataExtractor.RegisterSpec register : deviceConfiguration.registerSpecs()) {
                            registerObisCodes.add(new SchedulableItem(register.deviceObisCode(), defaultBufferSize));
                        }
                    } else {
                        for (DeviceMasterDataExtractor.RegisterSpec registerSpec : deviceConfiguration.registerSpecs()) {
                            for (DeviceMasterDataExtractor.RegisterGroup registerGroup : registerGroups) {
                                if (registerSpec.contains(registerGroup)) {
                                    registerObisCodes.add(new SchedulableItem(registerSpec.deviceObisCode(), defaultBufferSize));
                                }
                            }
                        }
                    }
                }
            }
        }
        return filterOutUnwantedRegisterObisCodes(registerObisCodes);
    }

    private List<SchedulableItem> filterOutUnwantedRegisterObisCodes(Set<SchedulableItem> obisCodes) {
        Iterator<SchedulableItem> iterator = obisCodes.iterator();
        while (iterator.hasNext()) {
            SchedulableItem item = iterator.next();
            ObisCode obisCode = item.getObisCode();
            if (Clock.getDefaultObisCode().equals(obisCode) ||
                    FIXED_SERIAL_NUMBER_OBISCODE.equals(obisCode)
                    ) {
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

    private List<SchedulableItem> getLoadProfileObisCodesForComTask(DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration, DeviceMasterDataExtractor.CommunicationTask communicationTask) {
        Unsigned32 defaultBacklogSize = new Unsigned32(getProperties().getDefaultBacklogLoadProfileInSeconds());
        Set<SchedulableItem> loadProfileObisCodes = new HashSet<>();
        if (communicationTask.isConfiguredToCollectLoadProfileData()) {
            for (DeviceMasterDataExtractor.ProtocolTask protocolTask : communicationTask.protocolTasks()) {
                if (protocolTask instanceof DeviceMasterDataExtractor.LoadProfiles) {
                    final Collection<DeviceMasterDataExtractor.LoadProfileType> loadProfileTypes = ((DeviceMasterDataExtractor.LoadProfiles) protocolTask).types();
                    if (loadProfileTypes.isEmpty()) {
                        deviceConfiguration
                                .loadProfileSpecs()
                                .stream()
                                .map(DeviceMasterDataExtractor.LoadProfileSpec::deviceObisCode)
                                .map(obisCode -> new SchedulableItem(obisCode, defaultBacklogSize))
                                .forEach(loadProfileObisCodes::add);
                    } else {
                        loadProfileTypes
                                .stream()
                                .map(DeviceMasterDataExtractor.LoadProfileType::obisCode)
                                .map(obisCode -> new SchedulableItem(obisCode, defaultBacklogSize))
                                .forEach(loadProfileObisCodes::add);
                    }
                }
            }
        }
        return new ArrayList<>(loadProfileObisCodes);
    }

    private Beacon3100ClientType getClientType(Device device, DeviceMasterDataExtractor.CommunicationTask task) {
        final long clientTypeId = this.getClientTypeId(task);
        final DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet = task.securityPropertySet();

        Collection<DeviceMasterDataExtractor.SecurityProperty> securityProperties = this.extractor.securityProperties(device, securityPropertySet);
        BigDecimal clientMacAddress;
        try {
            clientMacAddress = new BigDecimal(Integer.parseInt(securityPropertySet.client().toString()));
        } catch (NumberFormatException e) {
            throw DeviceConfigurationException.unsupportedPropertyValue(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(), securityPropertySet.client().toString());
        }

        int securitySuiteId = securityPropertySet.securitySuite();         //Note that -1 means undefined, we change that to the default suite (0)
        final int securitySuite = securitySuiteId == -1 ? 0 : securitySuiteId;

        return new Beacon3100ClientType(
                clientTypeId,
                clientMacAddress.intValue(),
                securitySuite,
                securityPropertySet.authenticationDeviceAccessLevelId(),
                securityPropertySet.encryptionDeviceAccessLevelId());
    }

    private BigDecimal defaultClientMacAddress(DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet) {
        return securityPropertySet
                .propertySpecs()
                .stream()
                .filter(each -> each.getName().equals(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString()))
                .findAny()
                .map(PropertySpec::getDefaultValue)
                .map(BigDecimal.class::cast)
                .orElse(BigDecimal.ONE);
    }

    /**
     * The protocol java class name and the properties (both general & dialect).
     * Note that the properties are fetched from config level, so they are the same for every device that has the same device type.
     */
    private Beacon3100ProtocolConfiguration getProtocolConfiguration(DeviceMasterDataExtractor.DeviceConfiguration deviceConfiguration, Device masterDevice) {
        final String javaClassName = deviceConfiguration.protocolJavaClassName();
        TypedProperties allProperties = TypedProperties.empty();

        //General props & pluggable class props, from the configuration of the slave device
        allProperties.setAllProperties(deviceConfiguration.properties(), true);

        //These properties will be used to read out the connected e-meters (down stream). The actual e-meter only has 1 logical device, it's ID is 1.
        allProperties.setProperty(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, BigDecimal.ONE);
        allProperties.setProperty(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, BigDecimal.ONE);
        allProperties.setProperty(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
        allProperties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), BigDecimal.ONE);

        //No need to send the PSK property
        allProperties.removeProperty(G3Properties.PSK);

        /* Use properties of dialect with name 'GatewayTcpDlmsDialect' of the master device configuration
         * since the physical slave doesn't have dialect properties. */
        if (masterDevice != null) {
            this.extractor
                    .configuration(masterDevice)
                    .dialectProperties(DeviceProtocolDialectTranslationKeys.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName())
                    .ifPresent(allProperties::setAllProperties);
        }

        addDefaultDialectValuesIfNecessary(allProperties);

        //Add the name of the gateway dialect. The downstream protocol talks directly to the e-meter, just like when using the gateway functionality of the beacon.
        allProperties.setProperty(DEVICE_PROTOCOL_DIALECT.getName(), DeviceProtocolDialectTranslationKeys.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());

        return new Beacon3100ProtocolConfiguration(javaClassName, allProperties);
    }

    /**
     * For all properties who are not yet specified - but for which a default value exist - the default value will be added.
     * Note that we specifically use the gateway TCP dialect of the Beacon3100 protocol for this.
     */
    private void addDefaultDialectValuesIfNecessary(TypedProperties dialectProperties) {
        DeviceProtocolDialect theActualDialect = new GatewayTcpDeviceProtocolDialect(this.propertySpecService, this.nlsService);
        for (PropertySpec propertySpec : theActualDialect.getUPLPropertySpecs()) {
            if (!dialectProperties.hasValueFor(propertySpec.getName()) && propertySpec.getPossibleValues() != null) {
                dialectProperties.setProperty(propertySpec.getName(), propertySpec.getPossibleValues().getDefault());
            }
        }
    }

    private boolean deviceTypeAlreadyExists(List<Beacon3100DeviceType> beacon3100DeviceTypes, DeviceMasterDataExtractor.DeviceConfiguration configuration) {
        for (Beacon3100DeviceType beacon3100DeviceType : beacon3100DeviceTypes) {
            if (beacon3100DeviceType.getId() == configuration.id()) {
                return true;
            }
        }
        return false;
    }

    private boolean clientTypeAlreadyExists(List<Beacon3100ClientType> clientTypes, long clientTypeId) {
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
    public byte[] getSecurityKey(Device device, String propertyName, Integer clientMacAddress) {
        return getSecurityKey(device, propertyName, clientMacAddress, null);
    }

    /**
     * Iterate over the security sets that have the given clientMacAddress to find a certain security property.
     * If the given clientMacAddress is null, iterate over all security sets.
     * If the requested property is not defined on any security set, return null.
     * <p>
     * Note that the list of security sets can be based on a given list of comtasks, or on all comtask of the device.
     */
    public byte[] getSecurityKey(Device device, String propertyName, Integer clientMacAddress, List<DeviceMasterDataExtractor.CommunicationTask> mirrorComTasks) {
        Set<DeviceMasterDataExtractor.SecurityPropertySet> securityPropertySets = new HashSet<>();
        if (mirrorComTasks != null) {
            for (DeviceMasterDataExtractor.CommunicationTask mirrorComTask : mirrorComTasks) {
                DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet = mirrorComTask.securityPropertySet();
                if (securityPropertySet != null) {
                    securityPropertySets.add(securityPropertySet);
                }
            }
        } else {
            securityPropertySets.addAll(extractor.securityPropertySets(device));
        }

        List<DeviceMasterDataExtractor.SecurityPropertySet> securitySets = new ArrayList<>();
        for (DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet : securityPropertySets) {
            if (clientMacAddress == null) {
                securitySets.add(securityPropertySet);
            } else {
                for (DeviceMasterDataExtractor.SecurityProperty protocolSecurityProperty : this.extractor.securityProperties(device, securityPropertySet)) {
                    //Only add this security set if it is for the given clientMacAddress
                    if (protocolSecurityProperty.name().equals(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString())
                            && ((BigDecimal) protocolSecurityProperty.value()).intValue() == clientMacAddress) {
                        securitySets.add(securityPropertySet);
                    }
                }
            }
        }

        for (DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet : securitySets) {
            final Collection<DeviceMasterDataExtractor.SecurityProperty> securityProperties = this.extractor.securityProperties(device, securityPropertySet);
            for (DeviceMasterDataExtractor.SecurityProperty securityProperty : securityProperties) {
                if (securityProperty.name().equals(propertyName)) {
                    final String propertyValue = (String) securityProperty.value();
                    if (propertyName.equals(SecurityPropertySpecTranslationKeys.PASSWORD.toString())) {
                        return parseASCIIPassword(this.extractor.id(device), propertyName, propertyValue);
                    } else {
                        return parseKey(device, propertyName, propertyValue);
                    }
                }
            }
        }
        return null;
    }

    public byte[] parseKey(Device device, String propertyName) {
        return this.parseKey(device, propertyName, TypedProperties.copyOf(this.extractor.protocolProperties(device)).getStringProperty(propertyName));
    }

    public byte[] parseKey(Device device, String propertyName, String propertyValue) {
        if (propertyValue == null) {
            throw missingProperty(propertyName);
        }
        if (propertyValue.length() != 32) {
            throw invalidFormatException(propertyName, "(hidden) of the device with id:" + this.extractor.id(device), " should have 32 hex characters.");
        }
        try {
            return ProtocolTools.getBytesFromHexString(propertyValue, "");
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw invalidFormatException(propertyName, "(hidden) of the device with id:" + this.extractor.id(device), " should have 32 hex characters.");
        }
    }

    protected byte[] parseASCIIPassword(Device device, String propertyName, String propertyValue) {
        return parseASCIIPassword(this.extractor.id(device), propertyName, propertyValue);
    }

    private byte[] parseASCIIPassword(long deviceId, String propertyName, String propertyValue) {
        if (propertyValue == null) {
            throw missingProperty(propertyName);
        }
        if ((propertyValue.length() % 8) != 0) {
            throw invalidFormatException(propertyName, "(hidden) of the device with id:" + deviceId, " should be a multiple of 8 ASCII characters.");
        }
        try {
            return propertyValue.getBytes();
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw invalidFormatException(propertyName, "(hidden) of the device with id:" + deviceId, " should be a multiple of 8 ASCII characters.");
        }
    }

    protected DeviceConfigurationException invalidFormatException(String propertyName, String propertyValue, String message) {
        return DeviceConfigurationException.invalidPropertyFormat(propertyName, propertyValue, message);
    }

    protected DeviceConfigurationException missingProperty(String propertyName) {
        return DeviceConfigurationException.missingProperty(propertyName);
    }

    private void validateDlmsClient(int clientId) {
        if (clientId == PUBLIC_CLIENT_ID) {
            //Drop full sync process if we find a configuration that uses public client
            String message = "We should NEVER try to synchronize a device configured to be readout with public client. Please change the configured dlms client with a proper one and try again!";
            throw DeviceConfigurationException.unsupportedPropertyValueWithReason(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(), String.valueOf(PUBLIC_CLIENT_ID), message);
        }
    }

    protected Beacon3100Properties getProperties() {
        return beacon3100Properties;
    }

}
