package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.meterdata.DeviceIpAddress;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author sva
 * @since 30/09/13 - 9:04
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateDeviceIpAddressTest {


    private final int DEVICE_ID = 1;
    private final String ipAddress = "10.0.1.50:4059";
    private final String connectionTaskPropertyName = "connectionTaskPropertyName";

    @Test
    public void testToJournalMessageDescription() throws Exception {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        final DeviceIpAddress deviceIpAddress = new DeviceIpAddress(deviceIdentifier, ipAddress, connectionTaskPropertyName);
        UpdateDeviceIpAddress command = new UpdateDeviceIpAddress(deviceIpAddress, issueService, clock);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(UpdateDeviceIpAddress.class.getSimpleName() + " {deviceIdentifier: id 1; IP address: 10.0.1.50:4059}");
    }
}
