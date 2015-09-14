package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.engine.impl.meterdata.NoLogBooksForDevice;
import com.energyict.mdc.engine.config.ComServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link CreateNoLogBooksForDeviceEvent} component.
 *
 * @author sva
 * @since 30/09/13 - 9:48
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateNoLogBooksForDeviceEventTest {

    private static final long DEVICE_ID = 97;

    @Mock
    private DeviceService deviceService;

    @Test
    public void testToJournalMessageDescription() throws Exception {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, this.deviceService);
        NoLogBooksForDevice noLogBooksForDevice = new NoLogBooksForDevice(deviceIdentifier);
        CreateNoLogBooksForDeviceEvent command = new CreateNoLogBooksForDeviceEvent(noLogBooksForDevice, null, new NoDeviceCommandServices());

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("{deviceIdentifier: id 97}");
    }

}