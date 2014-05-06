package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.meterdata.NoLogBooksForDevice;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author sva
 * @since 30/09/13 - 9:48
 */
public class CreateNoLogBooksForDeviceEventTest {

    private final int DEVICE_ID = 1;

    @Test
    public void testToJournalMessageDescription() throws Exception {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        NoLogBooksForDevice noLogBooksForDevice = new NoLogBooksForDevice(deviceIdentifier);
        CreateNoLogBooksForDeviceEvent command = new CreateNoLogBooksForDeviceEvent(noLogBooksForDevice, issueService);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CreateNoLogBooksForDeviceEvent.class.getSimpleName() + " {deviceIdentifier: id 1}");
    }
}
