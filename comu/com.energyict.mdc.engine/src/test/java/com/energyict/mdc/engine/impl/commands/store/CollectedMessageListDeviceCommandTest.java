/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessage;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageList;
import com.energyict.mdc.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
    @Mock
    private TransactionService transactionService;

    @Before
    public void initialize(){
        when(deviceMessage1.getId()).thenReturn(MESSAGE_ID1);
        when(deviceMessage2.getId()).thenReturn(MESSAGE_ID2);
        when(serviceProvider.issueService()).thenReturn(issueService);
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(serviceProvider.transactionService()).thenReturn(transactionService);
        when(transactionService.isInTransaction()).thenReturn(true);
    }

    @Test
    public void testExecute() {
        freezeClock(new Date());
        final DeviceMessageIdentifierById deviceMessageIdentifier = new DeviceMessageIdentifierById(deviceMessage1.getId(), deviceMessage1.getDeviceIdentifier());
        when(this.deviceMessageService.findDeviceMessageByIdentifier(deviceMessageIdentifier)).thenReturn(Optional.of(this.deviceMessage1));
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage.getMessageIdentifier()).thenReturn(deviceMessageIdentifier);
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
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierById(deviceMessage1.getId(), deviceMessage1.getDeviceIdentifier()), DeviceMessageStatus.CONFIRMED, Instant.now(getClock()), null);
    }

    @Test
    public void testToJournalMessageDescriptionOnDebugLevel() {
        freezeClock(new Date());
        final DeviceMessageIdentifierById deviceMessageIdentifier1 = new DeviceMessageIdentifierById(deviceMessage1.getId(), deviceMessage1.getDeviceIdentifier());
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setSentDate(getClock().instant());
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierById deviceMessageIdentifier2 = new DeviceMessageIdentifierById(deviceMessage2.getId(), deviceMessage2.getDeviceIdentifier());
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
        assertThat(journalMessage).contains("{messageIdentifier: messageId = 12, message status: confirmed, sent date: " + getClock().instant() +
                ", protocolInfo: null; messageIdentifier: messageId = 32, message status: indoubt, sent date: " + getClock().instant() + ", protocolInfo: null}");
    }

    @Test
    public void testToJournalMessageDescriptionOnInfoLevel() {
        final DeviceMessageIdentifierById deviceMessageIdentifier1 = new DeviceMessageIdentifierById(deviceMessage1.getId(), deviceMessage1.getDeviceIdentifier());
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierById deviceMessageIdentifier2 = new DeviceMessageIdentifierById(deviceMessage2.getId(), deviceMessage2.getDeviceIdentifier());
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
        assertThat(journalMessage).contains("{messageIdentifier: messageId = 12, message status: confirmed; messageIdentifier: messageId = 32, message status: indoubt}");
    }

    @Test
    public void testUnProcessedDeviceMessage(){
        freezeClock(new Date());
        final DeviceMessageIdentifierById deviceMessageIdentifier1 = new DeviceMessageIdentifierById(deviceMessage1.getId(), deviceMessage1.getDeviceIdentifier());
        doReturn(Optional.of(this.deviceMessage1)).when(this.deviceMessageService).findDeviceMessageByIdentifier(deviceMessageIdentifier1);
        DeviceProtocolMessage collectedMessage1 = new DeviceProtocolMessage(deviceMessageIdentifier1);
        collectedMessage1.setSentDate(getClock().instant());
        collectedMessage1.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        final DeviceMessageIdentifierById deviceMessageIdentifier2 = new DeviceMessageIdentifierById(deviceMessage2.getId(), deviceMessage2.getDeviceIdentifier());
        doReturn(Optional.of(this.deviceMessage2)).when(this.deviceMessageService).findDeviceMessageByIdentifier(deviceMessageIdentifier2);
        DeviceProtocolMessage collectedMessage2 = new DeviceProtocolMessage(deviceMessageIdentifier2);
        collectedMessage2.setSentDate(getClock().instant());
        collectedMessage2.setNewDeviceMessageStatus(DeviceMessageStatus.INDOUBT);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage1.getMessageIdentifier()).thenReturn(deviceMessageIdentifier1);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage2.getMessageIdentifier()).thenReturn(deviceMessageIdentifier2);
        when(offlineDeviceMessage2.getDeviceMessageStatus()).thenReturn(DeviceMessageStatus.SENT);

        DeviceProtocolMessageList deviceProtocolMessageList = new DeviceProtocolMessageList(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2), this.deviceMessageService);
        deviceProtocolMessageList.addCollectedMessage(collectedMessage1);
        // only adding the CollectedResult for DeviceMessage 1

        DeviceCommand command = deviceProtocolMessageList.toDeviceCommand(meterDataStoreCommand, serviceProvider);
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.lockDeviceMessages(ImmutableList.of(offlineDeviceMessage1, offlineDeviceMessage2)))
                .thenReturn(ImmutableMap.of(offlineDeviceMessage1, deviceMessage1, offlineDeviceMessage2, deviceMessage2));

        // Business method
        command.execute(comServerDAO);
        verify(comServerDAO).updateDeviceMessageInformation(new DeviceMessageIdentifierById(deviceMessage1.getId(), deviceMessage1.getDeviceIdentifier()), DeviceMessageStatus.CONFIRMED, Instant.now(getClock()), null);
        verify(comServerDAO).updateDeviceMessageInformation(deviceMessage2, DeviceMessageStatus.SENT, null, CollectedMessageList.REASON_FOR_PENDING_STATE);
    }

}
