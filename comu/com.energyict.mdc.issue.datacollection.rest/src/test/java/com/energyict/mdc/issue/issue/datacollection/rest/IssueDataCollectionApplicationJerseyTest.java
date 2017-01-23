package com.energyict.mdc.issue.issue.datacollection.rest;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.IssueDataCollectionApplication;

import javax.ws.rs.core.Application;
import java.time.Instant;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueDataCollectionApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    RestQueryService restQueryService;
    @Mock
    UserService userService;
    @Mock
    IssueService issueService;
    @Mock
    IssueDataCollectionService issueDataCollectionService;
    @Mock
    IssueActionService issueActionService;
    @Mock
    MeteringService meteringService;
    @Mock
    DeviceService deviceService;
    @Mock
    DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    MessageService messageService;
    @Mock
    AppService appService;
    @Mock
    JsonService jsonService;
    @Mock
    CommunicationTaskService communicationTaskService;
    @Mock
    BpmService bpmService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;

    @Override
    protected Application getApplication() {
        IssueDataCollectionApplication application = new IssueDataCollectionApplication();
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setUserService(userService);
        application.setBpmService(bpmService);
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        application.setIssueService(issueService);
        application.setIssueDataCollectionService(issueDataCollectionService);
        application.setMeteringService(meteringService);
        application.setNlsService(nlsService);
        when(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.REST)).thenReturn(thesaurus);
        application.setDeviceService(deviceService);
        application.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        application.setMessageService(messageService);
        application.setAppService(appService);
        application.setJsonService(jsonService);
        application.setCommunicationTaskService(communicationTaskService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        return application;
    }

    protected IssueStatus mockStatus(String key, String name, boolean isFinal) {
        IssueStatus status = mock(IssueStatus.class);
        when(status.isHistorical()).thenReturn(isFinal);
        when(status.getName()).thenReturn(name);
        when(status.getKey()).thenReturn(key);
        return status;
    }

    protected IssueStatus getDefaultStatus() {
        return mockStatus("1", "open", false);
    }

    protected IssueType mockIssueType(String key, String name) {
        IssueType issueType = mock(IssueType.class);
        when(issueType.getKey()).thenReturn(key);
        when(issueType.getName()).thenReturn(name);
        return issueType;
    }

    protected IssueType getDefaultIssueType() {
        return mockIssueType("datacollection", "Data Collection");
    }

    protected IssueReason mockReason(String key, String name, IssueType issueType) {
        IssueReason reason = mock(IssueReason.class);
        when(reason.getKey()).thenReturn(key);
        when(reason.getName()).thenReturn(name);
        when(reason.getIssueType()).thenReturn(issueType);
        return reason;
    }

    protected IssueReason getDefaultReason() {
        return mockReason("1", "Reason", getDefaultIssueType());
    }

    protected Meter mockDevice(long id, String name) {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(id);
        when(meter.getName()).thenReturn(name);
        when(meter.getAmrId()).thenReturn(String.valueOf(id));
        when(meter.getSerialNumber()).thenReturn("0.0.0.0.0.0.0.0");
        Optional<? extends MeterActivation> optionalMA = Optional.empty();
        doReturn(optionalMA).when(meter).getCurrentMeterActivation();
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        return meter;
    }

    protected Meter getDefaultDevice() {
        return mockDevice(1, "DefaultDevice");
    }

    protected IssueAssignee mockAssignee(long userId, String userName, long workGroupId, String workGroupName){
        IssueAssignee assignee = mock(IssueAssignee.class);
        User user = mock(User.class);
        WorkGroup workGroup = mock(WorkGroup.class);
        when(workGroup.getId()).thenReturn(workGroupId);
        when(workGroup.getName()).thenReturn(workGroupName);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(userName);
        when(assignee.getUser()).thenReturn(user);
        when(assignee.getWorkGroup()).thenReturn(workGroup);
        return assignee;
    }

    protected IssueAssignee getDefaultAssignee() {
        return mockAssignee(1L, "Admin", 1L, "WorkGroup");
    }

    protected OpenIssueDataCollection getDefaultIssue() {
        return mockIssue(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDefaultDevice());
    }

    protected OpenIssueDataCollection mockIssue(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, Meter meter) {
        OpenIssueDataCollection issue = mock(OpenIssueDataCollection.class);
        when(issue.getId()).thenReturn(id);
        when(issue.getReason()).thenReturn(reason);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getAssignee()).thenReturn(assingee);
        when(issue.getDevice()).thenReturn(meter);
        when(issue.getCreateTime()).thenReturn(Instant.EPOCH);
        when(issue.getModTime()).thenReturn(Instant.EPOCH);
        when(issue.getVersion()).thenReturn(1L);
        when(issue.getPriority()).thenReturn(Priority.DEFAULT);
        return issue;
    }
 }
