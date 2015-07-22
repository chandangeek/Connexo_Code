package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.cbo.MessageSeeds;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;
import org.junit.Before;
import org.mockito.Mock;

import javax.ws.rs.core.Application;

public class DeviceDataValidationRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    DeviceDataValidationService deviceDataValidationService;

    @Before
    public void setup() {
        //when(taskService.findComTask(anyLong())).thenReturn(Optional.empty());
        //when(taskService.findComTask(firmwareComTaskId)).thenReturn(Optional.of(firmwareComTask));
    }

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        DeviceDataValidationApplication application = new DeviceDataValidationApplication();
        application.setDeviceDataValidationService(deviceDataValidationService);
        return application;
    }
}