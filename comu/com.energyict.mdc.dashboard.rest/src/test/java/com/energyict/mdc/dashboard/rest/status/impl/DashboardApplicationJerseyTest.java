package com.energyict.mdc.dashboard.rest.status.impl;

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
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.json.JsonService;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    @Mock
    AppService appService;
    @Mock
    FirmwareService firmwareService;

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
        dashboardApplication.setAppService(appService);
        dashboardApplication.setFirmwareService(firmwareService);
        return dashboardApplication;
    }

    AppServer mockAppServers(String ...name) {
        AppServer appServer = mock(AppServer.class);
        List<SubscriberExecutionSpec> execSpecs = new ArrayList<>();
        for (String specName: name) {
            SubscriberExecutionSpec subscriberExecutionSpec = mock(SubscriberExecutionSpec.class);
            SubscriberSpec spec = mock(SubscriberSpec.class);
            when(subscriberExecutionSpec.getSubscriberSpec()).thenReturn(spec);
            DestinationSpec destinationSpec = mock(DestinationSpec.class);
            when(spec.getDestination()).thenReturn(destinationSpec);
            when(destinationSpec.getName()).thenReturn(specName);
            when(destinationSpec.isActive()).thenReturn(true);
            List<SubscriberSpec> list = mock(List.class);
            when(list.isEmpty()).thenReturn(false);
            when(destinationSpec.getSubscribers()).thenReturn(list);
            execSpecs.add(subscriberExecutionSpec);
        }
        doReturn(execSpecs).when(appServer).getSubscriberExecutionSpecs();
        when(appServer.isActive()).thenReturn(true);
        when(appService.findAppServers()).thenReturn(Arrays.asList(appServer));
        return appServer;
    }

}
