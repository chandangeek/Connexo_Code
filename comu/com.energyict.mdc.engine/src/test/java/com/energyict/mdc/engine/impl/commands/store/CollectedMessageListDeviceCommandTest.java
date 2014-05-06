package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.messages.EndDeviceMessageFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.DeviceProtocolMessage;
import com.energyict.mdc.meterdata.DeviceProtocolMessageList;
import com.energyict.mdc.meterdata.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CollectedMessageListDeviceCommand} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/03/13
 * Time: 9:37
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedMessageListDeviceCommandTest {

    private static final int MESSAGE_ID1 = 12;
    private static final int MESSAGE_ID2 = 32;

    @Mock
    private ServerManager manager;
    @Mock
    private EndDeviceMessageFactory deviceMessageFactory;
    @Mock
    private DeviceMessage deviceMessage1;
    @Mock
    private DeviceMessage deviceMessage2;
    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;

    @Before
    public void initialize(){
        ManagerFactory.setCurrent(manager);
        when(deviceMessage1.getId()).thenReturn(MESSAGE_ID1);
        when(deviceMessage2.getId()).thenReturn(MESSAGE_ID2);
        when(manager.getDeviceMessageFactory()).thenReturn(deviceMessageFactory);
        when(deviceMessageFactory.find(MESSAGE_ID1)).thenReturn(deviceMessage1);
        when(deviceMessageFactory.find(MESSAGE_ID2)).thenReturn(deviceMessage2);
    }

    @Test
    public void testExecute() {
        final DeviceMessageIdentifierById deviceMessageIdentifier = new DeviceMessageIdentifierById(MESSAGE_ID1);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage.getDeviceMessageId()).thenReturn(MESSAGE_ID1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier);
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage));
        deviceProtocolMessageList.addCollectedMessages(collectedMessage1);
        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(issueService);
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierById(MESSAGE_ID1), DeviceMessageStatus.CONFIRMED, null);
    }

    @Test
    public void testToJournalMessageDescriptionOnDebugLevel() {
        final DeviceMessageIdentifierById deviceMessageIdentifier1 = new DeviceMessageIdentifierById(MESSAGE_ID1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierById deviceMessageIdentifier2 = new DeviceMessageIdentifierById(MESSAGE_ID2);
        DeviceProtocolMessage collectedMessage2 = new DeviceProtocolMessage(deviceMessageIdentifier2);
        collectedMessage2.setNewDeviceMessageStatus(DeviceMessageStatus.INDOUBT);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage1.getDeviceMessageId()).thenReturn(MESSAGE_ID1);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage2.getDeviceMessageId()).thenReturn(MESSAGE_ID2);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2));
        deviceProtocolMessageList.addCollectedMessages(collectedMessage1);
        deviceProtocolMessageList.addCollectedMessages(collectedMessage2);
        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(issueService);


        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedMessageListDeviceCommand.class.getSimpleName()
                + " {messageIdentifier: messageId = 12, message status: confirmed, protocolInfo: null; messageIdentifier: messageId = 32, message status: indoubt, protocolInfo: null}");
    }

    @Test
    public void testToJournalMessageDescriptionOnInfoLevel() {
        final DeviceMessageIdentifierById deviceMessageIdentifier1 = new DeviceMessageIdentifierById(MESSAGE_ID1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierById deviceMessageIdentifier2 = new DeviceMessageIdentifierById(MESSAGE_ID2);
        DeviceProtocolMessage collectedMessage2 = new DeviceProtocolMessage(deviceMessageIdentifier2);
        collectedMessage2.setNewDeviceMessageStatus(DeviceMessageStatus.INDOUBT);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage1.getDeviceMessageId()).thenReturn(MESSAGE_ID1);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage2.getDeviceMessageId()).thenReturn(MESSAGE_ID2);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2));
        deviceProtocolMessageList.addCollectedMessages(collectedMessage1);
        deviceProtocolMessageList.addCollectedMessages(collectedMessage2);
        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(issueService);


        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedMessageListDeviceCommand.class.getSimpleName()
                + " {messageIdentifier: messageId = 12, message status: confirmed; messageIdentifier: messageId = 32, message status: indoubt}");
    }

    @Test
    public void testUnProcessedDeviceMessage(){
        final DeviceMessageIdentifierById deviceMessageIdentifier1 = new DeviceMessageIdentifierById(MESSAGE_ID1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierById deviceMessageIdentifier2 = new DeviceMessageIdentifierById(MESSAGE_ID2);
        DeviceProtocolMessage collectedMessage2 = new DeviceProtocolMessage(deviceMessageIdentifier2);
        collectedMessage2.setNewDeviceMessageStatus(DeviceMessageStatus.INDOUBT);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage1.getDeviceMessageId()).thenReturn(MESSAGE_ID1);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage2.getDeviceMessageId()).thenReturn(MESSAGE_ID2);
        when(offlineDeviceMessage2.getDeviceMessageStatus()).thenReturn(DeviceMessageStatus.SENT);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2));
        deviceProtocolMessageList.addCollectedMessages(collectedMessage1);
        // only adding the CollectedResult for DeviceMessage 1

        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(issueService);
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierById(MESSAGE_ID1), DeviceMessageStatus.CONFIRMED, null);
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierById(MESSAGE_ID2), DeviceMessageStatus.SENT, CollectedMessageList.REASON_FOR_PENDING_STATE);
    }
}
