package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
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
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.impl.device.messages.ClockDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageSpecificationServiceImpl;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
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

    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceMessageFactory deviceMessageFactory;
    @Mock
    private TopologyService topologyService;
    @Mock
    private OfflineDeviceImpl.ServiceProvider offlineDeviceServiceProvider;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private OrmService ormService;
    @Mock
    DataModel dataModel;

    private DeviceMessageSpecificationService deviceMessageSpecificationService;

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
        DeviceType deviceType = mock(DeviceType.class);
        Device device = mock(Device.class);
        when(device.getDeviceType()).thenReturn(deviceType);
//        when(device.getProperties()).thenReturn(getDeviceProperties());
        when(device.getDeviceProtocolProperties()).thenReturn(getDeviceProtocolProperties());
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = createMockDeviceProtocolPluggableClass();
        when(device.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
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
        when(this.offlineDeviceServiceProvider.topologyService()).thenReturn(this.topologyService);
        when(this.topologyService.getPhysicalGateway(any(Device.class))).thenReturn(Optional.empty());
        when(this.topologyService.getPhysicalGateway(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        when(this.topologyService.findPhysicalConnectedDevices(any(Device.class))).thenReturn(Collections.<Device>emptyList());
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
    }

    private int getTotalSizeOfProperties() {
        return getDeviceProperties().size() + getDeviceProtocolProperties().size() + getDeviceProtocolPluggableClassProperties().size();
    }

    @Before
    public void setupDeviceMessageService() {
        when(this.thesaurus.getString(anyString(), anyString())).thenReturn("Translation not supported in unit testing");
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        PropertySpecService propertySpecService = new PropertySpecServiceImpl(new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(), dataVaultService, ormService);
        this.deviceMessageSpecificationService = new DeviceMessageSpecificationServiceImpl(propertySpecService, this.nlsService);
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
        offlineRtu.addProperties(getDeviceProperties(), getDeviceProtocolProperties());
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
//        assertThat(offlineRtu.getAllSlaveDevices().contains(offlineSlave1)).isTrue();
//        assertThat(offlineRtu.getAllSlaveDevices().contains(offlineSlave2)).isTrue();
//        assertThat(offlineRtu.getAllSlaveDevices().contains(offlineSlaveFromSlave1)).isTrue();
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
        assertNotNull(offlineRtu.getAllRegisters());
        assertEquals("Should have gotten 2 registers", 2, offlineRtu.getAllRegisters().size());
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
        assertNotNull(offlineRtu.getAllRegisters());
        assertEquals("Should have gotten 3 registers", 3, offlineRtu.getAllRegisters().size());
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
        when(offlineRtu.getAllRegisters()).thenReturn(Arrays.asList(offlineRegister1, offlineRegister2));

        // asserts
        assertEquals("Should have gotten 1 registers", 1, offlineRtu.getRegistersForRegisterGroupAndMRID(Arrays.asList(rtuRegisterGroupId), "getRegistersForRegisterGroup").size());
    }

    private RegisterSpec createMockedRegisterSpec(RegisterType registerType) {
        RegisterSpec registerSpec = mock(RegisterSpec.class, withSettings().extraInterfaces(NumericalRegisterSpec.class));
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        return registerSpec;
    }

    private Register createMockedRegister(RegisterSpec registerSpec, Device device) {
        Register register = mock(Register.class);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(register.getDevice()).thenReturn(device);
        return register;
    }

    @Ignore // reenable when messages are properly implemented
    @Test
    public void pendingDeviceMessagesTest() {
        Device device = createMockDevice();

        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(ClockDeviceMessage.SET_TIME.getId());
        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be zero", 0, offlineDevice.getAllPendingDeviceMessages().size());

        when(deviceMessageFactory.findByDeviceAndState(device, DeviceMessageStatus.PENDING)).thenReturn(Arrays.asList(deviceMessage2));

        offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be one", 1, offlineDevice.getAllPendingDeviceMessages().size());
        assertEquals("Size of the sent list should be 0", 0, offlineDevice.getAllSentDeviceMessages().size());
    }

    @Ignore // reenable when messages are properly implemented
    @Test
    public void sentDeviceMessagesTest() {
        Device
                device = createMockDevice();
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = this.getDeviceMessageSpec(DeviceMessageId.CLOCK_SET_TIME);
        when(deviceMessage2.getSpecification()).thenReturn(mockedDeviceMessageSpec2);
        when(deviceMessage2.getDevice()).thenReturn(device);
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        assertNotNull(offlineDevice.getAllSentDeviceMessages());
        assertEquals("Size of the sent list should be zero", 0, offlineDevice.getAllSentDeviceMessages().size());

        when(deviceMessageFactory.findByDeviceAndState(device, DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(deviceMessage2));

        offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        assertNotNull(offlineDevice.getAllSentDeviceMessages());
        assertEquals("Size of the sent list should be one", 1, offlineDevice.getAllSentDeviceMessages().size());
        assertEquals("Size of the pending list should be 0", 0, offlineDevice.getAllPendingDeviceMessages().size());
    }

    @Test
    public void deviceProtocolPluggableClassNullTest() {
        Device
                device = createMockDevice();
        when(device.getDeviceProtocolPluggableClass()).thenReturn(null);
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        //asserts
        assertThat(offlineDevice.getDeviceProtocolPluggableClass()).isNull();
    }

    @Test
    public void getAllSlaveDevicesWithoutSlaveCapabilitiesTest() {
        long slaveId = 664513;
        Device master = createMockDevice();
        Device slaveWithoutCapability = mock(Device.class);
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

    private static LoadProfile getNewMockedLoadProfile(final ObisCode obisCode, Device device) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile.getLoadProfileTypeObisCode()).thenReturn(obisCode);
        when(loadProfile.getDevice()).thenReturn(device);
        return loadProfile;
    }

    private DeviceMessageSpec getDeviceMessageSpec(DeviceMessageId deviceMessageId) {
        return this.deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue()).orElseThrow(() -> new RuntimeException("Setup failure: could not find DeviceMessageSpec with id " + deviceMessageId));
    }

}
