package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ItemizeComTaskEnablementQueueMessage;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.configchange.SingleComTaskEnablementQueueMessage;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.tasks.ComTask;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.osgi.service.event.EventConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 22.12.15
 * Time: 09:46
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskEnablementChangeMessageHandlerTest {

    private static final long COMTASK_ENABLEMENT_ID = 6654L;
    private static final long OTHER_COMTASK_ENABLEMENT_ID = COMTASK_ENABLEMENT_ID + 10;
    private static final long COMTASK_ID = COMTASK_ENABLEMENT_ID + 10;
    private static final long OTHER_COMTASK_ID = COMTASK_ID + 10;

    private static final long DEVICE_CONFIG_ID = 44465L;
    private static final long OTHER_DEVICE_CONFIG_ID = DEVICE_CONFIG_ID + 10;
    private static final long DEVICE_1_ID = 13L;
    private static final long DEVICE_2_ID = 14L;

    @Mock
    private JsonService jsonService;
    @Mock
    private MessageService messageService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerDeviceService deviceService;
    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock(extraInterfaces = HasId.class)
    private DeviceConfiguration deviceConfiguration;
    @Mock(extraInterfaces = HasId.class)
    private DeviceConfiguration otherDeviceConfiguration;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private ComTaskEnablement otherComTaskEnablement;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private ComTaskExecution otherComTaskExecution;
    @Mock
    private Finder<Device> finder;
    @Mock
    private Device device_1;
    @Mock
    private Device device_2;
    @Mock
    private ComTask comTask;
    @Mock
    private ComTask otherComTask;

    @Before
    public void setup() {
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.fromStringValue(Matchers.anyString())).thenAnswer(inv -> inv.getArguments()[0]);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIG_ID);
        when(otherDeviceConfiguration.getId()).thenReturn(OTHER_DEVICE_CONFIG_ID);
        when(deviceConfigurationService.findDeviceConfiguration(DEVICE_CONFIG_ID)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findDeviceConfiguration(OTHER_DEVICE_CONFIG_ID)).thenReturn(Optional.of(otherDeviceConfiguration));

        when(comTaskEnablement.getId()).thenReturn(COMTASK_ENABLEMENT_ID);
        when(otherComTaskEnablement.getId()).thenReturn(OTHER_COMTASK_ENABLEMENT_ID);
        when(comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(otherComTaskEnablement.getDeviceConfiguration()).thenReturn(otherDeviceConfiguration);
        when(comTaskEnablement.isIgnoreNextExecutionSpecsForInbound()).thenReturn(true);
        when(otherComTaskEnablement.isIgnoreNextExecutionSpecsForInbound()).thenReturn(true);
        when(deviceConfigurationService.findComTaskEnablement(COMTASK_ENABLEMENT_ID)).thenReturn(Optional.of(comTaskEnablement));
        when(deviceConfigurationService.findComTaskEnablement(OTHER_COMTASK_ENABLEMENT_ID)).thenReturn(Optional.of(otherComTaskEnablement));
        when(deviceConfigurationService.findComTaskEnablement(COMTASK_ENABLEMENT_ID)).thenReturn(Optional.of(comTaskEnablement));

        finder = mock(Finder.class);
        when(finder.stream()).thenReturn(Stream.empty());   //By default, no devices match - if needed, in specific test class, this can be overridden
        when(deviceService.findDevicesByDeviceConfiguration(deviceConfiguration)).thenReturn(finder);

        when(comTask.getId()).thenReturn(COMTASK_ID);
        when(otherComTask.getId()).thenReturn(OTHER_COMTASK_ID);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(otherComTaskEnablement.getComTask()).thenReturn(otherComTask);

        when(device_1.getId()).thenReturn(DEVICE_1_ID);
        when(device_2.getId()).thenReturn(DEVICE_2_ID);
        when(deviceService.findDeviceById(DEVICE_1_ID)).thenReturn(Optional.of(device_1));
        when(deviceService.findDeviceById(DEVICE_2_ID)).thenReturn(Optional.of(device_2));
        when(device_1.newManuallyScheduledComTaskExecution(comTaskEnablement, null)).thenReturn(mock(ComTaskExecutionBuilder.class));
    }

    @Test
    public void enablementNotMarkedAsAlwaysExecute() throws JsonProcessingException {
        when(comTaskEnablement.isIgnoreNextExecutionSpecsForInbound()).thenReturn(false);
        when(jsonService.serialize(any())).thenThrow(new RuntimeException("No new messages should be put on the queue, so serialize method should not be called"));

        ItemizeComTaskEnablementQueueMessage itemizeConfigChangeQueueMessage = new ItemizeComTaskEnablementQueueMessage(COMTASK_ENABLEMENT_ID);
        mockMessageHandlerInternals(itemizeConfigChangeQueueMessage, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_ACTION);
        ComTaskEnablementChangeMessageHandler msgHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        msgHandler.process(queueMessage);   // Should not throw any exception
    }

    @Test
    public void enablementMarkedAsAlwaysExecuteNoDevices() throws JsonProcessingException {
        when(jsonService.serialize(any())).thenThrow(new RuntimeException("No new messages should be put on the queue, so serialize method should not be called"));

        ItemizeComTaskEnablementQueueMessage itemizeConfigChangeQueueMessage = new ItemizeComTaskEnablementQueueMessage(COMTASK_ENABLEMENT_ID);
        mockMessageHandlerInternals(itemizeConfigChangeQueueMessage, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_ACTION);
        ComTaskEnablementChangeMessageHandler msgHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        msgHandler.process(queueMessage);   // Should not throw any exception
    }

    @Test
    public void enablementMarkedAsAlwaysExecuteHavingDeviceWhichHasNoComTaskExecutions() throws JsonProcessingException {
        MessageBuilder messageBuilder = createTestMessageBuilder();

        when(device_1.getComTaskExecutions()).thenReturn(Collections.emptyList()); // The device doesn't have any ComTaskExecutions, so a new one should be created
        when(device_2.getComTaskExecutions()).thenReturn(Collections.emptyList()); // The device doesn't have any ComTaskExecutions, so a new one should be created
        when(finder.stream()).thenReturn(Stream.of(device_1, device_2));

        ItemizeComTaskEnablementQueueMessage itemizeConfigChangeQueueMessage = new ItemizeComTaskEnablementQueueMessage(COMTASK_ENABLEMENT_ID);
        mockMessageHandlerInternals(itemizeConfigChangeQueueMessage, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_ACTION);
        ComTaskEnablementChangeMessageHandler msgHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        msgHandler.process(queueMessage);

        verify(messageBuilder, times(2)).send();    // I'm expecting 2 messages to be send (one for each device)
    }

    @Test
    public void enablementMarkedAsAlwaysExecuteHavingDeviceWhichHasAlreadyComTaskExecutions() throws JsonProcessingException {
        MessageBuilder messageBuilder = createTestMessageBuilder();

        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(device_1.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution)); // The device has a matching ComTaskExecution

        when(otherComTaskExecution.getComTask()).thenReturn(otherComTask);
        when(device_2.getComTaskExecutions()).thenReturn(Collections.singletonList(otherComTaskExecution)); // The device doesn't have a matching ComTaskExecution, so new one should be created

        when(finder.stream()).thenReturn(Stream.of(device_1, device_2));

        ItemizeComTaskEnablementQueueMessage itemizeConfigChangeQueueMessage = new ItemizeComTaskEnablementQueueMessage(COMTASK_ENABLEMENT_ID);
        mockMessageHandlerInternals(itemizeConfigChangeQueueMessage, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_ACTION);
        ComTaskEnablementChangeMessageHandler msgHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        msgHandler.process(queueMessage);

        verify(messageBuilder, times(1)).send();    // I'm expecting 1 message to be send (only for the device without matching ComTaskExecution)
    }

    @Test
    public void executeSingleAction() throws JsonProcessingException {
        when(device_1.getComTaskExecutions()).thenReturn(Collections.emptyList()); // The device doesn't have any ComTaskExecutions, so a new one should be created

        SingleComTaskEnablementQueueMessage singleComTaskEnablementQueueMessage = new SingleComTaskEnablementQueueMessage(DEVICE_1_ID, COMTASK_ENABLEMENT_ID);
        mockMessageHandlerInternals(singleComTaskEnablementQueueMessage, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_SINGLE_ACTION);
        ComTaskEnablementChangeMessageHandler msgHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        msgHandler.process(queueMessage);

        verify(device_1, times(1)).newManuallyScheduledComTaskExecution(comTaskEnablement, null);
    }

    @Test
    public void executeSingleActionWithMachingComTaskExecution() throws JsonProcessingException {
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(device_1.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution)); // The device has a matching ComTaskExecution

        SingleComTaskEnablementQueueMessage singleComTaskEnablementQueueMessage = new SingleComTaskEnablementQueueMessage(DEVICE_1_ID, COMTASK_ENABLEMENT_ID);
        mockMessageHandlerInternals(singleComTaskEnablementQueueMessage, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_SINGLE_ACTION);
        ComTaskEnablementChangeMessageHandler msgHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        msgHandler.process(queueMessage);

        verify(device_1, never()).newManuallyScheduledComTaskExecution(any(), any());
    }

    private MessageBuilder createTestMessageBuilder() {
        String helloFromMyTest = "HelloFromMyTest";
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(helloFromMyTest)).thenReturn(messageBuilder);
        MessageService messageService = mock(MessageService.class);
        when(deviceDataModelService.messageService()).thenReturn(messageService);
        when(messageService.getDestinationSpec(ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_DESTINATION)).thenReturn(Optional.of(destinationSpec));
        when(jsonService.serialize(any())).thenReturn(helloFromMyTest);
        return messageBuilder;
    }

    private void mockMessageHandlerInternals(QueueMessage queueMessage, String eventAction) throws JsonProcessingException {
        Map<String, Object> message = new HashMap<>(2);
        message.put(EventConstants.EVENT_TOPIC, eventAction);
        message.put(ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_MESSAGE_VALUE, new ObjectMapper().writeValueAsString(queueMessage));

        when(jsonService.deserialize((byte[]) any(), any())).thenReturn(message); // this is for the first deserialization
        when(jsonService.deserialize((String) any(), any())).thenReturn(queueMessage); // this is for the second deserialization
    }

    private ComTaskEnablementChangeMessageHandler getTestInstance() {
        return new ComTaskEnablementChangeMessageHandler(jsonService, getComTaskEnablementConfig());
    }

    private ComTaskEnablementChangeMessageHandler.ComTaskEnablementConfig getComTaskEnablementConfig() {
        return new ComTaskEnablementChangeMessageHandler.ComTaskEnablementConfig(messageService, jsonService, thesaurus, deviceService, deviceDataModelService, deviceConfigurationService);
    }
}