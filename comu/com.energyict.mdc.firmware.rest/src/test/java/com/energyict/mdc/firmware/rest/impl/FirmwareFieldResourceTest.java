/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.tasks.TaskService;

import com.jayway.jsonpath.JsonModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FirmwareFieldResourceTest extends BaseFirmwareTest {

    @Test
    public void testGetComTasks() {
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        ComTaskEnablement cte = mock(ComTaskEnablement.class);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(1L);
        when(comTask.isManualSystemTask()).thenReturn(true);
        when(comTask.getName()).thenReturn("comTask");

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("devType");
        when(deviceType.getId()).thenReturn(1L);

        List<DeviceConfiguration> configsList = new ArrayList<>();
        configsList.add(deviceConfig);
        List<ComTaskEnablement> cteList = new ArrayList<>();
        cteList.add(cte);

        when(deviceConfigurationService.findDeviceType(anyLong())).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(configsList);
        when(deviceConfig.getComTaskEnablements()).thenReturn(cteList);
        when(cte.getComTask()).thenReturn(comTask);

        String json = target("field/comtasks").queryParam("type", 1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.[0].id")).isEqualTo(((Number) comTask.getId()).intValue());
        assertThat(jsonModel.<String>get("$.[0].name")).isEqualTo(comTask.getName());
    }

    @Test
    public void testGetFirmwareUploadComTasks() {
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        ComTaskEnablement cte = mock(ComTaskEnablement.class);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(1L);
        when(comTask.isManualSystemTask()).thenReturn(true);
        when(comTask.getName()).thenReturn(TaskService.FIRMWARE_COMTASK_NAME);
        FirmwareManagementTask firmwareManagementTask = mock(FirmwareManagementTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(firmwareManagementTask));

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("devType");
        when(deviceType.getId()).thenReturn(1L);

        List<DeviceConfiguration> configsList = new ArrayList<>();
        configsList.add(deviceConfig);
        List<ComTaskEnablement> cteList = new ArrayList<>();
        cteList.add(cte);

        when(deviceConfigurationService.findDeviceType(anyLong())).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(configsList);
        when(deviceConfig.getComTaskEnablements()).thenReturn(cteList);
        when(cte.getComTask()).thenReturn(comTask);

        String json = target("/field/firmwareuploadcomtasks").queryParam("type", 1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.[0].id")).isEqualTo(((Number) comTask.getId()).intValue());
        assertThat(jsonModel.<String>get("$.[0].name")).isEqualTo(comTask.getName());
    }
}
