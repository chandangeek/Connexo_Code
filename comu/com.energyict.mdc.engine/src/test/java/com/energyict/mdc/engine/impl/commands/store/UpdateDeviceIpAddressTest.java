package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.meterdata.DeviceIpAddress;
import com.energyict.mdc.engine.impl.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
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


    private final long DEVICE_ID = 1;
    private final String ipAddress = "10.0.1.50:4059";
    private final String connectionTaskPropertyName = "connectionTaskPropertyName";
    @Mock
    private DeviceDataService deviceDataService;

    @Test
    public void testToJournalMessageDescription() throws Exception {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);
        final DeviceIpAddress deviceIpAddress = new DeviceIpAddress(deviceIdentifier, ipAddress, connectionTaskPropertyName);
        UpdateDeviceIpAddress command = new UpdateDeviceIpAddress(deviceIpAddress);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(UpdateDeviceIpAddress.class.getSimpleName() + " {deviceIdentifier: id 1; IP address: 10.0.1.50:4059}");
    }
}
