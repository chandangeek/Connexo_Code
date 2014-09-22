package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import javax.ws.rs.core.Application;
import org.mockito.Mock;

/**
 * Created by bvn on 9/19/14.
 */
public class DashboardApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    StatusService statusService;
    @Mock
    DeviceDataService deviceDataService;
    @Mock
    SchedulingService schedulingService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    TaskService taskService;
    @Mock
    DashboardService dashboardService;
    @Mock
    EngineModelService engineModelService;
    @Mock
    ProtocolPluggableService protocolPluggableService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        DashboardApplication dashboardApplication = new DashboardApplication();
        dashboardApplication.setDashboardService(dashboardService);
        dashboardApplication.setDeviceConfigurationService(deviceConfigurationService);
        dashboardApplication.setDeviceDataService(deviceDataService);
        dashboardApplication.setEngineModelService(engineModelService);
        dashboardApplication.setNlsService(nlsService);
        dashboardApplication.setProtocolPluggableService(protocolPluggableService);
        dashboardApplication.setSchedulingService(schedulingService);
        dashboardApplication.setStatusService(statusService);
        dashboardApplication.setTaskService(taskService);
        dashboardApplication.setTransactionService(transactionService);
        return dashboardApplication;
    }
}
