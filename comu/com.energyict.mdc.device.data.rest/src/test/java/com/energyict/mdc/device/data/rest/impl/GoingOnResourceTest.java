/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.ProcessInstanceInfos;
import com.elster.jupiter.bpm.UserTaskInfo;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class GoingOnResourceTest extends DeviceDataRestApplicationJerseyTest {


    @Mock
    Device device;

    @Mock
    AmrSystem amrSystem;

    @Mock
    Meter meter;

    @Mock
    IssueFilter issueFilter;

    @Mock
    IssueStatus openStatus;

    @Mock
    IssueStatus inProgressStatus;

    @Mock
    Finder<? extends Issue> issueFinder;

    @Mock
    Finder<? extends DeviceAlarm> alarmFinder;

    @Mock
    Finder<ServiceCall> serviceCallFinder;

    @Mock
    Issue issue;

    @Mock
    IssueReason reason;

    @Mock
    IssueType issueType;

    @Mock
    CreationRule creationRule;

    @Mock
    CreationRule alarmCreationRule;

    @Mock
    DeviceAlarm deviceAlarm;

    @Mock
    IssueAssignee issueAssignee;

    @Mock
    Privilege userTaskPrivilege;

    @Mock
    Privilege issuePrivilege;

    @Mock
    Privilege alarmPrivilege;

    @Mock
    User user;


    @Before
    public void setup(){

        Map<String, List<Privilege>> appPrivileges = new HashMap<String, List<Privilege>>(){{
            put("MDC", Arrays.asList(userTaskPrivilege, issuePrivilege, alarmPrivilege));
        }};

        when(user.getName()).thenReturn("Admin");
        when(user.getId()).thenReturn(1L);
        when(user.getApplicationPrivileges()).thenReturn(appPrivileges);

        when(securityContext.getUserPrincipal()).thenReturn(user);
        when(issueAssignee.getUser()).thenReturn(user);

        when(userTaskPrivilege.getName()).thenReturn("privilege.execute.task");
        when(issuePrivilege.getName()).thenReturn("privilege.comment.issue");
        when(alarmPrivilege.getName()).thenReturn("privilege.action.alarm");

        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.of(device));
        when(meteringService.findAmrSystem(anyLong())).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter(anyString())).thenReturn(Optional.of(meter));

        when(issueService.newIssueFilter()).thenReturn(issueFilter);
        when(issueService.findStatus(IssueStatus.OPEN)).thenReturn(Optional.of(openStatus));
        when(issueService.findStatus(IssueStatus.IN_PROGRESS)).thenReturn(Optional.of(inProgressStatus));
        doReturn(issueFinder).when(issueService).findIssues(any(IssueFilter.class), anyVararg());
        doReturn(issueFinder).when(issueFinder).sorted(anyString(), anyBoolean());
        doReturn(Collections.singletonList(issue)).when(issueFinder).find();
        doReturn(Collections.singletonList(issue).stream()).when(issueFinder).stream();
        when(issue.getReason()).thenReturn(reason);
        when(reason.getIssueType()).thenReturn(issueType);
        when(issueType.getKey()).thenReturn("issueType.key");
        when(issueType.getName()).thenReturn("issueType.name");
        when(issue.getId()).thenReturn(1L);
        when(issue.getIssueId()).thenReturn("DCI-1");
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getStatus()).thenReturn(openStatus);
        when(issue.getRule()).thenReturn(Optional.of(creationRule));
        when(issue.getAssignee()).thenReturn(issueAssignee);
        when(openStatus.getName()).thenReturn(IssueStatus.OPEN);

        //when(serviceCallService.findServiceCalls(any(), any())).thenReturn(new HashSet<>());
        when(serviceCallService.getServiceCallFinder(any())).thenReturn(serviceCallFinder);
        doReturn(Collections.emptyList().stream()).when(serviceCallFinder).stream();

        doReturn(alarmFinder).when(deviceAlarmService).findAlarms(any(DeviceAlarmFilter.class), anyVararg());
        doReturn(Collections.singletonList(deviceAlarm).stream()).when(alarmFinder).stream();
        doReturn(alarmFinder).when(alarmFinder).sorted(anyString(), anyBoolean());
        ProcessInstanceInfos infos = new ProcessInstanceInfos();
        when(bpmService.getRunningProcesses(any(), any(), any())).thenReturn(infos);
        when(deviceAlarm.getId()).thenReturn(1L);
        when(deviceAlarm.getIssueId()).thenReturn("ALM-1");
        when(deviceAlarm.getReason()).thenReturn(reason);
        when(deviceAlarm.getDueDate()).thenReturn(null);
        when(deviceAlarm.getStatus()).thenReturn(openStatus);
        when(deviceAlarm.getAssignee()).thenReturn(issueAssignee);
        when(deviceAlarm.getRule()).thenReturn(Optional.of(alarmCreationRule));
    }

    @Test
    public void testWhatsGoingOnReturnsIssue(){
        Map<String, Object> response = target("/devices/SPE01/whatsgoingon").request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);
        List<?> data = (List<?>) response.get("goingsOn");
        assertThat(data).hasSize(2);
        Map<?, ?> issueMap = (Map<?, ?>) data.get(0);
        assertThat(issueMap.get("type")).isEqualTo("issue");
        assertThat(issueMap.get("id")).isEqualTo("DCI-1");
        assertThat(issueMap.get("isMyWorkGroup")).isEqualTo(false);
        assertThat(issueMap.get("userAssigneeIsCurrentUser")).isEqualTo(true);
        assertThat(issueMap.get("status")).isEqualTo("status.open");
        assertThat(issueMap.get("issueType")).isEqualTo("issueType.key");
        assertThat(issueMap.get("userAssignee")).isEqualTo("Admin");
    }

    @Test
    public void testWhatsGoingOnReturnsAlarm(){
        Map<String, Object> response = target("/devices/SPE01/whatsgoingon").request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);
        List<?> data = (List<?>) response.get("goingsOn");
        assertThat(data).hasSize(2);
        Map<?, ?> alarmMap = (Map<?, ?>) data.get(1);
        assertThat(alarmMap.get("type")).isEqualTo("alarm");
        assertThat(alarmMap.get("id")).isEqualTo("ALM-1");
        assertThat(alarmMap.get("isMyWorkGroup")).isEqualTo(false);
        assertThat(alarmMap.get("userAssigneeIsCurrentUser")).isEqualTo(true);
        assertThat(alarmMap.get("status")).isEqualTo("status.open");
    }

    @Test
    public void testWhatsGoingOnReturnsProcessesWithOpenTask(){
        UserTaskInfo task1 = new UserTaskInfo();
        task1.id = "1";
        task1.name = "Task1";

        ProcessInstanceInfo info1 = new ProcessInstanceInfo();
        info1.processId = "P1";
        info1.name = "Process1";
        info1.openTasks = Arrays.asList(task1);

        ProcessInstanceInfos infos = new ProcessInstanceInfos(Arrays.asList(info1));
        when(bpmService.getRunningProcesses(any(), any(), any())).thenReturn(infos);

        Map<String, Object> response = target("/devices/SPE01/whatsgoingon").request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(3);
        List<?> data = (List<?>) response.get("goingsOn");
        Map<?, ?> processMap = (Map<?, ?>) data.get(1);
        assertThat(processMap.get("type")).isEqualTo("process");
        assertThat(processMap.get("id")).isEqualTo("P1");
        Map<?, ?> task = (Map<?,?>) processMap.get("userTaskInfo");
        assertThat(task.get("name")).isEqualTo("Task1");
        assertThat(task.get("id")).isEqualTo("1");
    }

}
