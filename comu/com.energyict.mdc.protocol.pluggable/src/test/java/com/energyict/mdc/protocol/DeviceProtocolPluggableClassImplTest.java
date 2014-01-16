package com.energyict.mdc.protocol;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.DeviceProtocolPluggableClassImpl;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the {@link DeviceProtocolPluggableClassImpl} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 11:39
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolPluggableClassImplTest {

    public static final String DEVICE_PROTOCOL_NAME = "DeviceProtocolPluggableClassName";
    public static final String MOCK_DEVICE_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol";
    public static final String MOCK_METER_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol";
    public static final String MOCK_SMART_METER_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockSmartMeterProtocol";
    public static final String MOCK_NOT_A_DEVICE_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.NotADeviceProtocol";

    private static InMemoryPersistence inMemoryPersistence;

    private ProtocolPluggableService protocolPluggableService;

    @Mock
    private DeviceProtocolService deviceProtocolService;

    @BeforeClass
    public static void initializeDatabase() {
        inMemoryPersistence = InMemoryPersistence.initializeDatabase();
    }

    @AfterClass
    public static void cleanupDatabase () throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Before
    public void getProtocolPluggableService () {
        this.protocolPluggableService = inMemoryPersistence.getProtocolPluggableService();
    }

    @After
    public void cleanUp() throws BusinessException, SQLException {
        for (DeviceProtocolPluggableClass pluggableClass : this.protocolPluggableService.findAllDeviceProtocolPluggableClasses()) {
            pluggableClass.delete();
        }
    }

    @Test
    public void newInstanceDeviceProtocolTest() throws BusinessException, SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL);

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getName()).isEqualTo(DEVICE_PROTOCOL_NAME);
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(DeviceProtocol.class);
    }

    @Test
    public void saveDeviceProtocolTest() throws BusinessException, SQLException {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL);

        // Business method
        deviceProtocolPluggableClass.save();

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getName()).isEqualTo(DEVICE_PROTOCOL_NAME);
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(DeviceProtocol.class);
    }

    @Test
    public void updateDeviceProtocolTest() throws BusinessException, SQLException {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL);
        deviceProtocolPluggableClass.save();

        String newName = "Changed-" + deviceProtocolPluggableClass.getName();
        deviceProtocolPluggableClass.setName(newName);

        // Business method
        deviceProtocolPluggableClass.save();

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getName()).isEqualTo(newName);
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(DeviceProtocol.class);
    }

    @Test
    public void newInstanceMeterProtocolTest() throws BusinessException, SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_METER_PROTOCOL);

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getName()).isEqualTo(DEVICE_PROTOCOL_NAME);
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(MeterProtocolAdapter.class);
    }

    @Test
    public void newInstanceSmartMeterProtocolTest() throws BusinessException, SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_SMART_METER_PROTOCOL);

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getName()).isEqualTo(DEVICE_PROTOCOL_NAME);
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(SmartMeterProtocolAdapter.class);
    }

    @Test(expected = BusinessException.class)
    public void newInstanceNotADeviceProtocolTest() throws BusinessException, SQLException {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_NOT_A_DEVICE_PROTOCOL);

        // Business method
        deviceProtocolPluggableClass.getDeviceProtocol();
    }

}