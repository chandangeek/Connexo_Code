package com.energyict.mdc.issue.issue.datacollection.rest;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.IssueDataCollectionApplication;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
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
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.ws.rs.core.Application;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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

    @Override
    protected Application getApplication() {
        IssueDataCollectionApplication application = new IssueDataCollectionApplication();
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setUserService(userService);
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        application.setIssueService(issueService);
        application.setIssueDataCollectionService(issueDataCollectionService);
        application.setMeteringService(meteringService);
        application.setNlsService(nlsService);
        when(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.REST)).thenReturn(thesaurus);
        application.setDeviceService(deviceService);
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
        return meter;
    }

    protected Meter getDefaultDevice() {
        return mockDevice(1, "0.0.0.0.0.0.0.0");
    }

    protected IssueAssignee mockAssignee(long id, String name, String type) {
        IssueAssignee assignee = mock(IssueAssignee.class);
        when(assignee.getId()).thenReturn(id);
        when(assignee.getName()).thenReturn(name);
        when(assignee.getType()).thenReturn(type);
        return assignee;
    }

    protected IssueAssignee getDefaultAssignee() {
        return mockAssignee(1, "Admin", IssueAssignee.Types.USER);
    }

    protected AssignmentRule mockAssignmentRule(long id, String title, String description, long version, IssueAssignee assignee) {
        AssignmentRule rule = mock(AssignmentRule.class);
        when(rule.getId()).thenReturn(id);
        when(rule.getTitle()).thenReturn(title);
        when(rule.getDescription()).thenReturn(description);
        when(rule.getVersion()).thenReturn(version);
        when(rule.getAssignee()).thenReturn(assignee);
        return rule;
    }

    protected AssignmentRule getDefaultAssignmentRule() {
        IssueAssignee assignee = getDefaultAssignee();
        return mockAssignmentRule(1, "Assignment Rule", "Description", 1, assignee);
    }

    protected CreationRuleTemplate mockCreationRuleTemplate(String uuid, String name, String description, IssueType issueType, List<PropertySpec> propertySpecs) {
        CreationRuleTemplate template = mock(CreationRuleTemplate.class);
        when(template.getName()).thenReturn(name);
        when(template.getDescription()).thenReturn(description);
        when(template.getIssueType()).thenReturn(issueType);
        when(template.getPropertySpecs()).thenReturn(propertySpecs);
        return template;
    }

    protected CreationRuleTemplate getDefaultCreationRuleTemplate() {
        IssueType issueType = getDefaultIssueType();
        return mockCreationRuleTemplate("0-1-2", "Template 1", "Description", issueType, null);
    }

    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
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

    protected IssueAction getDefaultIssueAction() {
        return mockIssueAction("Send To Inspect");
    }

    protected IssueActionType mockIssueActionType(long id, String name, IssueType issueType) {
        IssueActionType type = mock(IssueActionType.class);
        IssueAction action = mockIssueAction(name);
        when(type.getId()).thenReturn(id);
        when(type.createIssueAction()).thenReturn(Optional.of(action));
        when(type.getIssueType()).thenReturn(issueType);
        return type;
    }

    protected IssueActionType getDefaultIssueActionType() {
        IssueType issueType = getDefaultIssueType();
        return mockIssueActionType(1, "send", issueType);
    }

    protected OpenIssueDataCollection getDefaultIssue() {
        return mockIssue(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDefaultDevice());
    }

    protected CreationRule mockCreationRule(long id, String name) {
        Instant now = Instant.now();
        IssueReason reason = getDefaultReason();
        CreationRuleTemplate template = getDefaultCreationRuleTemplate();
        CreationRule rule = mock(CreationRule.class);
        when(rule.getId()).thenReturn(id);
        when(rule.getName()).thenReturn(name);
        when(rule.getComment()).thenReturn("comment");
        when(rule.getReason()).thenReturn(reason);
        when(rule.getDueInType()).thenReturn(DueInType.DAY);
        when(rule.getDueInValue()).thenReturn(5L);
        when(rule.getActions()).thenReturn(Collections.emptyList());
        when(rule.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(rule.getTemplate()).thenReturn(template);
        when(rule.getModTime()).thenReturn(now);
        when(rule.getCreateTime()).thenReturn(now);
        when(rule.getVersion()).thenReturn(2L);
        return rule;
    }

    protected CreationRule getDefaultCreationRule() {
        return mockCreationRule(1, "Rule 1");
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
