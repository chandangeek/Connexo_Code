package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.scheduling.model.ComSchedule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceSharedScheduleResourceTest extends DeviceDataRestApplicationJerseyTest {


    @Test
    public void testAddSharedSchedulesToDevice() throws Exception {
        DeviceSharedScheduleResource.ScheduleIdsInfo scheduleIdsInfo = new DeviceSharedScheduleResource.ScheduleIdsInfo();
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("XAF")).thenReturn(Optional.of(device));
        ComSchedule schedule33 = mock(ComSchedule.class);
        ComSchedule schedule44 = mock(ComSchedule.class);
        when(schedulingService.findSchedule(33L)).thenReturn(Optional.of(schedule33));
        when(schedulingService.findSchedule(44L)).thenReturn(Optional.of(schedule44));
        scheduleIdsInfo.scheduleIds = Arrays.asList(33L,44L);
        ComTaskExecutionBuilder builder = mock(ComTaskExecutionBuilder.class);
        when(device.newScheduledComTaskExecution(schedule33)).thenReturn(builder);
        when(device.newScheduledComTaskExecution(schedule44)).thenReturn(builder);
        Response response = target("/devices/XAF/sharedschedules").request().put(Entity.json(scheduleIdsInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(device).save();
    }

}
