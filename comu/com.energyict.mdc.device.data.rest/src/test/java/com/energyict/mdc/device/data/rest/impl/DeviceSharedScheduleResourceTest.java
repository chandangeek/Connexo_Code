/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.users.User;

import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;

import com.google.common.collect.ImmutableList;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceSharedScheduleResourceTest extends DeviceDataRestApplicationJerseyTest {

    private User user;
    
    @Override
    public void setupMocks() {
        super.setupMocks();
        user = mock(User.class);
        when(securityContext.getUserPrincipal()).thenReturn(user);
    }

    @Test
    public void testAddSharedSchedulesToDevice() throws Exception {
        DeviceSharedScheduleResource.ScheduleIdsInfo scheduleIdsInfo = new DeviceSharedScheduleResource.ScheduleIdsInfo();
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("XAF")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("XAF", 1L)).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(1L, 1L)).thenReturn(Optional.of(deviceConfiguration));
        ComSchedule schedule33 = mock(ComSchedule.class);
        ComTask comTask = mock(ComTask.class);
        when(schedule33.getComTasks()).thenReturn(ImmutableList.of(comTask));
        preparePrivileges(comTask, user);
        ComSchedule schedule44 = mock(ComSchedule.class);
        when(schedulingService.findSchedule(33L)).thenReturn(Optional.of(schedule33));
        when(schedulingService.findSchedule(44L)).thenReturn(Optional.of(schedule44));
        scheduleIdsInfo.scheduleIds = Arrays.asList(new DeviceSharedScheduleResource.ScheduleIdAndVersion(33L, 1), new DeviceSharedScheduleResource.ScheduleIdAndVersion(44L, 1));
        when(schedulingService.findAndLockComScheduleByIdAndVersion(33L, 1)).thenReturn(Optional.of(schedule33));
        when(schedulingService.findAndLockComScheduleByIdAndVersion(44L, 1)).thenReturn(Optional.of(schedule44));
        scheduleIdsInfo.device = new DeviceInfo();
        scheduleIdsInfo.device.name = "XAF";
        scheduleIdsInfo.device.version = 1L;
        scheduleIdsInfo.device.parent = new VersionInfo<>(1L, 1L);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(mock(ComTask.class));
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        when(comTaskEnablement2.getComTask()).thenReturn(mock(ComTask.class));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement, comTaskEnablement2));

        ComTaskExecutionBuilder builder = mock(ComTaskExecutionBuilder.class);
        when(device.newScheduledComTaskExecution(schedule33)).thenReturn(builder);
        when(device.newScheduledComTaskExecution(schedule44)).thenReturn(builder);
        Response response = target("/devices/XAF/sharedschedules").request().put(Entity.json(scheduleIdsInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }



    @Test
    public void testAddSharedSchedulesToDeviceBadVersion() throws Exception {
        DeviceSharedScheduleResource.ScheduleIdsInfo scheduleIdsInfo = new DeviceSharedScheduleResource.ScheduleIdsInfo();
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("XAF")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("XAF", 1L)).thenReturn(Optional.empty());
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(1L, 1L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfiguration));
        scheduleIdsInfo.scheduleIds = Arrays.asList(new DeviceSharedScheduleResource.ScheduleIdAndVersion(33L, 1), new DeviceSharedScheduleResource.ScheduleIdAndVersion(44L, 1));
        scheduleIdsInfo.device = new DeviceInfo();
        scheduleIdsInfo.device.name = "XAF";
        scheduleIdsInfo.device.version = 1L;
        scheduleIdsInfo.device.parent = new VersionInfo<>(1L, 1L);

        Response response = target("/devices/XAF/sharedschedules").request().put(Entity.json(scheduleIdsInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(device, never()).save();
    }

}
