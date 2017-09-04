/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.tasks.ComTask;

import org.osgi.service.event.EventConstants;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private static final long CONNECTION_FUNCTION_ID1 = 123L;
    private static final long CONNECTION_FUNCTION_ID2 = 124L;

    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private PartialConnectionTask partialConnectionTask1;
    @Mock
    private PartialConnectionTask partialConnectionTask2;
    @Mock
    private ConnectionFunction connectionFunction1;
    @Mock
    private ConnectionFunction connectionFunction2;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;

    private JsonService jsonService = new JsonServiceImpl();

    @Before
    public void initializeMocks() {
        when(connectionFunction1.getId()).thenReturn(CONNECTION_FUNCTION_ID1);
        when(connectionFunction2.getId()).thenReturn(CONNECTION_FUNCTION_ID2);
        when(this.deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocolPluggableClass.getConsumableConnectionFunctions()).thenReturn(Arrays.asList(connectionFunction1, connectionFunction2));

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
    public void testSwitchOnUsingConnectionFunctionEventData() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("newConnectionFunctionId", CONNECTION_FUNCTION_ID1);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_ON_CONNECTION_FUNCTION.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchOnConnectionFunction(this.comTask, this.deviceConfiguration, this.connectionFunction1);
    }

    @Test
    public void testSwitchOffUsingConnectionFunctionEventData() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("oldConnectionFunctionId", CONNECTION_FUNCTION_ID1);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_OFF_CONNECTION_FUNCTION.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchOffConnectionFunction(this.comTask, this.deviceConfiguration, this.connectionFunction1);
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
    public void testSwitchFromDefaultToConnectionFunction() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("newConnectionFunctionId", CONNECTION_FUNCTION_ID1);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_CONNECTION_FUNCTION.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchFromDefaultConnectionTaskToConnectionFunction(this.comTask, this.deviceConfiguration, this.connectionFunction1);
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
    public void testSwitchFromTaskToConnectionFunction() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("oldPartialConnectionTaskId", PARTIAL_CONNECTION_TASK_ID1);
        messageProperties.put("newConnectionFunctionId", CONNECTION_FUNCTION_ID1);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_CONNECTION_FUNCTION.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchFromPreferredConnectionTaskToConnectionFunction(this.comTask, this.deviceConfiguration, this.partialConnectionTask1, this.connectionFunction1);
    }


   @Test
    public void testSwitchFromConnectionFunctionToDefault() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("oldConnectionFunctionId", CONNECTION_FUNCTION_ID1);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_FROM_CONNECTION_FUNCTION_TO_DEFAULT.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchFromConnectionFunctionToDefault(this.comTask, this.deviceConfiguration, this.connectionFunction1);
    }

    @Test
    public void testSwitchFromConnectionFunctionToTask() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("oldConnectionFunctionId", CONNECTION_FUNCTION_ID1);
        messageProperties.put("newPartialConnectionTaskId", PARTIAL_CONNECTION_TASK_ID1);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_FROM_CONNECTION_FUNCTION_TO_TASK.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).switchFromConnectionFunctionToPreferredConnectionTask(this.comTask, this.deviceConfiguration, this.connectionFunction1, this.partialConnectionTask1);
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
    public void testSwitchBetweenConnectionFunctions() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("oldConnectionFunctionId", CONNECTION_FUNCTION_ID1);
        messageProperties.put("newConnectionFunctionId", CONNECTION_FUNCTION_ID2);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SWITCH_BETWEEN_CONNECTION_FUNCTIONS.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).preferredConnectionFunctionChanged(this.comTask, this.deviceConfiguration, this.connectionFunction1, this.connectionFunction2);
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
        verify(this.communicationTaskService).switchOnPreferredConnectionTask(this.comTask, this.deviceConfiguration, this.partialConnectionTask2);
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