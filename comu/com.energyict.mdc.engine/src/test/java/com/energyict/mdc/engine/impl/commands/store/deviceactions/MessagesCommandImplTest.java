package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.tasks.MessagesTask;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 16/02/2016 - 10:51
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagesCommandImplTest {

    private static final int DEVICE_MESSAGE_CATEGORY_ID = 1;
    private static final long DEVICE_ID = 100;

    @Mock
    MessagesTask messageTask;
    @Mock
    DeviceMessageCategory deviceMessageCategory;
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
    @Mock
    IssueService issueService;
    @Mock
    Thesaurus thesaurus;

    @Before
    public void setUp() throws Exception {
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

        MessagesCommandImpl messagesCommand = new MessagesCommandImpl(messageTask, offlineDevice, commandRoot, comTaskExecution, this.issueService, thesaurus);

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
        when(deviceProtocol.updateSentMessages(anyList())).thenReturn(mock(CollectedMessageList.class));
        when(deviceProtocol.executePendingMessages(anyList())).thenReturn(mock(CollectedMessageList.class));
        when(this.issueService.newProblem(any(), any(), any(MessageSeed.class), anyVararg())).thenReturn(mock(Problem.class));
        MessagesCommandImpl messagesCommand = new MessagesCommandImpl(messageTask, offlineDevice, commandRoot, comTaskExecution, this.issueService, this.thesaurus);

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
        when(messageSpec.getCategory()).thenReturn(deviceMessageCategory);

        when(message.getReleaseDate()).thenReturn(releaseDate);
        return message;
    }
}