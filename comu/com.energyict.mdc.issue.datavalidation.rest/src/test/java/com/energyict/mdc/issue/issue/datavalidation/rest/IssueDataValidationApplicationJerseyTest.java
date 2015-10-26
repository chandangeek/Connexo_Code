package com.energyict.mdc.issue.issue.datavalidation.rest;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.rest.impl.IssueDataValidationApplication;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.ws.rs.core.Application;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueDataValidationApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    UserService userService;
    @Mock
    IssueService issueService;
    @Mock
    IssueDataValidationService issueDataValidationService;
    @Mock
    IssueActionService issueActionService;
    @Mock
    MeteringService meteringService;
    @Mock
    DeviceService deviceService;

    @Override
    protected Application getApplication() {
        IssueDataValidationApplication application = new IssueDataValidationApplication();
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        application.setIssueService(issueService);
        application.setIssueDataValidationService(issueDataValidationService);
        application.setMeteringService(meteringService);
        application.setUserService(userService);
        application.setTransactionService(transactionService);
        application.setDeviceService(deviceService);
        application.setNlsService(nlsService);
        return application;
    }

    protected IssueStatus mockStatus(String key, String name, boolean isFinal) {
        IssueStatus status = mock(IssueStatus.class);
        when(status.isHistorical()).thenReturn(isFinal);
        when(status.getName()).thenReturn(name);
        when(status.getKey()).thenReturn(key);
        when(issueService.findStatus(key)).thenReturn(Optional.of(status));
        return status;
    }

    protected IssueStatus getDefaultStatus() {
        return mockStatus("1", "open", false);
    }

    protected IssueType mockIssueType(String key, String name) {
        IssueType issueType = mock(IssueType.class);
        when(issueType.getKey()).thenReturn(key);
        when(issueType.getName()).thenReturn(name);
        when(issueService.findIssueType(key)).thenReturn(Optional.of(issueType));
        return issueType;
    }

    protected IssueType getDefaultIssueType() {
        return mockIssueType("datavalidation", "Data Validation");
    }

    protected IssueReason mockReason(String key, String name, IssueType issueType) {
        IssueReason reason = mock(IssueReason.class);
        when(reason.getKey()).thenReturn(key);
        when(reason.getName()).thenReturn(name);
        when(reason.getIssueType()).thenReturn(issueType);
        when(issueService.findReason(key)).thenReturn(Optional.of(reason));
        return reason;
    }

    protected IssueReason getDefaultReason() {
        return mockReason("1", "Reason", getDefaultIssueType());
    }

    protected Meter mockDevice(long id, String mrid) {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(id);
        when(meter.getMRID()).thenReturn(mrid);
        when(meter.getAmrId()).thenReturn(String.valueOf(id));
        Optional<? extends MeterActivation> optionalMA = Optional.empty();
        doReturn(optionalMA).when(meter).getCurrentMeterActivation();
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        when(meteringService.findEndDevice(id)).thenReturn(Optional.of(meter));
        when(meteringService.findEndDevice(mrid)).thenReturn(Optional.of(meter));
        return meter;
    }

    protected Meter getDefaultDevice() {
        return mockDevice(1, "0.0.0.0.0.0.0.0");
    }

    protected IssueAssignee mockAssignee(long id, String name, String type) {
        IssueAssignee assignee = mock(IssueAssignee.class);
        User user = mockUser(id, name);
        when(assignee.getId()).thenReturn(id);
        when(assignee.getName()).thenReturn(name);
        when(assignee.getType()).thenReturn(type);
        return assignee;
    }

    protected IssueAssignee getDefaultAssignee() {
        return mockAssignee(1, "Admin", IssueAssignee.Types.USER);
    }

    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(userService.getUser(id)).thenReturn(Optional.of(user));
        return user;
    }

    protected User getDefaultUser() {
        return mockUser(1, "Admin");
    }

    protected IssueAction mockIssueAction(String name) {
        IssueAction action = mock(IssueAction.class);
        when(action.getDisplayName()).thenReturn(name);
        when(action.getPropertySpecs()).thenReturn(Collections.emptyList());
        return action;
    }

    protected IssueActionType mockIssueActionType(long id, String name, IssueType issueType) {
        IssueActionType type = mock(IssueActionType.class);
        IssueAction action = mockIssueAction(name);
        when(type.getId()).thenReturn(id);
        when(type.createIssueAction()).thenReturn(Optional.of(action));
        when(type.getIssueType()).thenReturn(issueType);
        when(issueActionService.findActionType(id)).thenReturn(Optional.of(type));
        return type;
    }

    protected IssueActionType getDefaultIssueActionType() {
        IssueType issueType = getDefaultIssueType();
        return mockIssueActionType(1, "send", issueType);
    }

    protected OpenIssueDataValidation getDefaultIssue() {
        return mockIssue(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDefaultDevice());
    }

    protected OpenIssueDataValidation mockIssue(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, Meter meter) {
        OpenIssueDataValidation issue = mock(OpenIssueDataValidation.class);
        when(issue.getId()).thenReturn(id);
        when(issue.getReason()).thenReturn(reason);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getAssignee()).thenReturn(assingee);
        when(issue.getDevice()).thenReturn(meter);
        when(issue.getCreateTime()).thenReturn(Instant.EPOCH);
        when(issue.getModTime()).thenReturn(Instant.EPOCH);
        when(issue.getVersion()).thenReturn(1L);
        return issue;
    }

    protected IssueComment mockComment(long id, String text, User user) {
        IssueComment comment = mock(IssueComment.class);
        when(comment.getId()).thenReturn(id);
        when(comment.getComment()).thenReturn(text);
        when(comment.getCreateTime()).thenReturn(Instant.EPOCH);
        when(comment.getVersion()).thenReturn(1L);
        when(comment.getUser()).thenReturn(user);
        return comment;
    }
}
