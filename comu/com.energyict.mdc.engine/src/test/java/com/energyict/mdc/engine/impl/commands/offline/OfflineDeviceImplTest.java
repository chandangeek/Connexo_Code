package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageFactory;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.impl.device.messages.ClockDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.ContactorDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageAttributes;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link OfflineDeviceImpl} component.
 *
 * @author gna
 * @since 17/04/12 - 9:47
 */
@RunWith(MockitoJUnitRunner.class)
public class OfflineDeviceImplTest {

    private static final String cp_prop1 = "CP_Prop1";
    private static final String cp_prop2 = "CP_Prop2";
    private static final String cp_prop3 = "CP_Prop3";
    private static final String cp_propValue1 = "CP_PropValue1";
    private static final String cp_propValue2 = "CP_PropValue2";
    private static final String cp_propValue3 = "CP_PropValue3";

    private static final String device_prop1 = "DEVICE_Prop1";
    private static final String device_prop2 = "DEVICE_Prop2";
    private static final String device_prop3 = "DEVICE_Prop3";
    private static final String DEVICE_TIMEZONE_PROPERTY = "deviceTimeZone";
    private static final String device_propValue1 = "DEVICE_PropValue1";
    private static final String device_propValue2 = "DEVICE_PropValue2";
    private static final String device_propValue3 = "DEVICE_PropValue3";

    private static final String meterSerialNumber = "MeterSerialNumber";
    private static final int DEVICE_ID = 93;
    private static final TimeZone deviceProtocolPluggableClassTimeZone = TimeZone.getTimeZone("GMT");
    private static final TimeZone deviceTimeZone = TimeZone.getTimeZone("GMT+05");

    private static final String CALENDAR_NAME = "Whatever";
    @Mock
    DataModel dataModel;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceMessageFactory deviceMessageFactory;
    @Mock
    private TopologyService topologyService;
    @Mock
    private OfflineDeviceImpl.ServiceProvider offlineDeviceServiceProvider;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private FirmwareService firmwareService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private EventService eventService;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;

