package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.engine.impl.meterdata.DeviceIpAddress;
import com.energyict.mdc.engine.config.ComServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author sva
 * @since 30/09/13 - 9:04
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateDeviceIpAddressTest {


    private static final long DEVICE_ID = 1;
    private static final String IP_ADDRESS = "10.0.1.50:4059";
    private final String connectionTaskPropertyName = "connectionTaskPropertyName";
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;

    @Test
    public void testToJournalMessageDescription() throws Exception {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        final DeviceIpAddress deviceIpAddress = new DeviceIpAddress(deviceIdentifier, IP_ADDRESS, connectionTaskPropertyName);
        UpdateDeviceIpAddress command = new UpdateDeviceIpAddress(deviceIpAddress, null, serviceProvider);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("{deviceIdentifier: id 1; IP address: 10.0.1.50:4059}");
    }

}