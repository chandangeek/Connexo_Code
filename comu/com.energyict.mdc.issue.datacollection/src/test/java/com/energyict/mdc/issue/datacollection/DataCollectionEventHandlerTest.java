package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.issue.impl.service.IssueCreationServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.event.ConnectionLostEvent;
import com.energyict.mdc.issue.datacollection.event.ConnectionLostResolvedEvent;
import com.energyict.mdc.issue.datacollection.event.DeviceCommunicationFailureEvent;
import com.energyict.mdc.issue.datacollection.event.UnableToConnectEvent;
import com.energyict.mdc.issue.datacollection.event.UnableToConnectResolvedEvent;
import com.energyict.mdc.issue.datacollection.event.UnknownInboundDeviceEvent;
import com.energyict.mdc.issue.datacollection.event.UnknownSlaveDeviceEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventHandlerFactory;

import org.junit.Test;
import org.mockito.Matchers;
import org.osgi.service.event.EventConstants;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataCollectionEventHandlerTest extends BaseTest {
    @Test
    public void testSuccessfullProcess() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
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
    public void testUnmappedEvent() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/unknown/EVENT");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        MessageHandler handler = getDataCollectionEventHandler(getMockIssueCreationService());

        try {
            handler.process(message);
        } catch (DispatchCreationEventException ex) {
            fail("This event shouldn't be processed");
        }
    }

    @Test
    public void testEventMappingConnectionLost() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.SKIPPED_TASK_IDS, "1");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, 1);
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(ConnectionLostEvent.class, UnableToConnectResolvedEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
    }

    @Test
    public void testEventSplittingConnectionLost() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.SKIPPED_TASK_IDS, "1, 10, 47");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, 1);
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventCountServiceMock service = new CheckEventCountServiceMock();
        getDataCollectionEventHandler(service).process(message);
        assertThat(service.getCounter()).isEqualTo(2);
    }

    @Test
    public void testEventMappingDeviceCommunicationFailed() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.FAILED_TASK_IDS, "1");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, 1);
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(DeviceCommunicationFailureEvent.class, ConnectionLostResolvedEvent.class, UnableToConnectResolvedEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
    }

    @Test
    public void testEventSplittingDeviceCommunicationFailed() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.FAILED_TASK_IDS, "1, 17, 56, 57");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, 1);
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventCountServiceMock service = new CheckEventCountServiceMock();
        getDataCollectionEventHandler(service).process(message);
        assertThat(service.getCounter()).isEqualTo(6);
    }

    @Test
    public void testEventSplittingSeveralEvents() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.SKIPPED_TASK_IDS, "2,41,");
        messageMap.put(ModuleConstants.FAILED_TASK_IDS, " 1 , 17 ,  , 56 ");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, 1);
        messageMap.put(ModuleConstants.CONNECTION_TASK_ID, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventCountServiceMock service = new CheckEventCountServiceMock();
        getDataCollectionEventHandler(service).process(message);
        assertThat(service.getCounter()).isEqualTo(5);
    }

    @Test
    public void testEventMappingUnableToConnect() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/FAILURE");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, 1);
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(UnableToConnectEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
    }

    @Test
    public void testEventMappingUnknownInboundDevice() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(UnknownInboundDeviceEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
    }

    @Test
    public void testEventMappingUnknownOutboundDevice() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventTypeServiceMock mock = new CheckEventTypeServiceMock(UnknownSlaveDeviceEvent.class);
        getDataCollectionEventHandler(mock).process(message);
        assertThat(mock.isSuccessfull()).isTrue();
    }

    private MessageHandler getDataCollectionEventHandler(IssueCreationService issueCreationService) {
        IssueService issueService = mockIssueService(issueCreationService);
        DeviceService deviceService = mockDeviceService();
        MeteringService meteringService = mockMeteringService();
        DataCollectionEventHandlerFactory handlerFactory = getInjector().getInstance(DataCollectionEventHandlerFactory.class);
        handlerFactory.setIssueService(issueService);
        handlerFactory.setDeviceService(deviceService);
        handlerFactory.setMeteringService(meteringService);
        return handlerFactory.newMessageHandler();
    }

    private IssueService mockIssueService(IssueCreationService issueCreationService) {
        IssueService issueService = mock(IssueService.class);
        when(issueService.getIssueCreationService()).thenReturn(issueCreationService);
        IssueStatus status = mock(IssueStatus.class);
        when(issueService.findStatus(IssueStatus.OPEN)).thenReturn(Optional.of(status));
        return issueService;
    }

    private MeteringService mockMeteringService() {
        MeteringService meteringService = mock(MeteringService.class);
        AmrSystem amrSystem = mock(AmrSystem.class);
        Meter meter = mock(Meter.class);

        when(meteringService.findAmrSystem(1)).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter(Matchers.anyString())).thenReturn(Optional.of(meter));
        return meteringService;

    }

    private DeviceService mockDeviceService() {
        DeviceService deviceDataService = mock(DeviceService.class);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceDataService.findDeviceById(1)).thenReturn(Optional.of(device));
        return deviceDataService;
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
}
