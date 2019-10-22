/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceMessageIdentifierForAlreadyKnownMessage;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessage;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageList;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectedMessageListDeviceCommandTest extends AbstractCollectedDataIntegrationTest {

    private static final long MESSAGE_ID1 = 12;
    private static final long MESSAGE_ID2 = 32;

    @Mock
    private DeviceMessage deviceMessage1;
    @Mock
    private DeviceMessage deviceMessage2;
    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    private IssueService issueService;
    @Mock
    private MeterDataStoreCommandImpl meterDataStoreCommand;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;
    @Mock
    private DeviceMessageService deviceMessageService;

    @Before
    public void initialize(){
        when(deviceMessage1.getId()).thenReturn(MESSAGE_ID1);
        when(deviceMessage2.getId()).thenReturn(MESSAGE_ID2);
        when(serviceProvider.issueService()).thenReturn(issueService);
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testExecute() {
        freezeClock(new Date());
        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1);
        when(this.deviceMessageService.findDeviceMessageByIdentifier(deviceMessageIdentifier)).thenReturn(Optional.of(this.deviceMessage1));
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage.getIdentifier()).thenReturn(deviceMessageIdentifier);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier);
        collectedMessage1.setSentDate(getClock().instant());
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Collections.singletonList(offlineDeviceMessage), this.deviceMessageService);
        deviceProtocolMessageList.addCollectedMessage(collectedMessage1);
        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(meterDataStoreCommand, serviceProvider);
        command.logExecutionWith(this.executionLogger);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1), DeviceMessageStatus.CONFIRMED, Instant.now(getClock()), null);
    }

    @Test
    public void testToJournalMessageDescriptionOnDebugLevel() {
        freezeClock(new Date());
        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier1 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setSentDate(getClock().instant());
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier2 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage2);
        DeviceProtocolMessage collectedMessage2 = new DeviceProtocolMessage(deviceMessageIdentifier2);
        collectedMessage2.setSentDate(getClock().instant());
        collectedMessage2.setNewDeviceMessageStatus(DeviceMessageStatus.INDOUBT);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2), this.deviceMessageService);
        deviceProtocolMessageList.addCollectedMessage(collectedMessage1);
        deviceProtocolMessageList.addCollectedMessage(collectedMessage2);
        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(meterDataStoreCommand, serviceProvider);


        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        assertThat(journalMessage).contains("{messageIdentifier: message having id 12, message status: confirmed, sent date: " + getClock().instant() +
                ", protocolInfo: null; messageIdentifier: message having id 32, message status: indoubt, sent date: " + getClock().instant() + ", protocolInfo: null}");
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

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2), this.deviceMessageService);
        deviceProtocolMessageList.addCollectedMessage(collectedMessage1);
        deviceProtocolMessageList.addCollectedMessage(collectedMessage2);
        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(meterDataStoreCommand, serviceProvider);


        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("{messageIdentifier: message having id 12, message status: confirmed; messageIdentifier: message having id 32, message status: indoubt}");
    }

    @Test
    public void testUnProcessedDeviceMessage(){
        freezeClock(new Date());
        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier1 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1);
        doReturn(Optional.of(this.deviceMessage1)).when(this.deviceMessageService).findDeviceMessageByIdentifier(deviceMessageIdentifier1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setSentDate(getClock().instant());
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifier2 = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage2);
        doReturn(Optional.of(this.deviceMessage2)).when(this.deviceMessageService).findDeviceMessageByIdentifier(deviceMessageIdentifier2);
        DeviceProtocolMessage collectedMessage2 = new DeviceProtocolMessage(deviceMessageIdentifier2);
        collectedMessage2.setSentDate(getClock().instant());
        collectedMessage2.setNewDeviceMessageStatus(DeviceMessageStatus.INDOUBT);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage1.getIdentifier()).thenReturn(deviceMessageIdentifier1);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage2.getIdentifier()).thenReturn(deviceMessageIdentifier2);
        when(offlineDeviceMessage2.getDeviceMessageStatus()).thenReturn(DeviceMessageStatus.SENT);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2), this.deviceMessageService);
        deviceProtocolMessageList.addCollectedMessage(collectedMessage1);
        // only adding the CollectedResult for DeviceMessage 1

        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(meterDataStoreCommand, serviceProvider);
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage1), DeviceMessageStatus.CONFIRMED, Instant.now(getClock()), null);
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage2), DeviceMessageStatus.SENT, null, CollectedMessageList.REASON_FOR_PENDING_STATE);
    }

}