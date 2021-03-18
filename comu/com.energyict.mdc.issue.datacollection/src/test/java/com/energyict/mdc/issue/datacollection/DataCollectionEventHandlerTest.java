/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.impl.service.IssueCreationServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.issue.datacollection.event.ConnectionLostEvent;
import com.energyict.mdc.issue.datacollection.event.ConnectionLostResolvedEvent;
import com.energyict.mdc.issue.datacollection.event.DeviceCommunicationFailureEvent;
import com.energyict.mdc.issue.datacollection.event.UnableToConnectEvent;
import com.energyict.mdc.issue.datacollection.event.UnableToConnectResolvedEvent;
import com.energyict.mdc.issue.datacollection.event.UnknownInboundDeviceEvent;
import com.energyict.mdc.issue.datacollection.event.UnknownSlaveDeviceEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventHandlerFactory;

import org.osgi.service.event.EventConstants;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataCollectionEventHandlerTest extends BaseTest {
    protected DeviceType deviceType;
    protected DeviceConfiguration deviceConfiguration;
    private State state;
    private Stage stage;
    private Meter meter;

    @Test
    @Transactional
    public void testSuccessfullProcess() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, Long.toString(device.getId()));
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        MessageHandler handler = getDataCollectionEventHandler(getMockIssueCreationService());

        Boolean isProcessed = false;
        try {
            handler.process(message);
        } catch (DispatchCreationEventException ex) {
            assertThat(ex.getMessage()).isEqualTo("processed!");
            isProcessed = true;
        }
        assertThat(isProcessed).isTrue();
    }

    @Test
    @Transactional
    public void testUnmappedEvent() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/unknown/EVENT");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, device.getId());
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        MessageHandler handler = getDataCollectionEventHandler(getMockIssueCreationService());

        try {
            handler.process(message);
        } catch (DispatchCreationEventException ex) {
            fail("This event shouldn't be processed");
        }
    }

    @Test
    @Transactional
    public void testEventMappingConnectionLost() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.SKIPPED_TASK_IDS, "1");
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, device.getId());
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(ConnectionLostEvent.class, UnableToConnectResolvedEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
        verify(state, times(1)).getStage();
        verify(meter, times(1)).getState();
    }

    @Test
    @Transactional
    public void testEventSplittingConnectionLost() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.SKIPPED_TASK_IDS, "1, 10, 47");
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, device.getId());
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventCountServiceMock service = new CheckEventCountServiceMock();
        getDataCollectionEventHandler(service).process(message);
        assertThat(service.getCounter()).isEqualTo(1);
        verify(state, times(1)).getStage();
        verify(meter, times(1)).getState();
    }

    @Test
    @Transactional
    public void testEventMappingDeviceCommunicationFailed() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.FAILED_TASK_IDS, "1");
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, device.getId());
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(DeviceCommunicationFailureEvent.class, ConnectionLostResolvedEvent.class, UnableToConnectResolvedEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
        verify(state, times(3)).getStage();
        verify(meter, times(3)).getState();
    }

    @Test
    @Transactional
    public void testEventSplittingDeviceCommunicationFailed() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.FAILED_TASK_IDS, "1, 17, 56, 57");
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, device.getId());
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventCountServiceMock service = new CheckEventCountServiceMock();
        getDataCollectionEventHandler(service).process(message);
        assertThat(service.getCounter()).isEqualTo(6);
        verify(state, times(6)).getStage();
        verify(meter, times(6)).getState();
    }

    @Test
    @Transactional
    public void testEventSplittingSeveralEvents() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.SKIPPED_TASK_IDS, "2,41,");
        messageMap.put(ModuleConstants.FAILED_TASK_IDS, " 1 , 17 ,  , 56 ");
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, device.getId());
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventCountServiceMock service = new CheckEventCountServiceMock();
        getDataCollectionEventHandler(service).process(message);
        assertThat(service.getCounter()).isEqualTo(4);
        verify(state, times(4)).getStage();
        verify(meter, times(4)).getState();
    }

    @Test
    @Transactional
    public void testEventMappingUnableToConnect() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/FAILURE");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, device.getId());
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(UnableToConnectEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
        verify(state, times(1)).getStage();
        verify(meter, times(1)).getState();
    }

    @Test
    @Transactional
    public void testEventMappingUnknownInboundDevice() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, Long.toString(device.getId()));
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(UnknownInboundDeviceEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
    }

    @Test
    @Transactional
    public void testEventMappingUnknownOutboundDevice() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        Device device = createDevice();
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, Long.toString(device.getId()));
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(UnknownSlaveDeviceEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
    }

    private MessageHandler getDataCollectionEventHandler(IssueCreationService issueCreationService) {
        IssueService issueService = mockIssueService(issueCreationService);
        MeteringService meteringService = mockMeteringService();
        EventService eventService = mockEventService();
        DataCollectionEventHandlerFactory handlerFactory = getInjector().getInstance(DataCollectionEventHandlerFactory.class);
        handlerFactory.setIssueService(issueService);
        handlerFactory.setDeviceService(getDeviceService());
        handlerFactory.setMeteringService(meteringService);
        handlerFactory.setEventService(eventService);
        return handlerFactory.newMessageHandler();
    }

    private IssueService mockIssueService(IssueCreationService issueCreationService) {
        IssueService issueService = mock(IssueService.class);
        when(issueService.getIssueCreationService()).thenReturn(issueCreationService);
        IssueStatus status = mock(IssueStatus.class);
        when(issueService.findStatus(IssueStatus.OPEN)).thenReturn(Optional.of(status));
        return issueService;
    }

    private EventService mockEventService() {
        EventService eventService = mock(EventService.class);
        when(eventService.getEventType(Matchers.anyString())).thenReturn(Optional.empty());
        return eventService;
    }

    private MeteringService mockMeteringService() {
        MeteringService meteringService = mock(MeteringService.class);
        AmrSystem amrSystem = mock(AmrSystem.class);
        meter = mock(Meter.class);
        state = mock(State.class);
        stage = mock(Stage.class);
        when(stage.getName()).thenReturn(EndDeviceStage.OPERATIONAL.getKey());
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(meter.getState()).thenReturn(Optional.of(state));
        when(meteringService.findEndDeviceByMRID(Matchers.anyString())).thenReturn(Optional.empty());

        when(meteringService.findAmrSystem(1)).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter(Matchers.anyString())).thenReturn(Optional.of(meter));
        return meteringService;

    }

    protected class CheckEventTypeServiceMock extends IssueCreationServiceImpl {
        private List<Class<? extends IssueEvent>> expectedClasses;
        private int size = 0;

        @SafeVarargs
        public CheckEventTypeServiceMock(Class<? extends IssueEvent>... expectedClasses) {
            this.expectedClasses = new ArrayList<>(Arrays.asList(expectedClasses));
        }

        @Override
        public void dispatchCreationEvent(List<IssueEvent> events) {
            for (IssueEvent event : events) {
                if (!expectedClasses.contains(event.getClass())) {
                    size++;
                }
                ;
            }
        }

        public boolean isSuccessfull() {
            return size == 0;
        }
    }

    protected class CheckEventCountServiceMock extends IssueCreationServiceImpl {
        private int counter = 0;

        @Override
        public void dispatchCreationEvent(List<IssueEvent> events) {
            counter = events.size();
        }

        public int getCounter() {
            return counter;
        }
    }

    private Device createDevice() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getId()).thenReturn(1L);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getClientSecurityPropertySpec()).thenReturn(Optional.empty());
        deviceType = getDeviceConfigurationService().newDeviceType("DeviceTypeTest", deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigTest");
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();
        Device device = getDeviceService().newDevice(deviceConfiguration, "SerialNumberTest", "DeviceTest", Instant.now());
        device.save();
        return device;
    }
}
