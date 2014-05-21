package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.DeviceMessageFactory;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseDevice;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
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

    private static final String rtu_prop1 = "RTU_Prop1";
    private static final String rtu_prop2 = "RTU_Prop2";
    private static final String rtu_prop3 = "RTU_Prop3";
    private static final String DEVICE_TIMEZONE_PROPERTY = "deviceTimeZone";
    private static final String rtu_propValue1 = "RTU_PropValue1";
    private static final String rtu_propValue2 = "RTU_PropValue2";
    private static final String rtu_propValue3 = "RTU_PropValue3";

    private static final String meterSerialNumber = "MeterSerialNumber";
    private static final int DEVICE_ID = 93;
    private static final TimeZone deviceProtocolPluggableClassTimeZone = TimeZone.getTimeZone("GMT");
    private static final TimeZone deviceTimeZone = TimeZone.getTimeZone("GMT+05");

    private static final String DEVICE_PROTOCOL_JAVA_CLASS = "com.energyict.mdc.protocol.mocks.MockDeviceProtocol";
    private static final String DEVICE_PROTOCOL_PLUGGABLE_CLASS_NAME = "DeviceProtocolPluggableClassName";

    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private DeviceMessageFactory deviceMessageFactory;

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
        properties.setProperty(rtu_prop1, rtu_propValue1);
        properties.setProperty(rtu_prop2, rtu_propValue2);
        properties.setProperty(rtu_prop3, rtu_propValue3);
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
    }
