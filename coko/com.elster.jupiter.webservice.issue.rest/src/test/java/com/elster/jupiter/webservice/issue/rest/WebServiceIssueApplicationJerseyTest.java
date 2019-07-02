/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.WebServiceOpenIssue;
import com.elster.jupiter.webservice.issue.rest.impl.WebServiceIssueApplication;

import javax.ws.rs.core.Application;
import java.time.Instant;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class WebServiceIssueApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    UserService userService;
    @Mock
    IssueService issueService;
    @Mock
    WebServiceIssueService webServiceIssueService;
    @Mock
    IssueActionService issueActionService;

    @Override
    protected Application getApplication() {
        WebServiceIssueApplication application = new WebServiceIssueApplication();
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        application.setIssueService(issueService);
        application.setWebServiceIssueService(webServiceIssueService);
        application.setUserService(userService);
        application.setTransactionService(transactionService);
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
        return mockIssueType("webservice", "Web service");
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

    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(userService.getUser(id)).thenReturn(Optional.of(user));
        return user;
    }

    protected WebServiceOpenIssue getDefaultIssue() {
        return mockIssue(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDefaultOccurrence());
    }

    protected WebServiceOpenIssue mockIssue(long id, IssueReason reason, IssueStatus status, IssueAssignee assignee, WebServiceCallOccurrence occurrence) {
        WebServiceOpenIssue issue = mock(WebServiceOpenIssue.class,RETURNS_DEEP_STUBS);
        when(issue.getId()).thenReturn(id);
        when(issue.getReason()).thenReturn(reason);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getAssignee()).thenReturn(assignee);
        when(issue.getDevice()).thenReturn(null);
        when(issue.getCreateTime()).thenReturn(Instant.EPOCH);
        when(issue.getCreateDateTime()).thenReturn(Instant.EPOCH);
        when(issue.getModTime()).thenReturn(Instant.EPOCH);
        when(issue.getVersion()).thenReturn(1L);
        when(issue.getPriority()).thenReturn(Priority.DEFAULT);
        when(issue.getSnoozeDateTime()).thenReturn(Optional.empty());
        when(issue.getWebServiceCallOccurrence()).thenReturn(occurrence);
        return issue;
    }

    protected WebServiceCallOccurrence getDefaultOccurrence() {
        return mockOccurrence(33);
    }

    protected WebServiceCallOccurrence mockOccurrence(long id) {
        WebServiceCallOccurrence occurrence = mock(WebServiceCallOccurrence.class);
        when(occurrence.getId()).thenReturn(id);
        return occurrence;
    }
}
