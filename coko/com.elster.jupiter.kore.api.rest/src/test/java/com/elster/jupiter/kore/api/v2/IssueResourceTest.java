/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.kore.api.v2.issue.IssueShortInfo;
import com.elster.jupiter.kore.api.v2.issue.IssueStatusInfo;
import com.elster.jupiter.users.User;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueResourceTest extends PlatformPublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetAllIssues() throws Exception {
        IssueType issueType = getDefaultIssueType();
        IssueFilter issueFilter = mock(IssueFilter.class);
        Finder finder = mockFinder(Collections.emptyList());
        when(issueService.newIssueFilter()).thenReturn(issueFilter);
        when(issueService.findIssues(issueFilter)).thenReturn(finder);
        when(issueService.findIssueType(anyString())).thenReturn(Optional.of(issueType));
        Response response = target("issues").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testIssueFields() throws Exception {
        Response response = target("issues").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(14);
        assertThat(model.<List<String>>get("$"))
                .containsOnly("id", "title", "issueId", "issueType", "reason", "status", "priority", "priorityValue", "userAssignee", "workGroupAssignee", "device", "dueDate", "creationDate", "version");
    }

    @Test
    public void testAddComment() {
        OpenIssue issue = getDefaultOpenIssue();
        doReturn(Optional.of(issue)).when(issueService).findIssue(1L);
        User user = getDefaultUser();
        IssueComment comment = mockComment(1L, "Comment", user);
        when(issue.addComment(anyString(),anyObject())).thenReturn(Optional.of(comment));
        Map<String, String> params = new HashMap<>(1);
        params.put("comment", "Comment");
        Entity<Map<String, String>> json = Entity.json(params);

        Response response = target("/issues/1/comment").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCloseIssue() {
        OpenIssue openIssue = getDefaultOpenIssue();
        HistoricalIssue closedIssue = getDefaultClosedIssue();
        User user = getDefaultUser();
        IssueStatus closedStatus = getDefaultClosedStatus();
        doReturn(Optional.of(openIssue)).when(issueService).findIssue(1L);
        IssueComment comment = mockComment(1, "closing Issue", user);
        when(openIssue.addComment(Matchers.anyString(), Matchers.any())).thenReturn(Optional.of(comment));
        when(comment.getCreateTime()).thenReturn(Instant.now());
        when(comment.getUser()).thenReturn(user);
        doReturn(Optional.of(openIssue)).when(issueService).findAndLockIssueByIdAndVersion(1L, 1L);
        doReturn(closedIssue).when(openIssue).close(closedStatus);
        IssueStatusInfo statusInfo = new IssueStatusInfo();
        statusInfo.id = "status.resolved";
        statusInfo.name = "Resolved";
        doReturn(Optional.of(closedStatus)).when(issueService).findStatus(statusInfo.id);
        IssueShortInfo issueShortInfo = new IssueShortInfo();
        issueShortInfo.id = 1L;
        issueShortInfo.title = "Issue to be closed";
        issueShortInfo.status = statusInfo;
        issueShortInfo.version = 1L;
        Entity<IssueShortInfo> json = Entity.json(issueShortInfo);
        Response response = target("/issues/1/close").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}