//
//    @Before
//    public void initializeMocksAndFactories() {
//        deviceMessageFactory = mock(DeviceMessageFactory.class);
//        deviceCacheFactory = mock(DeviceCacheFactory.class);
//        mdcInterface = mock(MdcInterface.class);
//        MdcInterfaceProvider.instance.set(new MdcInterfaceProvider() {
//            @Override
//            public MdcInterface getMdcInterface() {
//                return mdcInterface;
//            }
//        });
//        DeviceCacheFactoryProvider.instance.set(new DeviceCacheFactoryProvider() {
//            @Override
//            public DeviceCacheFactory getDeviceCacheFactory() {
//                return deviceCacheFactory;
//            }
//        });
//
//        when(mdcInterface.getManager()).thenReturn(this.manager);
//        when(this.manager.getDeviceMessageFactory()).thenReturn(deviceMessageFactory);
//    }
//
//    @After
//    public void tearDown() {
//        MdcInterfaceProvider.instance.set(null);
//        DeviceCacheFactoryProvider.instance.set(null);
//    }

    private int getTotalSizeOfProperties() {
        return getDeviceProperties().size() + getDeviceProtocolProperties().size() + getDeviceProtocolPluggableClassProperties().size();
    }

    @Test
    public void goOfflineTest() {
        Device device = createMockDevice();
        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything);
        assertEquals("The SerialNumber should match", meterSerialNumber, offlineRtu.getSerialNumber());
        assertEquals("The ID should match", DEVICE_ID, offlineRtu.getId());
        assertNotNull("The DeviceProtocol PluggableClass should not be null", offlineRtu.getDeviceProtocolPluggableClass());

    }

    @Test
    public void propertiesTest() {
        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(createMockDevice(), DeviceOffline.needsEverything);
        offlineRtu.addProperties(getDeviceProperties(), getDeviceProtocolProperties());
        assertEquals("Size should be equal to seven", getTotalSizeOfProperties(), offlineRtu.getAllProperties().localSize());
        assertEquals(cp_propValue1, offlineRtu.getAllProperties().getProperty(cp_prop1));
        assertEquals(cp_propValue2, offlineRtu.getAllProperties().getProperty(cp_prop2));
        assertEquals(cp_propValue3, offlineRtu.getAllProperties().getProperty(cp_prop3));
        assertEquals(rtu_propValue1, offlineRtu.getAllProperties().getProperty(rtu_prop1));
        assertEquals(rtu_propValue2, offlineRtu.getAllProperties().getProperty(rtu_prop2));
        assertEquals(rtu_propValue3, offlineRtu.getAllProperties().getProperty(rtu_prop3));
        assertEquals(rtu_propValue3, offlineRtu.getAllProperties().getProperty(rtu_prop3));
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
        OfflineDevice offlineSlaveFromSlave1 = new OfflineDeviceImpl(slaveFromSlave1, DeviceOffline.needsEverything);
        when(slave1.getPhysicalConnectedDevices()).thenReturn(Arrays.<BaseDevice<Channel, LoadProfile, Register>>asList(slaveFromSlave1));
        OfflineDevice offlineSlave1 = new OfflineDeviceImpl(slave1, DeviceOffline.needsEverything);

        Device slave2 = createMockDevice(456, "slave2");
        when(slave2.getDeviceType()).thenReturn(slaveRtuType);
        OfflineDevice offlineSlave2 = new OfflineDeviceImpl(slave2, DeviceOffline.needsEverything);
        when(rtu.getPhysicalConnectedDevices()).thenReturn(Arrays.<BaseDevice<Channel, LoadProfile, Register>>asList(slave1, slave2));

        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(rtu, DeviceOffline.needsEverything);

        //asserts
        assertNotNull(offlineRtu.getAllSlaveDevices());
        assertEquals("Expected three slave devices", 3, offlineRtu.getAllSlaveDevices().size());
        assertThat(offlineRtu.getAllSlaveDevices().contains(offlineSlave1)).isTrue();
        assertThat(offlineRtu.getAllSlaveDevices().contains(offlineSlave2)).isTrue();
        assertThat(offlineRtu.getAllSlaveDevices().contains(offlineSlaveFromSlave1)).isTrue();
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
        OfflineDevice offlineSlaveFromSlave1 = new OfflineDeviceImpl(slaveFromSlave1, DeviceOffline.needsEverything);
        OfflineDevice offlineSlave1 = new OfflineDeviceImpl(slave1, DeviceOffline.needsEverything);
        when(slave1.getPhysicalConnectedDevices()).thenReturn(Arrays.<BaseDevice<Channel, LoadProfile, Register>>asList(slaveFromSlave1));

        Device slave2 = createMockDevice(456, "slave2");
        when(slave2.getDeviceType()).thenReturn(slaveRtuType);
        OfflineDevice offlineSlave2 = new OfflineDeviceImpl(slave2, DeviceOffline.needsEverything);
        when(device.getPhysicalConnectedDevices()).thenReturn(Arrays.<BaseDevice<Channel, LoadProfile, Register>>asList(slave1, slave2));

        ObisCode obisCode1 = ObisCode.fromString("1.0.99.1.0.255");
        ObisCode obisCode2 = ObisCode.fromString("1.0.99.2.0.255");
        ObisCode obisCode3 = ObisCode.fromString("1.0.99.3.0.255");
        ObisCode obisCode4 = ObisCode.fromString("1.0.99.4.0.255");
        ObisCode obisCodex = ObisCode.fromString("1.x.24.1.0.255");

        LoadProfile loadProfile1 = getNewMockedLoadProfile(obisCode1);
        LoadProfile loadProfile2 = getNewMockedLoadProfile(obisCode2);
        LoadProfile loadProfile3 = getNewMockedLoadProfile(obisCode3);
        LoadProfile loadProfile4 = getNewMockedLoadProfile(obisCode4);
        LoadProfile loadProfilex = getNewMockedLoadProfile(obisCodex);

        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(slave1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile3, loadProfilex));
        when(slave2.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile4, loadProfile2));
        when(slaveFromSlave1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfilex));

        OfflineDeviceImpl offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything);

        // Asserts
        assertNotNull(offlineDevice.getMasterOfflineLoadProfiles());
        assertEquals("Expected two loadProfiles on the master", 2, offlineDevice.getMasterOfflineLoadProfiles().size());
        assertNotNull(offlineDevice.getAllOfflineLoadProfiles());
        assertEquals("Expected 6 loadProfiles in total", 6, offlineDevice.getAllOfflineLoadProfiles().size());
    }

    @Test
    public void getAllRegistersTest() {
        Device device = createMockDevice();
        Register register1 = createMockedRegister();
        Register register2 = createMockedRegister();
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));

        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything);

        // asserts
        assertNotNull(offlineRtu.getAllRegisters());
        assertEquals("Should have gotten 2 registers", 2, offlineRtu.getAllRegisters().size());
    }

    @Test
    public void getAllRegistersIncludingSlavesTest() {

        OfflineDevice mockOfflineDevice = mock(OfflineDevice.class);

        Device device = createMockDevice();
        Register register1 = createMockedRegister();
        Register register2 = createMockedRegister();
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));

        DeviceType slaveRtuType = mock(DeviceType.class);
        when(slaveRtuType.isLogicalSlave()).thenReturn(true);
        Device slaveWithNeedProxy = createMockDevice(132, "654654");
        when(slaveWithNeedProxy.getDeviceType()).thenReturn(slaveRtuType);
        Register registerSlave1 = createMockedRegister();
        when(slaveWithNeedProxy.getRegisters()).thenReturn(Arrays.asList(registerSlave1));
        Device slaveWithoutNeedProxy = createMockDevice(133, "65465415");
        DeviceType notASlaveRtuType = mock(DeviceType.class);
        Register registerSlave2 = createMockedRegister();
        when(slaveWithoutNeedProxy.getDeviceType()).thenReturn(notASlaveRtuType);
        when(slaveWithoutNeedProxy.getRegisters()).thenReturn(Arrays.asList(registerSlave2));
        when(device.getPhysicalConnectedDevices()).thenReturn(Arrays.<BaseDevice<Channel, LoadProfile, Register>>asList(slaveWithNeedProxy, slaveWithoutNeedProxy));

        OfflineDeviceImpl offlineRtu = new OfflineDeviceImpl(device, DeviceOffline.needsEverything);

        // asserts
        assertNotNull(offlineRtu.getAllRegisters());
        assertEquals("Should have gotten 3 registers", 3, offlineRtu.getAllRegisters().size());
    }

    @Test
    public void getRegistersForRegisterGroup() {
        final long rtuRegisterGroupId = 135143654;
        RegisterGroup rtuRegisterGroup = mock(RegisterGroup.class);
        Device device = createMockDevice();
        RegisterMapping registerMapping = mock(RegisterMapping.class);
        when(registerMapping.getRegisterGroup()).thenReturn(rtuRegisterGroup);
        RegisterSpec registerSpec = createMockedRegisterSpec(registerMapping);
        Register register1 = createMockedRegister(registerSpec);
        when(register1.getRegisterSpec().getRegisterMapping().getRegisterGroup()).thenReturn(rtuRegisterGroup);
        OfflineRegister offlineRegister1 = mock(OfflineRegister.class);
        when(offlineRegister1.getRegisterGroupId()).thenReturn(rtuRegisterGroupId);
        Register register2 = createMockedRegister();
        when(device.getRegisters()).thenReturn(Arrays.asList(register1, register2));
        OfflineRegister offlineRegister2 = mock(OfflineRegister.class);

        OfflineDeviceImpl offlineRtu = spy(new OfflineDeviceImpl(device, DeviceOffline.needsEverything));
        when(offlineRtu.getAllRegisters()).thenReturn(Arrays.asList(offlineRegister1, offlineRegister2));

        // asserts
        assertNotNull(offlineRtu.getRegistersForRegisterGroup(Arrays.asList(((int)rtuRegisterGroup.getId()))));
        assertEquals("Should have gotten 1 registers", 1, offlineRtu.getRegistersForRegisterGroup(Arrays.asList((int)rtuRegisterGroup.getId())).size());
    }

    private RegisterSpec createMockedRegisterSpec() {
        return mock(RegisterSpec.class);
    }

    private RegisterSpec createMockedRegisterSpec(RegisterMapping registerMapping) {
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        when(registerSpec.getRegisterMapping()).thenReturn(registerMapping);
        return registerSpec;
    }

    private Register createMockedRegister() {
        return mock(Register.class);
    }

    private Register createMockedRegister(RegisterSpec registerSpec) {
        Register register = mock(Register.class);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
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
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything);

        assertNotNull(offlineDevice.getAllPendingDeviceMessages());
        assertEquals("Size of the pending list should be zero", 0, offlineDevice.getAllPendingDeviceMessages().size());

        when(deviceMessageFactory.findByDeviceAndState(device, DeviceMessageStatus.PENDING)).thenReturn(Arrays.asList(deviceMessage2));

        offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything);
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
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything);

        assertNotNull(offlineDevice.getAllSentDeviceMessages());
        assertEquals("Size of the sent list should be zero", 0, offlineDevice.getAllSentDeviceMessages().size());

        when(deviceMessageFactory.findByDeviceAndState(device, DeviceMessageStatus.SENT)).thenReturn(Arrays.asList(deviceMessage2));

        offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything);
        assertNotNull(offlineDevice.getAllSentDeviceMessages());
        assertEquals("Size of the sent list should be one", 1, offlineDevice.getAllSentDeviceMessages().size());
        assertEquals("Size of the pending list should be 0", 0, offlineDevice.getAllPendingDeviceMessages().size());
    }

    @Test
    public void deviceProtocolPluggableClassNullTest() {
        Device
                device = createMockDevice();
        when(device.getDeviceProtocolPluggableClass()).thenReturn(null);
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, DeviceOffline.needsEverything);

        //asserts
        assertThat(offlineDevice.getDeviceProtocolPluggableClass()).isNull();
    }

    @Test
    public void getAllSlaveDevicesWithoutSlaveCapabilitiesTest() {
        Device master = createMockDevice();
        Device slaveWithoutCapability = mock(Device.class);
        DeviceType deviceTypeSlave = mock(DeviceType.class);
        OfflineDevice offlineSlaveDevice = mock(OfflineDevice.class);
        when(slaveWithoutCapability.getDeviceType()).thenReturn(deviceTypeSlave);
        when(master.getPhysicalConnectedDevices()).thenReturn(Arrays.<BaseDevice<Channel, LoadProfile, Register>>asList(slaveWithoutCapability));

        // business method
        OfflineDevice offlineDevice = new OfflineDeviceImpl(master, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG));

        // assert
        assertThat(offlineDevice.getAllSlaveDevices()).isNotNull();
        assertThat(offlineDevice.getAllSlaveDevices()).isNotEmpty();
        assertThat(offlineDevice.getAllSlaveDevices()).containsOnly(offlineSlaveDevice);
    }

    @Test
    public void getAllSlaveDevicesWithSlaveCapabilitiesTest() {
        OfflineDevice offlineSlaveDevice = mock(OfflineDevice.class);
        when(offlineSlaveDevice.getAllSlaveDevices()).thenReturn(Collections.<OfflineDevice>emptyList());
        Device master = createMockDevice();
        DeviceType deviceTypeSlave = mock(DeviceType.class);
        when(deviceTypeSlave.isLogicalSlave()).thenReturn(true);
        Device slaveWithCapability = createMockDevice();
        when(slaveWithCapability.getDeviceType()).thenReturn(deviceTypeSlave);
        when(master.getPhysicalConnectedDevices()).thenReturn(Arrays.<BaseDevice<Channel, LoadProfile, Register>>asList(slaveWithCapability));

        // business method
        OfflineDevice offlineDevice = new OfflineDeviceImpl(master, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG));

        // assert
        assertThat(offlineDevice.getAllSlaveDevices()).isNotEmpty();
        assertThat(offlineDevice.getAllSlaveDevices()).containsOnly(offlineSlaveDevice);
    }

    @Test
    public void propertyOfDeviceOverrulesPropertiesOfDeviceProtocolTest() {
        Device
                device = createMockDevice();
        OfflineDevice offlineDevice = new OfflineDeviceImpl(device, new DeviceOfflineFlags());

        Object property = offlineDevice.getAllProperties().getProperty(DEVICE_TIMEZONE_PROPERTY);
        assertThat(property).isEqualTo(deviceTimeZone);
    }

    private static LoadProfile getNewMockedLoadProfile(final ObisCode obisCode) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile.getLoadProfileTypeObisCode()).thenReturn(obisCode);
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
