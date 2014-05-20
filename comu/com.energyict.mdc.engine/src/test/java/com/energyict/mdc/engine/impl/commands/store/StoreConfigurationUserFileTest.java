package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceUserFileConfigurationInformation;
import com.energyict.mdc.engine.impl.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.engine.model.ComServer;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.DateFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
    private DeviceDataService deviceDataService;

    @Test
    public void testExecute () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);
        DeviceUserFileConfigurationInformation collectedData = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);
        StoreConfigurationUserFile command = new StoreConfigurationUserFile(collectedData);
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).storeConfigurationFile(any(DeviceIdentifier.class), any(DateFormat.class), anyString(), any(byte[].class));
        verify(comServerDAO, never()).findDevice(deviceIdentifier);
    }

    @Test
    public void testToString () {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);
        DeviceUserFileConfigurationInformation collectedData = new DeviceUserFileConfigurationInformation(deviceIdentifier, FILE_EXTENSION, CONTENTS);
        StoreConfigurationUserFile command = new StoreConfigurationUserFile(collectedData);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        assertThat(journalMessage).isEqualTo(StoreConfigurationUserFile.class.getSimpleName()
                + " {deviceIdentifier: id 97; file extension: txt}");
    }
}