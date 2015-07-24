package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.tasks.ComTask;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.event.EventConstants;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Tests the {@link ComTaskEnablementConnectionMessageHandler} component,
 * verifying mostly that the handler delegates to the correct
 * {@link DeviceService} methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-28 (16:20)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskEnablementConnectionMessageHandlerTest {

    private static final long DEVICE_CONFIGURATION_ID = 97;
    private static final long COMTASK_ID = DEVICE_CONFIGURATION_ID + 1;
    private static final long COMTASK_ENABLEMENT_ID = COMTASK_ID + 1;
    private static final long PARTIAL_CONNECTION_TASK_ID1 = COMTASK_ENABLEMENT_ID + 1;
    private static final long PARTIAL_CONNECTION_TASK_ID2 = PARTIAL_CONNECTION_TASK_ID1 + 1;

    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private PartialConnectionTask partialConnectionTask1;
    @Mock
    private PartialConnectionTask partialConnectionTask2;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;

    private JsonService jsonService = new JsonServiceImpl();

    @Before
    public void initializeMocks() {
        when(this.deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(this.comTask.getId()).thenReturn(COMTASK_ID);
        when(this.comTaskEnablement.getId()).thenReturn(COMTASK_ENABLEMENT_ID);
        when(this.comTaskEnablement.getComTask()).thenReturn(this.comTask);
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.partialConnectionTask1.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID1);
        when(this.partialConnectionTask1.getConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.partialConnectionTask2.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID2);
        when(this.partialConnectionTask2.getConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(this.partialConnectionTask1, this.partialConnectionTask2));
        when(this.deviceConfigurationService.findComTaskEnablement(COMTASK_ENABLEMENT_ID)).thenReturn(Optional.of(this.comTaskEnablement));
        when(this.deviceConfigurationService.findPartialConnectionTask(PARTIAL_CONNECTION_TASK_ID1)).thenReturn(Optional.of(this.partialConnectionTask1));
        when(this.deviceConfigurationService.findPartialConnectionTask(PARTIAL_CONNECTION_TASK_ID2)).thenReturn(Optional.of(this.partialConnectionTask2));
    }

    /**
     * Tests that sending an unintended message to the receiver
     * does not cause any unexpected exceptions but instead
     * the message is ignored.
     */
    @Test
    public void testProcessUnIntendedMessage() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("id", 97);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.CHANNELSPEC_CREATED.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts: no exceptions is good enough
    }

    @Test
    public void testSwitchOnUsingDefaultConnectionEventData() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_ON_DEFAULT.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchOnDefault(this.comTask, this.deviceConfiguration);
    }

    @Test
    public void testSwitchOffUsingDefaultConnectionEventData() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_OFF_DEFAULT.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchOffDefault(this.comTask, this.deviceConfiguration);
    }

    @Test
    public void testSwitchFromDefaultToTask() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("partialConnectionTaskId", PARTIAL_CONNECTION_TASK_ID1);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_TASK.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchFromDefaultConnectionTaskToPreferredConnectionTask(this.comTask, this.deviceConfiguration, this.partialConnectionTask1);
    }

    @Test
    public void testSwitchFromTaskToDefault() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("partialConnectionTaskId", PARTIAL_CONNECTION_TASK_ID1);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_DEFAULT.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchFromPreferredConnectionTaskToDefault(this.comTask, this.deviceConfiguration, this.partialConnectionTask1);
    }

    @Test
    public void testSwitchBetweenTasks() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("oldPartialConnectionTaskId", PARTIAL_CONNECTION_TASK_ID1);
        messageProperties.put("newPartialConnectionTaskId", PARTIAL_CONNECTION_TASK_ID2);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_BETWEEN_TASKS.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).preferredConnectionTaskChanged(this.comTask, this.deviceConfiguration, this.partialConnectionTask1, this.partialConnectionTask2);
    }

    @Test
    public void testStartUsingTask() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("partialConnectionTaskId", PARTIAL_CONNECTION_TASK_ID2);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_START_USING_TASK.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchFromDefaultConnectionTaskToPreferredConnectionTask(this.comTask, this.deviceConfiguration, this.partialConnectionTask2);
    }

    @Test
    public void testRemoveTask() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("partialConnectionTaskId", PARTIAL_CONNECTION_TASK_ID2);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_REMOVE_TASK.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).removePreferredConnectionTask(this.comTask, this.deviceConfiguration, this.partialConnectionTask2);
    }

    private JsonService getJsonService() {
        return this.jsonService;
    }

    private ComTaskEnablementConnectionMessageHandler newHandler() {
        return new ComTaskEnablementConnectionMessageHandler(this.getJsonService(), this.deviceConfigurationService, this.communicationTaskService);
    }

    private ComTaskEnablementConnectionMessageHandler newHandler(JsonService jsonService) {
        return new ComTaskEnablementConnectionMessageHandler(jsonService, this.deviceConfigurationService, this.communicationTaskService);
    }

}