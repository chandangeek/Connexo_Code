package com.elster.jupiter.issue.tests;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.datacollection.DataCollectionEvent;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.tasks.history.TaskHistoryService;
import org.junit.Test;
import org.mockito.Matchers;
import org.osgi.service.event.EventConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataCollectionEventTest extends BaseTest {

    @Test
    public void testInit() {
        Map messageMap = new HashMap<>();
        String topic = "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE";
        messageMap.put(EventConstants.EVENT_TOPIC, topic);
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, "1");

        DeviceDataService deviceDataService = getDeviceDataService();
        MeteringService meteringService = mock(MeteringService.class);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceDataService.findDeviceById(1)).thenReturn(device);

        Meter meter = mock(Meter.class);
        Query<Meter> meterQuery = mock(Query.class);
        when(meter.getAmrId()).thenReturn("test");
        when(meteringService.getMeterQuery()).thenReturn(meterQuery);
        when(meterQuery.select(Matchers.any(Condition.class))).thenReturn(Collections.singletonList(meter));

        DataCollectionEvent event = new DataCollectionEvent(getIssueService(), meteringService, deviceDataService, mock(TaskHistoryService.class), getThesaurus(), messageMap);

        assertThat(event.getEventType()).isEqualTo(topic);
        assertThat(event.getDevice().getAmrId()).isEqualTo("test");
    }
}
