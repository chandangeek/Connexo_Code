package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import javax.ws.rs.core.Application;
import org.mockito.Mock;

/**
 * Created by bvn on 9/19/14.
 */
public class DashboardApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    StatusService statusService;
    @Mock
    ConnectionTaskService connectionTaskService;
    @Mock
    CommunicationTaskService communicationTaskService;
    @Mock
    DeviceService deviceService;
    @Mock
    SchedulingService schedulingService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    TaskService taskService;
    @Mock
    IssueDataCollectionService issueDataCollectionService;
    @Mock
    IssueService issueService;
    @Mock
    DashboardService dashboardService;
    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    ProtocolPluggableService protocolPluggableService;
    @Mock
    DataCollectionKpiService dataCollectionKpiService;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    FavoritesService favoritesService;
    @Mock
    MessageService messageService;
    @Mock
    JsonService jsonService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        DashboardApplication dashboardApplication = new DashboardApplication();
        dashboardApplication.setDashboardService(dashboardService);
        dashboardApplication.setDeviceConfigurationService(deviceConfigurationService);
        dashboardApplication.setConnectionTaskService(connectionTaskService);
        dashboardApplication.setCommunicationTaskService(communicationTaskService);
        dashboardApplication.setDeviceService(deviceService);
        dashboardApplication.setEngineConfigurationService(engineConfigurationService);
        dashboardApplication.setNlsService(nlsService);
        dashboardApplication.setProtocolPluggableService(protocolPluggableService);
        dashboardApplication.setSchedulingService(schedulingService);
        dashboardApplication.setStatusService(statusService);
        dashboardApplication.setTaskService(taskService);
        dashboardApplication.setIssueDataCollectionService(issueDataCollectionService);
        dashboardApplication.setIssueService(issueService);
        dashboardApplication.setTransactionService(transactionService);
        dashboardApplication.setDataCollectionKpiService(dataCollectionKpiService);
        dashboardApplication.setMeteringGroupsService(meteringGroupsService);
        dashboardApplication.setClock(Clock.fixed(LocalDateTime.of(2014, 10, 1, 16, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault()));
        dashboardApplication.setFavoritesService(favoritesService);
        dashboardApplication.setMessageService(messageService);
        dashboardApplication.setJsonService(jsonService);
        return dashboardApplication;
    }
}
