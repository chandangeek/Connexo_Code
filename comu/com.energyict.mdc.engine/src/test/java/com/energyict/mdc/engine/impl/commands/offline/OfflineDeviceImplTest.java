package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.DeviceMessageFactory;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategoryPrimaryKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.google.common.base.Optional;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


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
    private DeviceDataService deviceDataService;
    @Mock
    private DeviceMessageFactory deviceMessageFactory;
    @Mock
    private OfflineDeviceImpl.ServiceProvider offlineDeviceServiceProvider;

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
        Environment mockedEnvironment = mock(Environment.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(mockedEnvironment.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getModulesImplementing(DeviceMessageFactory.class)).thenReturn(Arrays.asList(deviceMessageFactory));
        Environment.DEFAULT.set(mockedEnvironment);
        when(this.offlineDeviceServiceProvider.findProtocolCacheByDevice(any(Device.class))).thenReturn(Optional.<DeviceCache>absent());
    }

    private int getTotalSizeOfProperties() {
        return getDeviceProperties().size() + getDeviceProtocolProperties().size() + getDeviceProtocolPluggableClassProperties().size();
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
//        OfflineDevice offlineSlaveFromSlave1 = new OfflineDeviceImpl(slaveFromSlave1, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        when(slave1.getPhysicalConnectedDevices()).thenReturn(Arrays.asList(slaveFromSlave1));
//        OfflineDevice offlineSlave1 = new OfflineDeviceImpl(slave1, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        Device slave2 = createMockDevice(456, "slave2");
        when(slave2.getDeviceType()).thenReturn(slaveRtuType);
//        OfflineDevice offlineSlave2 = new OfflineDeviceImpl(slave2, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        when(rtu.getPhysicalConnectedDevices()).thenReturn(Arrays.asList(slave1, slave2));

        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(rtu, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);

        //asserts
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
        when(slave1.getPhysicalConnectedDevices()).thenReturn(Arrays.asList(slaveFromSlave1));

        Device slave2 = createMockDevice(456, "slave2");
        when(slave2.getDeviceType()).thenReturn(slaveRtuType);
        OfflineDevice offlineSlave2 = new OfflineDeviceImpl(slave2, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider);
        when(device.getPhysicalConnectedDevices()).thenReturn(Arrays.asList(slave1, slave2));

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
        assertEquals("Expected 6 loadProfiles in total", 6, offlineDevice.getAllOfflineLoadProfiles().size());
    }

    @Test
    public void getAllRegistersTest() {
        Device device = createMockDevice();
        Register register1 = createMockedRegister(createMockedRegisterSpec(), device);
        Register register2 = createMockedRegister(createMockedRegisterSpec(), device);
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
        Register register1 = createMockedRegister(createMockedRegisterSpec(), device);
        Register register2 = createMockedRegister(createMockedRegisterSpec(), device);
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));

        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);
        Device slaveWithNeedProxy = createMockDevice(132, "654654");
        when(slaveWithNeedProxy.getDeviceType()).thenReturn(slaveRtuType);
        Register registerSlave1 = createMockedRegister(createMockedRegisterSpec(), device);
        when(slaveWithNeedProxy.getRegisters()).thenReturn(Arrays.asList(registerSlave1));
        Device slaveWithoutNeedProxy = createMockDevice(133, "65465415");
        DeviceType notASlaveRtuType = mock(DeviceType.class);
        Register registerSlave2 = createMockedRegister(createMockedRegisterSpec(), device);
        when(slaveWithoutNeedProxy.getDeviceType()).thenReturn(notASlaveRtuType);
        when(slaveWithoutNeedProxy.getRegisters()).thenReturn(Arrays.asList(registerSlave2));
        when(device.getPhysicalConnectedDevices()).thenReturn(Arrays.asList(slaveWithNeedProxy, slaveWithoutNeedProxy));

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
        RegisterMapping registerMapping = mock(RegisterMapping.class);
        when(registerMapping.getRegisterGroups()).thenReturn(Arrays.asList(rtuRegisterGroup));
        RegisterSpec registerSpec = createMockedRegisterSpec(registerMapping);
        Register register1 = createMockedRegister(registerSpec, device);
        when(register1.getRegisterSpec().getRegisterMapping().getRegisterGroups()).thenReturn(Arrays.asList(rtuRegisterGroup));
        OfflineRegister offlineRegister1 = mock(OfflineRegister.class);
        when(offlineRegister1.inGroup(rtuRegisterGroupId)).thenReturn(true);
        when(offlineRegister1.inAtLeastOneGroup(Arrays.asList(rtuRegisterGroupId))).thenReturn(true);
        Register register2 = createMockedRegister(registerSpec, device);
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));
        OfflineRegister offlineRegister2 = mock(OfflineRegister.class);

        OfflineDeviceImpl offlineRtu = spy(new OfflineDeviceImpl(device, DeviceOffline.needsEverything, this.offlineDeviceServiceProvider));
        when(offlineRtu.getAllRegisters()).thenReturn(Arrays.asList(offlineRegister1, offlineRegister2));

        // asserts
        assertEquals("Should have gotten 1 registers", 1, offlineRtu.getRegistersForRegisterGroup(Arrays.asList(rtuRegisterGroupId)).size());
    }

    private RegisterSpec createMockedRegisterSpec() {
        return mock(RegisterSpec.class, RETURNS_DEEP_STUBS);
    }

    private RegisterSpec createMockedRegisterSpec(RegisterMapping registerMapping) {
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        when(registerSpec.getRegisterMapping()).thenReturn(registerMapping);
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
        Device
                device = createMockDevice();

        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS;
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

    @Test
    public void sentDeviceMessagesTest() {
        Device
                device = createMockDevice();
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessageSpec mockedDeviceMessageSpec2 = DeviceMessageTestSpec.TEST_SPEC_WITH_SIMPLE_SPECS;
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
        when(master.getPhysicalConnectedDevices()).thenReturn(Arrays.asList(slaveWithoutCapability));

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
        when(master.getPhysicalConnectedDevices()).thenReturn(Arrays.asList(slaveWithCapability));

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
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile.getLoadProfileTypeObisCode()).thenReturn(obisCode);
        when(loadProfile.getDevice()).thenReturn(device);
        return loadProfile;
    }

    /**
     * Test enum for DeviceMessageCategories
     * <p/>
     * Copyrights EnergyICT
     * Date: 8/02/13
     * Time: 15:30
     */
    public enum DeviceMessageTestCategories implements DeviceMessageCategory {

        FIRST_TEST_CATEGORY {
            @Override
            public List<DeviceMessageSpec> getMessageSpecifications() {
                return Arrays.<DeviceMessageSpec>asList(DeviceMessageTestSpec.values());

            }
        };

        @Override
        public String getName() {
            return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
        }

        /**
         * Gets the resource key that determines the name
         * of this category to the user's language settings.
         *
         * @return The resource key
         */
        private String getNameResourceKey() {
            return DeviceMessageTestCategories.class.getSimpleName() + "." + this.toString();
        }

        @Override
        public String getDescription() {
            return UserEnvironment.getDefault().getTranslation(this.getDescriptionResourceKey());
        }

        /**
         * Gets the resource key that determines the description
         * of this category to the user's language settings.
         *
         * @return The resource key
         */
        private String getDescriptionResourceKey() {
            return this.getNameResourceKey() + ".description";
        }

        @Override
        public int getId() {
            return this.ordinal();
        }

        @Override
        public abstract List<DeviceMessageSpec> getMessageSpecifications();

        @Override
        public DeviceMessageCategoryPrimaryKey getPrimaryKey() {
            return new DeviceMessageCategoryPrimaryKey(this, name());
        }
    }

    /**
     * Test enum implementing DeviceMessageSpec
     * <p/>
     * Copyrights EnergyICT
     * Date: 8/02/13
     * Time: 15:16
     */
    public enum DeviceMessageTestSpec implements DeviceMessageSpec {

        TEST_SPEC_WITH_SIMPLE_SPECS(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec("testMessageSpec.simpleBigDecimal"),
                RequiredPropertySpecFactory.newInstance().stringPropertySpec("testMessageSpec.simpleString")),
        TEST_SPEC_WITHOUT_SPECS;

        private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.FIRST_TEST_CATEGORY;

        private List<PropertySpec> deviceMessagePropertySpecs;

        DeviceMessageTestSpec(PropertySpec... deviceMessagePropertySpecs) {
            this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
        }

        @Override
        public DeviceMessageCategory getCategory() {
            return activityCalendarCategory;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return deviceMessagePropertySpecs;
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            for (PropertySpec securityProperty : getPropertySpecs()) {
                if (securityProperty.getName().equals(name)) {
                    return securityProperty;
                }
            }
            return null;
        }

        @Override
        public DeviceMessageSpecPrimaryKey getPrimaryKey() {
            return new DeviceMessageSpecPrimaryKey(this, name());
        }
    }
}
