package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.json.JsonService;

import javax.ws.rs.core.Application;
import java.time.Clock;

import org.mockito.Mock;

/**
 * Created by bvn on 9/19/14.
 */
public class SchedulingApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    Clock clock;
    @Mock
    SchedulingService schedulingService;
    @Mock
    DeviceService deviceService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    TaskService taskService;
    @Mock
    JsonService jsonService;
    @Mock
    MeteringService meteringService;

    @Override
    protected Application getApplication() {
        MasterSchedulingApplication masterSchedulingApplication = new MasterSchedulingApplication();
        masterSchedulingApplication.setTransactionService(transactionService);
        masterSchedulingApplication.setTaskService(taskService);
        masterSchedulingApplication.setSchedulingService(schedulingService);
        masterSchedulingApplication.setClock(clock);
        masterSchedulingApplication.setDeviceConfigurationService(deviceConfigurationService);
        masterSchedulingApplication.setJsonService(jsonService);
        masterSchedulingApplication.setDeviceService(deviceService);
        masterSchedulingApplication.setMeteringService(meteringService);
        masterSchedulingApplication.setNlsService(nlsService);
        return masterSchedulingApplication;
    }
}
