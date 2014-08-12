package com.elster.jupiter.issue.tests;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.event.DataCollectionEventHandler;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import org.junit.Test;
import org.mockito.Matchers;
import org.osgi.service.event.EventConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataCollectionEventHandlerTest extends BaseTest {
    @Test
    public void testSuccessfullProcess() {
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");

        DeviceDataService deviceDataService = getDeviceDataService();
        MeteringService meteringService = mock(MeteringService.class);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceDataService.findDeviceById(1)).thenReturn(device);

        Meter meter = mock(Meter.class);
        Query<Meter> meterQuery = mock(Query.class);
        when(meteringService.getMeterQuery()).thenReturn(meterQuery);
        when(meterQuery.select(Matchers.any(Condition.class))).thenReturn(Collections.singletonList(meter));

        String serializedMap = getJsonService().serialize(messageMap);
        Message message = getMockMessage(serializedMap);
        Boolean isProcessed = false;
        DataCollectionEventHandler handler = new DataCollectionEventHandler(getJsonService(), getIssueService(), getMockIssueCreationService(), meteringService, deviceDataService, getThesaurus());
        try {
            handler.process(message);
        } catch (DispatchCreationEventException ex) {
            assertThat(ex.getMessage()).isEqualTo("processed!");
            isProcessed = true;
        }
        assertThat(isProcessed).isTrue();
    }

    @Test
    public void testUnsuccessfullProcess() {
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "some/fake/topic");
        String serializedMap = getJsonService().serialize(messageMap);
        Message message = getMockMessage(serializedMap);
        Boolean isProcessed = false;
        DataCollectionEventHandler handler = new DataCollectionEventHandler(getJsonService(), getIssueService(), getMockIssueCreationService(), getMeteringService(), getDeviceDataService(), getThesaurus());
        try {
            handler.process(message);
        } catch (DispatchCreationEventException ex) {
            assertThat(ex.getMessage()).isEqualTo("processed!");
            isProcessed = true;
        }
        assertThat(isProcessed).isFalse();
    }
}
