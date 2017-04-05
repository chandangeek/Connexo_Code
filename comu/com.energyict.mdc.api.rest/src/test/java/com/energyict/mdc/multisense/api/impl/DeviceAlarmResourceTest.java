/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

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

import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;

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

public class DeviceAlarmResourceTest extends MultisensePublicApiJerseyTest{

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetAllOpenAlarms() throws Exception {
        IssueType issueType = getDefaultIssueType();
        Finder finder = mockFinder(Collections.emptyList());
        when(deviceAlarmService.findAlarms(anyObject())).thenReturn(finder);
        when(issueService.findIssueType(anyString())).thenReturn(Optional.of(issueType));
        Response response = target("alarms").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testAlarmFields() throws Exception {
        Response response = target("alarms").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(13);
        assertThat(model.<List<String>>get("$"))
                .containsOnly("id", "title", "alarmId", "reason", "status", "priority", "priorityValue", "userAssignee", "workGroupAssignee", "device", "dueDate", "creationDate", "version");
    }

    @Test
    public void testAddComment() {
        OpenDeviceAlarm alarm = getDefaultOpenDeviceAlarm();
        doReturn(Optional.of(alarm)).when(deviceAlarmService).findAlarm(1L);
        User user = getDefaultUser();
        IssueComment comment = mockComment(1L, "Comment", user);
        when(alarm.addComment(anyString(),anyObject())).thenReturn(Optional.of(comment));
        Map<String, String> params = new HashMap<>(1);
        params.put("comment", "Comment");
        Entity<Map<String, String>> json = Entity.json(params);

        Response response = target("/alarms/1/comment").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCloseAlarm() {
        OpenDeviceAlarm openAlarm = getDefaultOpenDeviceAlarm();
        HistoricalDeviceAlarm closedAlarm = getDefaultClosedDeviceAlarm();
        User user = getDefaultUser();
        IssueStatus closedStatus = getDefaultClosedStatus();
        doReturn(Optional.of(openAlarm)).when(deviceAlarmService).findAlarm(1L);
        IssueComment comment = mockComment(1, "closing Alarm", user);
        when(openAlarm.addComment(Matchers.anyString(), Matchers.any())).thenReturn(Optional.of(comment));
        when(comment.getCreateTime()).thenReturn(Instant.now());
        when(comment.getUser()).thenReturn(user);
        doReturn(Optional.of(openAlarm)).when(deviceAlarmService).findAndLockDeviceAlarmByIdAndVersion(1L, 1L);
        doReturn(closedAlarm).when(openAlarm).close(closedStatus);
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
        Response response = target("/alarms/1/close").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}