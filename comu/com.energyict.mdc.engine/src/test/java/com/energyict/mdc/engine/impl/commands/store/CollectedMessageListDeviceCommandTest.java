package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessage;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageList;
import com.energyict.mdc.device.data.impl.identifiers.DeviceMessageIdentifierForAlreadyKnownMessage;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link CollectedMessageListDeviceCommand} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/03/13
 * Time: 9:37
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedMessageListDeviceCommandTest {

    private static final long MESSAGE_ID1 = 12;
    private static final long MESSAGE_ID2 = 32;

    @Mock
    private DeviceMessage<Device> deviceMessage1;
    @Mock
    private DeviceMessage<Device> deviceMessage2;
    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    private IssueService issueService;
    @Mock
    private MeterDataStoreCommandImpl meterDataStoreCommand;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;

    @Before
    public void initialize(){
        when(deviceMessage1.getId()).thenReturn(MESSAGE_ID1);
        when(deviceMessage2.getId()).thenReturn(MESSAGE_ID2);
        when(serviceProvider.issueService()).thenReturn(issueService);
    }

    @Test
    public void testExecute() {
        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage.getIdentifier()).thenReturn(deviceMessageIdentifier);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier);
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage));
        deviceProtocolMessageList.addCollectedMessages(collectedMessage1);
        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(meterDataStoreCommand, serviceProvider);
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1), DeviceMessageStatus.CONFIRMED, null);
    }

    @Test
    public void testToJournalMessageDescriptionOnDebugLevel() {
        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier1 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier2 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage2);
        DeviceProtocolMessage collectedMessage2 = new DeviceProtocolMessage(deviceMessageIdentifier2);
        collectedMessage2.setNewDeviceMessageStatus(DeviceMessageStatus.INDOUBT);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2));
        deviceProtocolMessageList.addCollectedMessages(collectedMessage1);
        deviceProtocolMessageList.addCollectedMessages(collectedMessage2);
        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(meterDataStoreCommand, serviceProvider);


        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        assertThat(journalMessage).contains("{messageIdentifier: messageId = 12, message status: confirmed, protocolInfo: null; messageIdentifier: messageId = 32, message status: indoubt, protocolInfo: null}");
    }

    @Test
    public void testToJournalMessageDescriptionOnInfoLevel() {
        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier1 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier2 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage2);
        DeviceProtocolMessage collectedMessage2 = new DeviceProtocolMessage(deviceMessageIdentifier2);
        collectedMessage2.setNewDeviceMessageStatus(DeviceMessageStatus.INDOUBT);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2));
        deviceProtocolMessageList.addCollectedMessages(collectedMessage1);
        deviceProtocolMessageList.addCollectedMessages(collectedMessage2);
        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(meterDataStoreCommand, serviceProvider);


        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("{messageIdentifier: messageId = 12, message status: confirmed; messageIdentifier: messageId = 32, message status: indoubt}");
    }

    @Test
    public void testUnProcessedDeviceMessage(){
        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier1 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier2 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage2);
        DeviceProtocolMessage collectedMessage2 = new DeviceProtocolMessage(deviceMessageIdentifier2);
        collectedMessage2.setNewDeviceMessageStatus(DeviceMessageStatus.INDOUBT);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage1.getIdentifier()).thenReturn(deviceMessageIdentifier1);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage2.getIdentifier()).thenReturn(deviceMessageIdentifier2);
        when(offlineDeviceMessage2.getDeviceMessageStatus()).thenReturn(DeviceMessageStatus.SENT);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2));
        deviceProtocolMessageList.addCollectedMessages(collectedMessage1);
        // only adding the CollectedResult for DeviceMessage 1

        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(meterDataStoreCommand, serviceProvider);
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1), DeviceMessageStatus.CONFIRMED, null);
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage2), DeviceMessageStatus.SENT, CollectedMessageList.REASON_FOR_PENDING_STATE);
    }
}
