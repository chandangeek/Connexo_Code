package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceSharedScheduleResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testAddSharedSchedulesToDevice() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("XAF")).thenReturn(Optional.of(device));
        ComSchedule schedule33 = mock(ComSchedule.class);
        ComSchedule schedule44 = mock(ComSchedule.class);
        when(schedulingService.findSchedule(33L)).thenReturn(Optional.of(schedule33));
        when(schedulingService.findSchedule(44L)).thenReturn(Optional.of(schedule44));
        List<Long> ids = Arrays.asList(33L,44L);
        ComTaskExecutionBuilder builder = mock(ComTaskExecutionBuilder.class);
        when(device.newScheduledComTaskExecution(schedule33)).thenReturn(builder);
        when(device.newScheduledComTaskExecution(schedule44)).thenReturn(builder);
        Response response = target("/devices/XAF/sharedschedules").request().put(Entity.json(ids));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(device).save();
    }

}
