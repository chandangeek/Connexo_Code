package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.DeviceIdentifierById;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link DeviceIpAddress} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (16:11)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceIpAddressTest {

    private static final int DEVICE_ID = 97;
    private static final String IP_ADDRESS = "192.168.2.100";
    private static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";

    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private IssueService issueService;

    @Test
    public void testConstructorDoesNotThrowExceptions () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);

        // Business method
        new DeviceIpAddress(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Simply asserting that no exceptions are thrown
    }

    @Test
    public void testIsAlwaysConfiguredOnComTasks () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);
        DeviceIpAddress deviceIpAddress = new DeviceIpAddress(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);
        DataCollectionConfiguration comTask = mock(DataCollectionConfiguration.class);

        // Business method
        boolean isConfiguredIn = deviceIpAddress.isConfiguredIn(comTask);

        // Asserts
        assertThat(isConfiguredIn).isTrue();
    }

    @Test
    public void testToDeviceCommand () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);
        DeviceIpAddress deviceIpAddress = new DeviceIpAddress(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Business method
        DeviceCommand command = deviceIpAddress.toDeviceCommand(issueService, meterDataStoreCommand);

        // Asserts
        assertThat(command).isNotNull();
    }

    @Test
    public void testGetDeviceIdentifier () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);
        DeviceIpAddress deviceIpAddress = new DeviceIpAddress(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Business method
        DeviceIdentifier needsChecking = deviceIpAddress.getDeviceIdentifier();

        // Asserts
        assertThat(needsChecking).isSameAs(deviceIdentifier);
    }

    @Test
    public void testGetIpAddress () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);
        DeviceIpAddress deviceIpAddress = new DeviceIpAddress(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Business method
        String propertyValue = deviceIpAddress.getIpAddress();

        // Asserts
        assertThat(propertyValue).isEqualTo(IP_ADDRESS);
    }

    @Test
    public void testGetConnectionTypePropertyName () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);
        DeviceIpAddress deviceIpAddress = new DeviceIpAddress(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Business method
        String connectionTaskPropertyName = deviceIpAddress.getConnectionTaskPropertyName();

        // Asserts
        assertThat(connectionTaskPropertyName).isEqualTo(IP_ADDRESS_PROPERTY_NAME);
    }

}