package com.energyict.mdc.issue.tests;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.service.IssueCreationServiceImpl;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.event.*;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventHandlerFactory;
import org.junit.Test;
import org.mockito.Matchers;
import org.osgi.service.event.EventConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataCollectionEventHandlerTest extends BaseTest {
    @Test
    public void testSuccessfullProcess() {
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
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
    public void testUnmappedEvent(){
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/unknown/EVENT");
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
    public void testEventMappingConnectionLost(){
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(ModuleConstants.SKIPPED_TASK_IDS, "1");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        getDataCollectionEventHandler(new CheckEventTypeServiceMock(ConnectionLostEvent.class)).process(message);
    }

    @Test
    public void testEventSplittingConnectionLost(){
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(ModuleConstants.SKIPPED_TASK_IDS, "1, 10, 47");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventCountServiceMock service = new CheckEventCountServiceMock();
        getDataCollectionEventHandler(service).process(message);
        assertThat(service.getCounter()).isEqualTo(3);
    }

    @Test
    public void testEventMappingDeviceCommunicationFailed(){
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(ModuleConstants.FAILED_TASK_IDS, "1");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        getDataCollectionEventHandler(new CheckEventTypeServiceMock(DeviceCommunicationFailureEvent.class)).process(message);
    }

    @Test
    public void testEventSplittingDeviceCommunicationFailed(){
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(ModuleConstants.FAILED_TASK_IDS, "1, 17, 56, 57");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventCountServiceMock service = new CheckEventCountServiceMock();
        getDataCollectionEventHandler(service).process(message);
        assertThat(service.getCounter()).isEqualTo(4);
    }

    @Test
    public void testEventSplittingSeveralEvents(){
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/COMPLETION");
        messageMap.put(ModuleConstants.SKIPPED_TASK_IDS, "2,41,");
        messageMap.put(ModuleConstants.FAILED_TASK_IDS, " 1 , 17 ,  , 56 ");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        CheckEventCountServiceMock service = new CheckEventCountServiceMock();
        getDataCollectionEventHandler(service).process(message);
        assertThat(service.getCounter()).isEqualTo(5);
    }

    @Test
    public void testEventMappingUnableToConnect(){
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/connectiontask/FAILURE");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        getDataCollectionEventHandler(new CheckEventTypeServiceMock(UnableToConnectEvent.class)).process(message);
    }

    @Test
    public void testEventMappingUnknownInboundDevice(){
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        getDataCollectionEventHandler(new CheckEventTypeServiceMock(UnknownInboundDeviceEvent.class)).process(message);
    }

    @Test
    public void testEventMappingUnknownOutboundDevice(){
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        getDataCollectionEventHandler(new CheckEventTypeServiceMock(UnknownOutboundDeviceEvent.class)).process(message);
    }

    private MessageHandler getDataCollectionEventHandler(IssueCreationService issueCreationService) {
        DeviceService deviceService = mockDeviceService();
        MeteringService meteringService = mockMeteringService();
        DataCollectionEventHandlerFactory handlerFactory = getInjector().getInstance(DataCollectionEventHandlerFactory.class);
        handlerFactory.setIssueCreationService(issueCreationService);
        handlerFactory.setDeviceService(deviceService);
        handlerFactory.setMeteringService(meteringService);
        return handlerFactory.newMessageHandler();
    }

    private MeteringService mockMeteringService() {
        MeteringService meteringService = mock(MeteringService.class);
        Meter meter = mock(Meter.class);
        Query<Meter> meterQuery = mock(Query.class);
        when(meteringService.getMeterQuery()).thenReturn(meterQuery);
        when(meterQuery.select(Matchers.any(Condition.class))).thenReturn(Collections.singletonList(meter));
        return meteringService;
    }

    private DeviceService mockDeviceService() {
        DeviceService deviceDataService = mock(DeviceService.class);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceDataService.findDeviceById(1)).thenReturn(device);
        return deviceDataService;
    }

    protected class CheckEventTypeServiceMock extends IssueCreationServiceImpl {
        private Class<? extends IssueEvent> expectedClass;

        public CheckEventTypeServiceMock(Class<? extends IssueEvent> expectedClass){
            this.expectedClass = expectedClass;
        }

        @Override
        public void dispatchCreationEvent(List<IssueEvent> events){
            for (IssueEvent event : events) {
                assertThat(event.getClass()).isEqualTo(expectedClass);
            }
        }
    }

    protected class CheckEventCountServiceMock extends IssueCreationServiceImpl {
        private int counter = 0;

        @Override
        public void dispatchCreationEvent(List<IssueEvent> events){
            counter = events.size();
        }

        public int getCounter() {
            return counter;
        }
    }
}
