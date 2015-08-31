package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceUserFileConfigurationInformation;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link StoreConfigurationUserFile} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (16:45)
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreConfigurationUserFileTest {

    private static final long DEVICE_ID = 97;
    private static final String FILE_EXTENSION = "txt";
    private static final byte[] CONTENTS = "Example of collected device configuration".getBytes();

    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    private DeviceService deviceService;

    @Test
    public void testExecute() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        DeviceUserFileConfigurationInformation collectedData = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);
        StoreConfigurationUserFile command = new StoreConfigurationUserFile(collectedData, null, new NoDeviceCommandServices());
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).storeConfigurationFile(any(DeviceIdentifier.class), any(DateTimeFormatter.class), anyString(), any(byte[].class));
        verify(comServerDAO, never()).findOfflineDevice(deviceIdentifier);
    }

    @Test
    public void testToString() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        DeviceUserFileConfigurationInformation collectedData = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);
        StoreConfigurationUserFile command = new StoreConfigurationUserFile(collectedData, null, new NoDeviceCommandServices());

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        assertThat(journalMessage).contains("{deviceIdentifier: id 97; file extension: txt}");
    }
}