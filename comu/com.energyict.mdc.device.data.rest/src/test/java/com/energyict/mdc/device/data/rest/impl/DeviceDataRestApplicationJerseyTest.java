package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.time.Clock;

/**
 * Created by bvn on 9/19/14.
 */
public class DeviceDataRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    ConnectionTaskService connectionTaskService;
    @Mock
    DeviceService deviceService;
    @Mock
    DeviceImportService deviceImportService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    EngineModelService engineModelService;
    @Mock
    IssueService issueService;
    @Mock
    SchedulingService schedulingService;
    @Mock
    ValidationService validationService;
    @Mock
    Clock clock;
    @Mock
    MasterDataService masterDataService;
    @Mock
    JsonService jsonService;
    @Mock
    ProtocolPluggableService protocolPluggableService;
    @Mock
    MeteringGroupsService meteringGroupService;
    @Mock
    MeteringService meteringService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    TaskService taskService;
    @Mock
    CommunicationTaskService communicationTaskService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        DeviceApplication application = new DeviceApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setMasterDataService(masterDataService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setJsonService(jsonService);
        application.setProtocolPluggableService(protocolPluggableService);
        application.setClockService(clock);
        application.setConnectionTaskService(connectionTaskService);
        application.setDeviceService(deviceService);
        application.setDeviceImportService(deviceImportService);
        application.setEngineModelService(engineModelService);
        application.setIssueService(issueService);
        application.setMeteringGroupsService(meteringGroupService);
        application.setMeteringService(meteringService);
        application.setSchedulingService(schedulingService);
        application.setValidationService(validationService);
        application.setRestQueryService(restQueryService);
        application.setTaskService(taskService);
        application.setCommunicationTaskService(communicationTaskService);
        return application;
    }
}
