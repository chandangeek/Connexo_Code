/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.MessagesCommand;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import org.json.JSONException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 16/02/2016 - 10:51
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagesCommandImplTest extends AbstractComCommandExecuteTest {

    private static final int DEVICE_MESSAGE_CATEGORY_ID = 1;
    private static final long DEVICE_ID = 100;

    @Mock
    MessagesTask messageTask;
    @Mock
    DeviceMessageCategory uplDeviceMessageCategory;
    @Mock
    com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory deviceMessageCategory;
    @Mock
    Device device;
    @Mock
    OfflineDevice offlineDevice;
    @Mock
    CommandRoot commandRoot;
    @Mock
    ComTaskExecution comTaskExecution;
    @Mock
    DeviceProtocol deviceProtocol;

    @Before
    public void setUp() throws Exception {
        when(uplDeviceMessageCategory.getId()).thenReturn(DEVICE_MESSAGE_CATEGORY_ID);
        when(deviceMessageCategory.getId()).thenReturn(DEVICE_MESSAGE_CATEGORY_ID);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(comTaskExecution.getDevice()).thenReturn(device);
    }

    @Test
    public void testDeviceMessageAreSortedAccordingToReleaseDate() throws Exception {
        when(messageTask.getDeviceMessageCategories()).thenReturn(Collections.singletonList(deviceMessageCategory));

        OfflineDeviceMessage msg1 = getNewOfflineDeviceMessage(Instant.ofEpochSecond(1454284800));
        OfflineDeviceMessage msg2 = getNewOfflineDeviceMessage(Instant.ofEpochSecond(1422748800));
        OfflineDeviceMessage msg3 = getNewOfflineDeviceMessage(Instant.ofEpochSecond(1264982400));
        List<OfflineDeviceMessage> deviceMessages = Arrays.asList(
                msg1,  // 01/02/2015 00:00:00
                msg2,  // 01/02/2014 00:00:00
                msg3   // 01/02/2010 00:00:00
        );
        when(offlineDevice.getAllPendingDeviceMessages()).thenReturn(deviceMessages);
        when(offlineDevice.getAllSentDeviceMessages()).thenReturn(deviceMessages);

        ArgumentCaptor<List> sentMessagesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> pendingMessagesCaptor = ArgumentCaptor.forClass(List.class);
        when(deviceProtocol.updateSentMessages(sentMessagesCaptor.capture())).thenReturn(mock(CollectedMessageList.class));
        when(deviceProtocol.executePendingMessages(pendingMessagesCaptor.capture())).thenReturn(mock(CollectedMessageList.class));
        when(offlineDevice.getMacException()).thenReturn(Optional.empty());
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        MessagesCommandImpl messagesCommand = new MessagesCommandImpl(groupedDeviceCommand, messageTask, comTaskExecution);

        // Business method
        messagesCommand.doExecute(deviceProtocol, null);

        // Assert
        assertThat(sentMessagesCaptor.getValue()).hasSize(3);
        assertThat(sentMessagesCaptor.getValue()).containsExactly(msg3, msg2, msg1);    // will verify also the order of the elements

        assertThat(pendingMessagesCaptor.getValue()).hasSize(3);
        assertThat(pendingMessagesCaptor.getValue()).containsExactly(msg3, msg2, msg1); // will verify also the order of the elements
    }

    @Test
    public void testInvalidPendingMessages() throws Exception {
        when(messageTask.getDeviceMessageCategories()).thenReturn(Collections.singletonList(deviceMessageCategory));
        OfflineDeviceMessage message = getNewOfflineDeviceMessage(Instant.ofEpochSecond(1454284800));
        List<OfflineDeviceMessage> deviceMessages = Collections.singletonList(message);
        when(offlineDevice.getAllInvalidPendingDeviceMessages()).thenReturn(deviceMessages);
        when(offlineDevice.getAllPendingDeviceMessages()).thenReturn(Collections.emptyList());
        when(offlineDevice.getAllSentDeviceMessages()).thenReturn(Collections.emptyList());
        when(offlineDevice.getMacException()).thenReturn(Optional.empty());
        when(deviceProtocol.updateSentMessages(anyList())).thenReturn(mock(CollectedMessageList.class));
        when(deviceProtocol.executePendingMessages(anyList())).thenReturn(mock(CollectedMessageList.class));
        doReturn(mock(Problem.class)).when(this.issueService).newProblem(any(), any(MessageSeed.class), anyVararg());
        doReturn(mock(Problem.class)).when(this.issueService).newProblem(any(), any(MessageSeed.class), anyVararg());

        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        MessagesCommandImpl messagesCommand = new MessagesCommandImpl(groupedDeviceCommand, messageTask, comTaskExecution);

        // Business method
        messagesCommand.doExecute(this.deviceProtocol, null);

        // Asserts
        verify(this.offlineDevice).getAllInvalidPendingDeviceMessages();
        verify(this.issueService).newProblem(this.offlineDevice, MessageSeeds.MESSAGE_NO_LONGER_VALID);
    }

    private OfflineDeviceMessage getNewOfflineDeviceMessage(Instant releaseDate) {
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        OfflineDeviceMessage message = mock(OfflineDeviceMessage.class);

        when(message.getDeviceId()).thenReturn(DEVICE_ID);
        when(message.getSpecification()).thenReturn(messageSpec);
        when(messageSpec.getCategory()).thenReturn(uplDeviceMessageCategory);

        when(message.getReleaseDate()).thenReturn(Date.from(releaseDate));
        return message;
    }

    @Test(expected = CodingException.class)
    public void messageTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new MessagesCommandImpl(createGroupedDeviceCommand(device, deviceProtocol), null, comTaskExecution);
        // exception should have occurred
    }

    @Test(expected = CodingException.class)
    public void deviceNullTest() {
        MessagesTask messagesTask = mock(MessagesTask.class);
        new MessagesCommandImpl(createGroupedDeviceCommand(null, deviceProtocol), messagesTask, comTaskExecution);
        // exception should have occurred
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        MessagesTask messagesTask = mock(MessagesTask.class);
        new MessagesCommandImpl(null, messagesTask, comTaskExecution);
        // exception should have occurred
    }

    @Test
    public void commandTypeTest() {
        MessagesTask messagesTask = mock(MessagesTask.class);
        OfflineDevice device = mock(OfflineDevice.class);
        when(device.getMacException()).thenReturn(Optional.empty());
        MessagesCommand messagesCommand = new MessagesCommandImpl(createGroupedDeviceCommand(device, deviceProtocol), messagesTask, comTaskExecution);

        // assert
        assertThat(messagesCommand.getCommandType()).isEqualTo(ComCommandTypes.MESSAGES_COMMAND);
    }

    @Test
    public void updateMessageListsTest() {
        OfflineDevice device = getMockedDeviceWithPendingAndSentMessages();
        when(device.getMacException()).thenReturn(Optional.empty());
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        MessagesCommandImpl messagesCommand = (MessagesCommandImpl) groupedDeviceCommand.getMessagesCommand(createMockedMessagesTaskWithCategories(), groupedDeviceCommand, comTaskExecution);

        //asserts
        assertThat(messagesCommand.getPendingMessages()).isNotNull();
        assertThat(messagesCommand.getPendingMessages()).hasSize(2);    // Expecting only the messages who belong to the corresponding message categories
        assertThat(messagesCommand.getSentMessages()).isNotNull();
        assertThat(messagesCommand.getSentMessages()).hasSize(1);       // Expecting only the messages who belong to the corresponding message categories
    }

    @Test
    public void testUpdateAccordingToCategories() throws Exception {
        OfflineDevice device = getMockedDeviceWithPendingAndSentMessages();
        when(device.getMacException()).thenReturn(Optional.empty());
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        MessagesCommandImpl messagesCommand = (MessagesCommandImpl) groupedDeviceCommand.getMessagesCommand(createMockedMessagesTaskWithCategories(), groupedDeviceCommand, comTaskExecution);

        //asserts
        assertThat(messagesCommand.getPendingMessages()).isNotNull();
        assertThat(messagesCommand.getPendingMessages()).hasSize(2);    // Expecting only the messages who belong to the corresponding message categories
        assertThat(messagesCommand.getSentMessages()).isNotNull();
        assertThat(messagesCommand.getSentMessages()).hasSize(1);       // Expecting only the messages who belong to the corresponding message categories

        messagesCommand.updateAccordingTo(createMockedMessagesTaskWithOtherCategories(), groupedDeviceCommand, comTaskExecution);

        //asserts
        assertThat(messagesCommand.getPendingMessages()).isNotNull();
        assertThat(messagesCommand.getPendingMessages()).hasSize(3);    // Expecting messages from all message categories
        assertThat(messagesCommand.getSentMessages()).isNotNull();
        assertThat(messagesCommand.getSentMessages()).hasSize(2);       // Expecting messages from all message categories
    }

    @Test
    public void doExecuteTest() {
        OfflineDevice device = getMockedDeviceWithPendingAndSentMessages();
        when(device.getMacException()).thenReturn(Optional.empty());
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        MessagesCommandImpl messagesCommand = (MessagesCommandImpl) groupedDeviceCommand.getMessagesCommand(createMockedMessagesTaskWithCategories(), groupedDeviceCommand, comTaskExecution);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedMessageList collectedMessageList1 = mock(CollectedMessageList.class);
        CollectedMessageList collectedMessageList2 = mock(CollectedMessageList.class);
        when(collectedMessageList1.getResultType()).thenReturn(ResultType.Supported);
        when(collectedMessageList2.getResultType()).thenReturn(ResultType.Supported);
        when(deviceProtocol.updateSentMessages(anyList())).thenReturn(collectedMessageList1);
        when(deviceProtocol.executePendingMessages(anyList())).thenReturn(collectedMessageList2);

        // Business logic
        messagesCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        verify(deviceProtocol, times(1)).updateSentMessages(Matchers.<List<OfflineDeviceMessage>>any());
        verify(deviceProtocol, times(1)).executePendingMessages(Matchers.<List<OfflineDeviceMessage>>any());
    }

    @Test
    public void testJournalMessageDescription() throws JSONException {
        OfflineDevice device = this.getMockedDeviceWithPendingAndSentMessages();
        when(device.getMacException()).thenReturn(Optional.empty());
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(device, deviceProtocol);
        MessagesTask messagesTask = this.createMockedMessagesTaskWithCategories();
        MessagesCommandImpl messagesCommand = (MessagesCommandImpl) groupedDeviceCommand.getMessagesCommand(messagesTask, groupedDeviceCommand, comTaskExecution);

        // Business method
        String infoJournalMessage = messagesCommand.toJournalMessageDescription(LogLevel.INFO);
        String debugJournalMessage = messagesCommand.toJournalMessageDescription(LogLevel.DEBUG);

        // Asserts
        assertThat(infoJournalMessage)
                .isEqualTo(
                        "Handle all device messages" +
                        " {executionState: NOT_EXECUTED; completionCode: Ok;" +
                        " nrOfPendingMessages: 2; nrOfPendingInvalidMessages: 0; nrOfMessagesFromPreviousSessions: 1}");
        assertThat(debugJournalMessage)
                .matches(
                        "Handle all device messages" +
                        " \\{executionState: NOT_EXECUTED; completionCode: Ok;" +
                        " pendingMessages: \\(.*, DeviceMessageTestCategories - TEST_SPEC_WITH_EXTENDED_SPECS\\), \\(.*, DeviceMessageTestCategories - TEST_SPEC_WITHOUT_SPECS\\);" +
                        " There are no invalid pending messages;" +
                        " messagesFromPreviousSession: \\(.*, DeviceMessageTestCategories - TEST_SPEC_WITH_SIMPLE_SPECS\\)\\}");
    }

    private MessagesTask createMockedMessagesTaskWithCategories() {
        MessagesTask messagesTask = mock(MessagesTask.class);
        when(messagesTask.getDeviceMessageCategories()).thenReturn(
                Arrays.<com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory>asList(
                        DeviceMessageTestCategories.FIRST_TEST_CATEGORY,
                        DeviceMessageTestCategories.THIRD_TEST_CATEGORY));
        return messagesTask;
    }

    private MessagesTask createMockedMessagesTaskWithOtherCategories() {
        MessagesTask messagesTask = mock(MessagesTask.class);
        when(messagesTask.getDeviceMessageCategories()).thenReturn(
                Arrays.<com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory>asList(
                        DeviceMessageTestCategories.FIRST_TEST_CATEGORY,
                        DeviceMessageTestCategories.SECOND_TEST_CATEGORY));
        return messagesTask;
    }

    private OfflineDevice getMockedDeviceWithPendingAndSentMessages() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);

        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        UPLDeviceMessageTestSpec spec1 = UPLDeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS;
        long id1 = spec1.getId();
        when(offlineDeviceMessage1.getSpecification()).thenReturn(spec1);
        when(offlineDeviceMessage1.getDeviceMessageId()).thenReturn(id1);
        when(offlineDeviceMessage1.getReleaseDate()).thenReturn(Date.from(Instant.now()));
        when(offlineDeviceMessage1.getDeviceId()).thenReturn(DEVICE_ID);

        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        AnotherUPLDeviceMessageTestSpec spec2 = AnotherUPLDeviceMessageTestSpec.TEST_SPEC_WITH_SIMPLE_SPECS;
        long id2 = spec2.getId();
        when(offlineDeviceMessage2.getSpecification()).thenReturn(spec2);
        when(offlineDeviceMessage2.getDeviceMessageId()).thenReturn(id2);
        when(offlineDeviceMessage2.getReleaseDate()).thenReturn(Date.from(Instant.now()));
        when(offlineDeviceMessage2.getDeviceId()).thenReturn(DEVICE_ID);

        OfflineDeviceMessage offlineDeviceMessage3 = mock(OfflineDeviceMessage.class);
        UPLDeviceMessageTestSpec spec3 = UPLDeviceMessageTestSpec.TEST_SPEC_WITH_SIMPLE_SPECS;
        long id3 = spec3.getId();
        when(offlineDeviceMessage3.getSpecification()).thenReturn(spec3);
        when(offlineDeviceMessage3.getDeviceMessageId()).thenReturn(id3);
        when(offlineDeviceMessage3.getReleaseDate()).thenReturn(Date.from(Instant.now()));
        when(offlineDeviceMessage3.getDeviceId()).thenReturn(DEVICE_ID);

        OfflineDeviceMessage offlineDeviceMessage4 = mock(OfflineDeviceMessage.class);
        AnotherUPLDeviceMessageTestSpec spec4 = AnotherUPLDeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS;
        long id4 = spec4.getId();
        when(offlineDeviceMessage4.getSpecification()).thenReturn(spec4);
        when(offlineDeviceMessage4.getDeviceMessageId()).thenReturn(id4);
        when(offlineDeviceMessage4.getReleaseDate()).thenReturn(Date.from(Instant.now()));
        when(offlineDeviceMessage4.getDeviceId()).thenReturn(DEVICE_ID);

        OfflineDeviceMessage offlineDeviceMessage5 = mock(OfflineDeviceMessage.class);
        UPLDeviceMessageTestSpec spec5 = UPLDeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS;
        long id5 = spec5.getId();
        when(offlineDeviceMessage5.getSpecification()).thenReturn(spec5);
        when(offlineDeviceMessage5.getDeviceMessageId()).thenReturn(id5);
        when(offlineDeviceMessage5.getReleaseDate()).thenReturn(Date.from(Instant.now()));
        when(offlineDeviceMessage5.getDeviceId()).thenReturn(DEVICE_ID);

        when(offlineDevice.getAllPendingDeviceMessages()).thenReturn(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2, offlineDeviceMessage5));
        when(offlineDevice.getAllSentDeviceMessages()).thenReturn(Arrays.asList(offlineDeviceMessage3, offlineDeviceMessage4));
        return offlineDevice;
    }

}