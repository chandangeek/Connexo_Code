package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.mdc.engine.impl.meterdata.DeviceUserFileConfigurationInformation;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierById;
import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link DeviceUserFileConfigurationInformation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (16:48)
 */
public class DeviceUserFileConfigurationInformationTest {

    private static final int DEVICE_ID = 97;
    private static final String FILE_EXTENSION = "txt";
    private static final byte[] CONTENTS = "Example of collected device configuration".getBytes();

    @Test
    public void testConstructorDoesNotThrowExceptions () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);

        // Business method
        new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Simply asserting that no exceptions are thrown
    }

    @Test
    public void testIsNeverConfiguredOnComTasks () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);
        DataCollectionConfiguration comTask = mock(DataCollectionConfiguration.class);

        // Business method
        boolean isConfiguredIn = deviceIpAddress.isConfiguredIn(comTask);

        // Asserts
        assertThat(isConfiguredIn).isFalse();
    }

    @Test
    public void testToDeviceCommand () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Business method
        DeviceCommand command = deviceIpAddress.toDeviceCommand(issueService);

        // Asserts
        assertThat(command).isNotNull();
    }

    @Test
    public void testGetDeviceIdentifier () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Business method
        DeviceIdentifier needsChecking = deviceIpAddress.getDeviceIdentifier();

        // Asserts
        assertThat(needsChecking).isSameAs(deviceIdentifier);
    }

    @Test
    public void testGetFileExtension () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Business method
        String needsChecking = deviceIpAddress.getFileExtension();

        // Asserts
        assertThat(needsChecking).isEqualTo(FILE_EXTENSION);
    }

    @Test
    public void testGetContents () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceUserFileConfigurationInformation deviceIpAddress = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);

        // Business method
        byte[] needsChecking = deviceIpAddress.getContents();

        // Asserts
        assertThat(needsChecking).isEqualTo(CONTENTS);
    }

}