    private static LoadProfile getNewMockedLoadProfile(final ObisCode obisCode, Device device) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getLastReading()).thenReturn(null);
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile.getLoadProfileTypeObisCode()).thenReturn(obisCode);
        when(loadProfile.getDevice()).thenReturn(device);
        return loadProfile;
    }

    private TypedProperties getDeviceProtocolProperties() {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(cp_prop1, cp_propValue1);
        properties.setProperty(cp_prop2, cp_propValue2);
        properties.setProperty(cp_prop3, cp_propValue3);
        properties.setProperty(DEVICE_TIMEZONE_PROPERTY, deviceTimeZone);
        return properties;
    }

    private DeviceProtocolPluggableClass createMockDeviceProtocolPluggableClass() {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getProperties(anyList())).thenReturn(getDeviceProtocolPluggableClassProperties());
        return deviceProtocolPluggableClass;
    }

    private TypedProperties getDeviceProperties() {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(device_prop1, device_propValue1);
        properties.setProperty(device_prop2, device_propValue2);
        properties.setProperty(device_prop3, device_propValue3);
        return properties;
    }

    private TypedProperties getDeviceProtocolPluggableClassProperties() {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(DEVICE_TIMEZONE_PROPERTY, deviceProtocolPluggableClassTimeZone);
        return properties;
    }

    private Device createMockDevice(final long deviceId, final String meterSerialNumber) {
        return createMockDevice(deviceId, meterSerialNumber, mock(DeviceProtocol.class));
    }

    private Device createMockDevice(final long deviceId, final String meterSerialNumber, final DeviceProtocol deviceProtocol) {
        DeviceType deviceType = mock(DeviceType.class);
        Device device = mock(Device.class);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceProtocolProperties()).thenReturn(getDeviceProtocolProperties());
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = createMockDeviceProtocolPluggableClass();
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(device.getId()).thenReturn(deviceId);
        when(device.getSerialNumber()).thenReturn(meterSerialNumber);
        return device;
    }

    private Device createMockDevice() {
        return createMockDevice(DEVICE_ID, meterSerialNumber);
    }

    @Before
    public void initBefore() {
        when(this.offlineDeviceServiceProvider.findProtocolCacheByDevice(any(Device.class))).thenReturn(Optional.empty());
        when(this.offlineDeviceServiceProvider.thesaurus()).thenReturn(this.thesaurus);
        when(this.offlineDeviceServiceProvider.topologyService()).thenReturn(this.topologyService);
        when(this.offlineDeviceServiceProvider.identificationService()).thenReturn(this.identificationService);
        when(this.offlineDeviceServiceProvider.firmwareService()).thenReturn(this.firmwareService);
        when(this.offlineDeviceServiceProvider.deviceConfigurationService()).thenReturn(this.deviceConfigurationService);
        when(this.offlineDeviceServiceProvider.eventService()).thenReturn(eventService);
        when(this.topologyService.getPhysicalGateway(any(Device.class))).thenReturn(Optional.empty());
        when(this.topologyService.getPhysicalGateway(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        when(this.topologyService.findPhysicalConnectedDevices(any(Device.class))).thenReturn(Collections.<Device>emptyList());
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        when(this.firmwareService.findFirmwareManagementOptions(any(DeviceType.class))).thenReturn(Optional.empty());
        when(this.deviceConfigurationService.findTimeOfUseOptions(any(DeviceType.class))).thenReturn(Optional.empty());
    }

    private int getTotalSizeOfProperties() {
        return getDeviceProperties().size() + getDeviceProtocolProperties().size() + getDeviceProtocolPluggableClassProperties().size();
    }

    @Before
    public void setupDeviceMessageService() {
        when(this.thesaurus.getString(anyString(), anyString())).thenReturn("Translation not supported in unit testing");
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        PropertySpecService propertySpecService = new PropertySpecServiceImpl(new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(), dataVaultService, ormService);
    }

    @Test
    public void goOfflineTest() {
        Device device = createMockDevice();
        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        assertEquals("The SerialNumber should match", meterSerialNumber, offlineRtu.getSerialNumber());
        assertEquals("The ID should match", DEVICE_ID, offlineRtu.getId());
        assertNotNull("The DeviceProtocol PluggableClass should not be null", offlineRtu.getDeviceProtocolPluggableClass());
    }

    @Test
    public void propertiesTest() {
        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(createMockDevice(), DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        offlineRtu.addProperties(getDeviceProperties());
        offlineRtu.addProperties(getDeviceProtocolProperties());
        assertEquals("Size should be equal to seven", getTotalSizeOfProperties(), offlineRtu.getAllProperties().localSize());
        assertEquals(cp_propValue1, offlineRtu.getAllProperties().getProperty(cp_prop1));
        assertEquals(cp_propValue2, offlineRtu.getAllProperties().getProperty(cp_prop2));
        assertEquals(cp_propValue3, offlineRtu.getAllProperties().getProperty(cp_prop3));
        assertEquals(device_propValue1, offlineRtu.getAllProperties().getProperty(device_prop1));
        assertEquals(device_propValue2, offlineRtu.getAllProperties().getProperty(device_prop2));
        assertEquals(device_propValue3, offlineRtu.getAllProperties().getProperty(device_prop3));
        assertEquals(device_propValue3, offlineRtu.getAllProperties().getProperty(device_prop3));
        assertEquals(offlineRtu.getSerialNumber(), offlineRtu.getAllProperties().getProperty(MeterProtocol.SERIALNUMBER));
    }

    @Test
    public void convertToOfflineRtuTest() {
        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);

        Device rtu = createMockDevice();

        Device slave1 = createMockDevice(123, "slave1");
        when(slave1.getDeviceType()).thenReturn(slaveRtuType);
        Device slaveFromSlave1 = createMockDevice(789, "slaveFromSlave1");
        when(slaveFromSlave1.getDeviceType()).thenReturn(slaveRtuType);
        when(this.topologyService.findPhysicalConnectedDevices(slave1)).thenReturn(Arrays.asList(slaveFromSlave1));

        Device slave2 = createMockDevice(456, "slave2");
        when(slave2.getDeviceType()).thenReturn(slaveRtuType);
        when(this.topologyService.findPhysicalConnectedDevices(rtu)).thenReturn(Arrays.asList(slave1, slave2));

        // Business method
        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(rtu, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // Asserts
        assertNotNull(offlineRtu.getAllSlaveDevices());
        assertEquals("Expected three slave devices", 3, offlineRtu.getAllSlaveDevices().size());
    }

    @Test
    public void getAllLoadProfilesIncludingDownStreamsTest() {
        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);

        Device device = createMockDevice();

        Device slave1 = createMockDevice(123, "slave1");
        when(slave1.getDeviceType()).thenReturn(slaveRtuType);
        Device slaveFromSlave1 = createMockDevice(789, "slaveFromSlave1");
        when(slaveFromSlave1.getDeviceType()).thenReturn(slaveRtuType);
        OfflineDevice offlineSlaveFromSlave1 = new OfflineDeviceImpl(slaveFromSlave1, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        OfflineDevice offlineSlave1 = new OfflineDeviceImpl(slave1, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        when(this.topologyService.findPhysicalConnectedDevices(slave1)).thenReturn(Arrays.asList(slaveFromSlave1));

        Device slave2 = createMockDevice(456, "slave2");
        when(slave2.getDeviceType()).thenReturn(slaveRtuType);
        OfflineDevice offlineSlave2 = new OfflineDeviceImpl(slave2, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        when(this.topologyService.findPhysicalConnectedDevices(device)).thenReturn(Arrays.asList(slave1, slave2));

        ObisCode obisCode1 = ObisCode.fromString("1.0.99.1.0.255");
        ObisCode obisCode2 = ObisCode.fromString("1.0.99.2.0.255");
        ObisCode obisCode3 = ObisCode.fromString("1.0.99.3.0.255");
        ObisCode obisCode4 = ObisCode.fromString("1.0.99.4.0.255");
        ObisCode obisCodex = ObisCode.fromString("1.x.24.1.0.255");

        LoadProfile loadProfile1 = getNewMockedLoadProfile(obisCode1, device);
        LoadProfile loadProfile2 = getNewMockedLoadProfile(obisCode2, device);
        LoadProfile loadProfile3 = getNewMockedLoadProfile(obisCode3, device);
        LoadProfile loadProfile4 = getNewMockedLoadProfile(obisCode4, device);
        LoadProfile loadProfilex = getNewMockedLoadProfile(obisCodex, device);

        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(slave1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile3, loadProfilex));
        when(slave2.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile4, loadProfile2));
        when(slaveFromSlave1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfilex));

        OfflineDeviceImpl offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // Asserts
        assertNotNull(offlineDevice.getMasterOfflineLoadProfiles());
        assertEquals("Expected two loadProfiles on the master", 2, offlineDevice.getMasterOfflineLoadProfiles().size());
        assertNotNull(offlineDevice.getAllOfflineLoadProfiles());
        assertEquals("Expected 9 loadProfiles in total", 9, offlineDevice.getAllOfflineLoadProfiles().size());
    }

    @Test
    public void getAllRegistersTest() {
        Device device = createMockDevice();
        RegisterType registerType = mock(RegisterType.class);
        Register register1 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        Register register2 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));

        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // asserts
        assertNotNull(offlineRtu.getAllOfflineRegisters());
        assertEquals("Should have gotten 2 registers", 2, offlineRtu.getAllOfflineRegisters().size());
    }

    @Test
    public void getAllRegistersIncludingSlavesTest() {

        OfflineDevice mockOfflineDevice = mock(OfflineDevice.class);

        Device device = createMockDevice();
        RegisterType registerType = mock(RegisterType.class);
        Register register1 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        Register register2 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));

        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);
        Device slaveWithNeedProxy = createMockDevice(132, "654654");
        when(slaveWithNeedProxy.getDeviceType()).thenReturn(slaveRtuType);
        Register registerSlave1 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        when(slaveWithNeedProxy.getRegisters()).thenReturn(Arrays.asList(registerSlave1));
        Device slaveWithoutNeedProxy = createMockDevice(133, "65465415");
        DeviceType notASlaveRtuType = mock(DeviceType.class);
        Register registerSlave2 = createMockedRegister(createMockedRegisterSpec(registerType), device);
        when(slaveWithoutNeedProxy.getDeviceType()).thenReturn(notASlaveRtuType);
        when(slaveWithoutNeedProxy.getRegisters()).thenReturn(Arrays.asList(registerSlave2));
        when(this.topologyService.findPhysicalConnectedDevices(device)).thenReturn(Arrays.asList(slaveWithNeedProxy, slaveWithoutNeedProxy));

        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // asserts
        assertNotNull(offlineRtu.getAllOfflineRegisters());
        assertEquals("Should have gotten 3 registers", 3, offlineRtu.getAllOfflineRegisters().size());
    }

    @Test
    public void getRegistersForRegisterGroup() {
        final long rtuRegisterGroupId = 135143654;
        RegisterGroup rtuRegisterGroup = mock(RegisterGroup.class);
        when(rtuRegisterGroup.getId()).thenReturn(rtuRegisterGroupId);
        Device device = createMockDevice();
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getRegisterGroups()).thenReturn(Arrays.asList(rtuRegisterGroup));
        RegisterSpec registerSpec = createMockedRegisterSpec(registerType);
        Register register1 = createMockedRegister(registerSpec, device);
        when(register1.getRegisterSpec().getRegisterType().getRegisterGroups()).thenReturn(Arrays.asList(rtuRegisterGroup));
        OfflineRegister offlineRegister1 = mock(OfflineRegister.class);
        when(offlineRegister1.inGroup(rtuRegisterGroupId)).thenReturn(true);
        when(offlineRegister1.inAtLeastOneGroup(Arrays.asList(rtuRegisterGroupId))).thenReturn(true);
        when(offlineRegister1.getDeviceMRID()).thenReturn("getRegistersForRegisterGroup");
        Register register2 = createMockedRegister(registerSpec, device);
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));
        OfflineRegister offlineRegister2 = mock(OfflineRegister.class);
        when(offlineRegister2.getDeviceMRID()).thenReturn("getRegistersForRegisterGroup");

        OfflineDeviceImpl offlineRtu = spy(new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider));
        when(offlineRtu.getAllOfflineRegisters()).thenReturn(Arrays.asList(offlineRegister1, offlineRegister2));

        // asserts
        assertEquals("Should have gotten 1 registers", 1, offlineRtu.getRegistersForRegisterGroupAndMRID(Arrays.asList(rtuRegisterGroupId), "getRegistersForRegisterGroup").size());
    }

    private RegisterSpec createMockedRegisterSpec(RegisterType registerType) {
        RegisterSpec registerSpec = mock(RegisterSpec.class, withSettings().extraInterfaces(NumericalRegisterSpec.class));
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(((NumericalRegisterSpec) registerSpec).getOverflowValue()).thenReturn(Optional.empty());
        return registerSpec;
    }

    private Register createMockedRegister(RegisterSpec registerSpec, Device device) {
        Register register = mock(Register.class);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(register.getDevice()).thenReturn(device);
        return register;
    }

    @Test
    public void pendingDeviceMessagesTest() {
        Device device = createMockDevice();
        Calendar calendar = mock(Calendar.class);
        AllowedCalendar allowedCalendar = mock(AllowedCalendar.class);
        when(allowedCalendar.getCalendar()).thenReturn(Optional.of(calendar));
        when(allowedCalendar.getName()).thenReturn(CALENDAR_NAME);
        when(calendar.getName()).thenReturn(CALENDAR_NAME);
        when(device.getDeviceType().getAllowedCalendars()).thenReturn(Collections.singletonList(allowedCalendar));

        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(ClockDeviceMessage.SET_TIME.getId());
        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be zero", 0, offlineDevice.getAllPendingDeviceMessages().size());

        when(device.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(deviceMessage2));

        offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be one", 1, offlineDevice.getAllPendingDeviceMessages().size());
        assertEquals("Size of the sent list should be 0", 0, offlineDevice.getAllSentDeviceMessages().size());
    }

    @Test
    public void pendingInvalidDeviceMessagesTest() {
        Calendar calendar = mock(Calendar.class);
        when(calendar.getName()).thenReturn(CALENDAR_NAME);
        Device device = createMockDevice();
        when(device.getDeviceType().getAllowedCalendars()).thenReturn(Collections.emptyList());
        DeviceMessageSpec sendCalendarSpec = this.getDeviceMessageSpec(1);
        List<PropertySpec> propertySpecs = sendCalendarSpec.getPropertySpecs();
        DeviceMessage sendCalendar = mock(DeviceMessage.class);
        when(sendCalendar.getDeviceMessageId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND);
        when(sendCalendar.getSpecification()).thenReturn(sendCalendarSpec);

        DeviceMessageAttribute calendarNameAttribute = mock(DeviceMessageAttribute.class);
        when(calendarNameAttribute.getDeviceMessage()).thenReturn(sendCalendar);
        when(calendarNameAttribute.getName()).thenReturn(DeviceMessageAttributes.activityCalendarNameAttributeName.getDefaultFormat());
        when(calendarNameAttribute.getValue()).thenReturn("pendingInvalidDeviceMessagesTest");
        when(calendarNameAttribute.getSpecification()).thenReturn(propertySpecs.get(0));
        DeviceMessageAttribute calendarAttribute = mock(DeviceMessageAttribute.class);
        when(calendarAttribute.getDeviceMessage()).thenReturn(sendCalendar);
        when(calendarAttribute.getName()).thenReturn(DeviceMessageAttributes.activityCalendarAttributeName.getDefaultFormat());
        when(calendarAttribute.getValue()).thenReturn(calendar);
        when(calendarAttribute.getSpecification()).thenReturn(propertySpecs.get(1));
        doReturn(Arrays.asList(calendarNameAttribute, calendarAttribute)).when(sendCalendar.getAttributes());
        when(sendCalendar.getDevice()).thenReturn(device);
        when(device.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(sendCalendar));

        // Business method
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        // Asserts
        assertEquals("Size of the pending list should be zero", 0, offlineDevice.getAllPendingDeviceMessages().size());
        assertEquals("Size of the pending list should be one", 1, offlineDevice.getAllInvalidPendingDeviceMessages().size());
        assertEquals("Size of the sent list should be 0", 0, offlineDevice.getAllSentDeviceMessages().size());
    }

    @Test
    public void getAllPendingDeviceMessagesIncludingDownStreamsTest() {
        Device device = createMockDevice();
        DeviceMessage deviceMessage1 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec1 = this.getDeviceMessageSpec(ClockDeviceMessage.SET_TIME.getId());
        when(deviceMessage1.getSpecification()).thenReturn(mockedDeviceMessageSpec1);
        when(deviceMessage1.getDevice()).thenReturn(device);
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(ContactorDeviceMessage.CONTACTOR_ARM.getId());
        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        when(device.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Arrays.asList(deviceMessage1, deviceMessage2));

        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);
        DeviceProtocol slaveDeviceProtocol = mock(DeviceProtocol.class);
        Device slaveWithNeedProxy = createMockDevice(132, "654654", slaveDeviceProtocol);
        when(slaveWithNeedProxy.getDeviceType()).thenReturn(slaveRtuType);
        DeviceMessage slaveDeviceMessage1 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedSlaveDeviceMessageSpec1 = this.getDeviceMessageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE.getId());
        when(slaveDeviceMessage1.getSpecification()).thenReturn(mockedSlaveDeviceMessageSpec1);
        when(slaveDeviceMessage1.getDevice()).thenReturn(slaveWithNeedProxy);
        when(slaveWithNeedProxy.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(slaveDeviceMessage1));

        DeviceType notASlaveRtuType = mock(DeviceType.class);
        Device slaveWithoutNeedProxy = createMockDevice(133, "65465415", slaveDeviceProtocol);
        when(slaveWithoutNeedProxy.getDeviceType()).thenReturn(notASlaveRtuType);
        DeviceMessage slaveDeviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedSlaveDeviceMessageSpec2 = this.getDeviceMessageSpec(ContactorDeviceMessage.CONTACTOR_OPEN.getId());
        when(slaveDeviceMessage2.getSpecification()).thenReturn(mockedSlaveDeviceMessageSpec2);
        when(slaveDeviceMessage2.getDevice()).thenReturn(slaveWithoutNeedProxy);
        when(slaveWithoutNeedProxy.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(slaveDeviceMessage2));

        when(deviceMessageFactory.findByDeviceAndState(device, DeviceMessageStatus.PENDING)).thenReturn(Arrays.asList(deviceMessage1, deviceMessage2));
        when(deviceMessageFactory.findByDeviceAndState(slaveWithNeedProxy, DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(slaveDeviceMessage1));
        when(deviceMessageFactory.findByDeviceAndState(slaveWithoutNeedProxy, DeviceMessageStatus.PENDING)).thenReturn(Collections.singletonList(slaveDeviceMessage2));
        when(this.topologyService.findPhysicalConnectedDevices(device)).thenReturn(Arrays.asList(slaveWithNeedProxy, slaveWithoutNeedProxy));

        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be three", 3, offlineDevice.getAllPendingDeviceMessages().size());
        assertEquals("Size of the sent list should be 0", 0, offlineDevice.getAllSentDeviceMessages().size());

        List<OfflineDeviceMessage> offlineDeviceMessages = offlineDevice.getAllPendingDeviceMessages().stream().filter(offlineDeviceMessage -> offlineDeviceMessage.getDeviceId() == 132).collect(Collectors.toList());
        assertEquals("Expecting one slave device message", 1, offlineDeviceMessages.size());
        assertEquals("Expecting the serialnumber of the slave", "654654", offlineDeviceMessages.get(0).getDeviceSerialNumber());
        assertEquals("Expecting the DeviceProtocol of the slave", slaveDeviceProtocol, offlineDeviceMessages.get(0).getDeviceProtocol());
    }

    @Test
    public void sentDeviceMessagesTest() {
        Device device = createMockDevice();
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(DeviceMessageId.CLOCK_SET_TIME);
        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllSentDeviceMessages());
        assertEquals("Size of the sent list should be zero", 0, offlineDevice.getAllSentDeviceMessages().size());

        when(device.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Collections.singletonList(deviceMessage2));

        offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        assertNotNull(offlineDevice.getAllSentDeviceMessages());
        assertEquals("Size of the sent list should be one", 1, offlineDevice.getAllSentDeviceMessages().size());
        assertEquals("Size of the pending list should be 0", 0, offlineDevice.getAllPendingDeviceMessages().size());
    }

    @Test
    public void getAllSentDeviceMessagesIncludingDownStreamsTest() {
        Device device = createMockDevice();
        DeviceMessage deviceMessage1 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec1 = this.getDeviceMessageSpec(ClockDeviceMessage.SET_TIME.getId());
        when(deviceMessage1.getSpecification()).thenReturn(mockedDeviceMessageSpec1);
        when(deviceMessage1.getDevice()).thenReturn(device);
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(ContactorDeviceMessage.CONTACTOR_ARM.getId());
        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        when(device.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(deviceMessage1, deviceMessage2));

        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);
        DeviceProtocol slaveDeviceProtocol = mock(DeviceProtocol.class);
        Device slaveWithNeedProxy = createMockDevice(132, "654654", slaveDeviceProtocol);
        when(slaveWithNeedProxy.getDeviceType()).thenReturn(slaveRtuType);
        DeviceMessage slaveDeviceMessage1 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedSlaveDeviceMessageSpec1 = this.getDeviceMessageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE.getId());
        when(slaveDeviceMessage1.getSpecification()).thenReturn(mockedSlaveDeviceMessageSpec1);
        when(slaveDeviceMessage1.getDevice()).thenReturn(slaveWithNeedProxy);
        when(slaveWithNeedProxy.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Collections.singletonList(slaveDeviceMessage1));

        DeviceType notASlaveRtuType = mock(DeviceType.class);
        Device slaveWithoutNeedProxy = createMockDevice(133, "65465415", slaveDeviceProtocol);
        when(slaveWithoutNeedProxy.getDeviceType()).thenReturn(notASlaveRtuType);
        DeviceMessage slaveDeviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedSlaveDeviceMessageSpec2 = this.getDeviceMessageSpec(ContactorDeviceMessage.CONTACTOR_OPEN.getId());
        when(slaveDeviceMessage2.getSpecification()).thenReturn(mockedSlaveDeviceMessageSpec2);
        when(slaveDeviceMessage2.getDevice()).thenReturn(slaveWithoutNeedProxy);
        when(slaveWithoutNeedProxy.getMessagesByState(DeviceMessageStatus.SENT)).thenReturn(Collections.singletonList(slaveDeviceMessage2));

        when(deviceMessageFactory.findByDeviceAndState(device, DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(deviceMessage1, deviceMessage2));
        when(deviceMessageFactory.findByDeviceAndState(slaveWithNeedProxy, DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(slaveDeviceMessage1));
        when(deviceMessageFactory.findByDeviceAndState(slaveWithoutNeedProxy, DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(slaveDeviceMessage2));
        when(this.topologyService.findPhysicalConnectedDevices(device)).thenReturn(Arrays.asList(slaveWithNeedProxy, slaveWithoutNeedProxy));

        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be three", 0, offlineDevice.getAllPendingDeviceMessages().size());
        assertEquals("Size of the sent list should be 0", 3, offlineDevice.getAllSentDeviceMessages().size());

        List<OfflineDeviceMessage> offlineDeviceMessages = offlineDevice.getAllSentDeviceMessages().stream().filter(offlineDeviceMessage -> offlineDeviceMessage.getDeviceId() == 132).collect(Collectors.toList());
        assertEquals("Expecting one slave device message", 1, offlineDeviceMessages.size());
        assertEquals("Expecting the serialnumber of the slave", "654654", offlineDeviceMessages.get(0).getDeviceSerialNumber());
        assertEquals("Expecting the DeviceProtocol of the slave", slaveDeviceProtocol, offlineDeviceMessages.get(0).getDeviceProtocol());
    }

    @Test
    public void deviceProtocolPluggableClassNullTest() {
        Device
                device = createMockDevice();
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        //asserts
        assertThat(offlineDevice.getDeviceProtocolPluggableClass()).isNull();
    }

    @Test
    public void getAllSlaveDevicesWithoutSlaveCapabilitiesTest() {
        long slaveId = 664513;
        Device master = createMockDevice();
        Device slaveWithoutCapability = mock(Device.class);
        when(slaveWithoutCapability.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        when(slaveWithoutCapability.getId()).thenReturn(slaveId);
        DeviceType deviceTypeSlave = mock(DeviceType.class);
        when(slaveWithoutCapability.getDeviceType()).thenReturn(deviceTypeSlave);
        when(slaveWithoutCapability.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(this.topologyService.findPhysicalConnectedDevices(master)).thenReturn(Arrays.asList(slaveWithoutCapability));

        // business method
        OfflineDevice offlineDevice = new OfflineDeviceImpl(master, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG), this.offlineDeviceServiceProvider);

        // assert
        assertThat(offlineDevice.getAllSlaveDevices()).isNotNull();
        assertThat(offlineDevice.getAllSlaveDevices()).isNotEmpty();
        assertThat(offlineDevice.getAllSlaveDevices().get(0).getId()).isEqualTo(slaveId);
    }

    @Test
    public void getAllSlaveDevicesWithSlaveCapabilitiesTest() {
        long slaveId = 664513;
        Device master = createMockDevice();
        DeviceType deviceTypeSlave = mock(DeviceType.class);
        when(deviceTypeSlave.isLogicalSlave()).thenReturn(true);
        Device slaveWithCapability = createMockDevice();
        when(slaveWithCapability.getId()).thenReturn(slaveId);
        when(slaveWithCapability.getDeviceType()).thenReturn(deviceTypeSlave);
        when(this.topologyService.findPhysicalConnectedDevices(master)).thenReturn(Arrays.asList(slaveWithCapability));

        // business method
        OfflineDevice offlineDevice = new OfflineDeviceImpl(master, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG), this.offlineDeviceServiceProvider);

        // assert
        assertThat(offlineDevice.getAllSlaveDevices()).isNotEmpty();
        assertThat(offlineDevice.getAllSlaveDevices().get(0).getId()).isEqualTo(slaveId);
    }

    @Test
    public void propertyOfDeviceOverrulesPropertiesOfDeviceProtocolTest() {
        Device device = createMockDevice();
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, new DeviceOfflineFlags(), this.offlineDeviceServiceProvider);

        Object property = offlineDevice.getAllProperties().getProperty(DEVICE_TIMEZONE_PROPERTY);
        assertThat(property).isEqualTo(deviceTimeZone);
    }

    private DeviceMessageSpec getDeviceMessageSpec(DeviceMessageId deviceMessageId) {
        return getDeviceMessageSpec(deviceMessageId.dbValue());
    }

    private DeviceMessageSpec getDeviceMessageSpec(long id) {
        return this.deviceMessageSpecificationService.findMessageSpecById(id).orElseThrow(() -> new RuntimeException("Setup failure: could not find DeviceMessageSpec with id " + id));
    }
}