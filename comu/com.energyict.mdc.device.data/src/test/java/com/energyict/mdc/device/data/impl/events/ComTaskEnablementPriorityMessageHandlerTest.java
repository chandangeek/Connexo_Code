package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.EventType;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.google.common.base.Optional;
import org.osgi.service.event.EventConstants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComTaskEnablementPriorityMessageHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-29 (09:52)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskEnablementPriorityMessageHandlerTest {

    private static final long DEVICE_CONFIGURATION_ID = 97;
    private static final long COMTASK_ID = DEVICE_CONFIGURATION_ID + 1;
    private static final long COMTASK_ENABLEMENT_ID = COMTASK_ID + 1;
    private static final int OLD_PRIORITY = ComTaskEnablement.LOWEST_PRIORITY;
    private static final int NEW_PRIORITY = ComTaskEnablement.HIGHEST_PRIORITY;

    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;

    private JsonService jsonService = new JsonServiceImpl();

    @Before
    public void initializeMocks () {
        when(this.deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(this.comTask.getId()).thenReturn(COMTASK_ID);
        when(this.comTaskEnablement.getId()).thenReturn(COMTASK_ENABLEMENT_ID);
        when(this.comTaskEnablement.getComTask()).thenReturn(this.comTask);
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfigurationService.findComTaskEnablement(COMTASK_ENABLEMENT_ID)).thenReturn(Optional.of(this.comTaskEnablement));
    }

    /**
     * Tests that sending an unintended message to the receiver
     * does not cause any unexpected exceptions but instead
     * the message is ignored.
     */
    @Test
    public void testProcessUnIntendedMessage () {
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
    public void testProcessPriorityChange () {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("comTaskEnablementId", COMTASK_ENABLEMENT_ID);
        messageProperties.put("oldPriority", OLD_PRIORITY);
        messageProperties.put("newPriority", NEW_PRIORITY);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_PRIORITY_UPDATED.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).preferredPriorityChanged(this.comTask, this.deviceConfiguration, OLD_PRIORITY, NEW_PRIORITY);
    }

    private JsonService getJsonService () {
        return this.jsonService;
    }

    private ComTaskEnablementPriorityMessageHandler newHandler () {
        return new ComTaskEnablementPriorityMessageHandler(this.getJsonService(), this.deviceConfigurationService, this.communicationTaskService);
    }

}