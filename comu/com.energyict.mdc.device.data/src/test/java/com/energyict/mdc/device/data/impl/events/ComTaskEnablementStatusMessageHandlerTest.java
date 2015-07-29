package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.tasks.ComTask;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.event.EventConstants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Tests the {@link ComTaskEnablementStatusMessageHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-29 (10:33)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskEnablementStatusMessageHandlerTest {

    private static final long DEVICE_CONFIGURATION_ID = 97;
    private static final long COMTASK_ID = DEVICE_CONFIGURATION_ID + 1;
    private static final long COMTASK_ENABLEMENT_ID = COMTASK_ID + 1;

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
    public void initializeMocks() {
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
    public void testProcessUnIntendedMessage() {
        Map<String, Object> messageProperties = new HashMap<>();
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
    public void testSuspend() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("id", COMTASK_ENABLEMENT_ID);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_SUSPEND.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).suspendAll(this.comTask, this.deviceConfiguration);
    }

    @Test
    public void testResume() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("id", COMTASK_ENABLEMENT_ID);
        messageProperties.put(EventConstants.TIMESTAMP, new Date().getTime());
        messageProperties.put(EventConstants.EVENT_TOPIC, EventType.COMTASKENABLEMENT_RESUME.topic());
        String payload = this.getJsonService().serialize(messageProperties);
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());

        // Business method
        this.newHandler().process(message);

        // Asserts
        verify(this.communicationTaskService).resumeAll(this.comTask, this.deviceConfiguration);
    }

    private JsonService getJsonService() {
        return this.jsonService;
    }

    private ComTaskEnablementStatusMessageHandler newHandler() {
        return new ComTaskEnablementStatusMessageHandler(this.getJsonService(), this.deviceConfigurationService, this.communicationTaskService);
    }

}