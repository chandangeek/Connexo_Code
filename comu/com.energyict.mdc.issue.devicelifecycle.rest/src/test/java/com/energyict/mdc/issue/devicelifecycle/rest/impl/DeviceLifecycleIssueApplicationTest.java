/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.rest.impl;


import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.devicelifecycle.FailedTransition;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;

import javax.ws.rs.core.Application;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class DeviceLifecycleIssueApplicationTest extends FelixRestApplicationJerseyTest {
    @Mock
    RestQueryService restQueryService;
    @Mock
    UserService userService;
    @Mock
    IssueService issueService;
    @Mock
    IssueDeviceLifecycleService issueDeviceLifecycleService;
    @Mock
    IssueActionService issueActionService;
    @Mock
    MeteringService meteringService;
    @Mock
    DeviceService deviceService;
    @Mock
    DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    JsonService jsonService;
    @Mock
    CommunicationTaskService communicationTaskService;
    @Mock
    LocationService locationService;


    @Override
    protected Application getApplication() {
        IssueDeviceLifecycleApplication application = new IssueDeviceLifecycleApplication();
        application.setTransactionService(transactionService);
        application.setUserService(userService);
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        application.setIssueService(issueService);
        application.setIssueDeviceLifecycleService(issueDeviceLifecycleService);
        application.setMeteringService(meteringService);
        application.setNlsService(nlsService);
        when(nlsService.getThesaurus(IssueDeviceLifecycleService.COMPONENT_NAME, Layer.REST)).thenReturn(thesaurus);
        application.setDeviceService(deviceService);
        application.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        application.setLocationService(locationService);
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
        return mockIssueType("devicelifecycle", "Device lifecycle");
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

    protected IssueAssignee mockAssignee(long userId, String userName, long workGroupId, String workGroupName) {
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

    protected OpenIssueDeviceLifecycle getDefaultIssue() {
        return mockIssue(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDefaultDevice(), getFailedTransition());
    }
    protected List <FailedTransition> getFailedTransition() {
        return mockTransition("FailedTransition", 1L, 1L,1L,1L);
    }

    private List<FailedTransition> mockTransition(String cause, long transitionId, long deviceLifecycleId, long fromId, long toId) {
        List<FailedTransition> failedTransitionList = new ArrayList<>();
        FailedTransition failedTransition =mock(FailedTransition.class);
        StateTransition stateTransition = mock(StateTransition.class);
        when(stateTransition.getId()).thenReturn(transitionId);
        DeviceLifeCycle deviceLifeCycle = mock(DeviceLifeCycle.class);
        when(deviceLifeCycle.getId()).thenReturn(deviceLifecycleId);
        State stateFrom = mock(State.class);
        when(stateFrom.getId()).thenReturn(fromId);
        State stateTo = mock(State.class);
        when(stateTo.getId()).thenReturn(toId);
        when(failedTransition.getCause()).thenReturn(cause);
        when(failedTransition.getTransition()).thenReturn(stateTransition);
        when(failedTransition.getLifecycle()).thenReturn(deviceLifeCycle);
        when(failedTransition.getFrom()).thenReturn(stateFrom);
        when(failedTransition.getTo()).thenReturn(stateTo);
        failedTransitionList.add(failedTransition);
        return failedTransitionList;
    }

    protected OpenIssueDeviceLifecycle mockIssue(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, Meter meter, List<FailedTransition> failedTransition) {
        OpenIssueDeviceLifecycle issue = mock(OpenIssueDeviceLifecycle.class, RETURNS_DEEP_STUBS);
        when(issue.getId()).thenReturn(id);
        when(issue.getReason()).thenReturn(reason);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getAssignee()).thenReturn(assingee);
        when(issue.getDevice()).thenReturn(meter);
        when(issue.getCreateTime()).thenReturn(Instant.EPOCH);
        when(issue.getCreateDateTime()).thenReturn(Instant.EPOCH);
        when(issue.getModTime()).thenReturn(Instant.EPOCH);
        when(issue.getVersion()).thenReturn(1L);
        when(issue.getPriority()).thenReturn(Priority.DEFAULT);
        when(issue.getSnoozeDateTime()).thenReturn(Optional.empty());
        when(issue.getDevice().getLocation()).thenReturn(Optional.empty());
        when(issue.getFailedTransitions()).thenReturn(failedTransition);
        return issue;
    }
}
