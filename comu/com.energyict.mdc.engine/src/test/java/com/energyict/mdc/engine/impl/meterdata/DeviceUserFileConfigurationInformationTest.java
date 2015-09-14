package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommandImpl;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link DeviceUserFileConfigurationInformation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (16:48)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceUserFileConfigurationInformationTest {

    private static final long DEVICE_ID = 97;
    private static final String FILE_EXTENSION = "txt";
    private static final byte[] CONTENTS = "Example of collected device configuration".getBytes();

    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;

    @Test
    public void testConstructorDoesNotThrowExceptions() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);

        // Business method
        new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Simply asserting that no exceptions are thrown
    }

    @Test
    public void testIsNeverConfiguredOnComTasks() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);
        DataCollectionConfiguration comTask = mock(DataCollectionConfiguration.class);

        // Business method
        boolean isConfiguredIn = deviceIpAddress.isConfiguredIn(comTask);

        // Asserts
        assertThat(isConfiguredIn).isFalse();
    }

    @Test
    public void testToDeviceCommand() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Business method
        DeviceCommand command = deviceIpAddress.toDeviceCommand(new MeterDataStoreCommandImpl(serviceProvider), serviceProvider);

        // Asserts
        assertThat(command).isNotNull();
    }

    @Test
    public void testGetDeviceIdentifier() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Business method
        DeviceIdentifier needsChecking = deviceIpAddress.getDeviceIdentifier();

        // Asserts
        assertThat(needsChecking).isSameAs(deviceIdentifier);
    }

    @Test
    public void testGetFileExtension() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Business method
        String needsChecking = deviceIpAddress.getFileExtension();

        // Asserts
        assertThat(needsChecking).isEqualTo(FILE_EXTENSION);
    }

    @Test
    public void testGetContents() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Business method
        byte[] needsChecking = deviceIpAddress.getContents();

        // Asserts
        assertThat(needsChecking).isEqualTo(CONTENTS);
    }

}