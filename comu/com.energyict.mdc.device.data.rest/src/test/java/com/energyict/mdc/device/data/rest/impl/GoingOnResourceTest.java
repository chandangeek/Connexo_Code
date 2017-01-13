package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.bpm.ProcessInstanceInfos;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.data.Device;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoingOnResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testWhatsGoingOn(){
        Device device = mock(Device.class);
        AmrSystem amrSystem = mock(AmrSystem.class);
        Meter meter = mock(Meter.class);
        IssueFilter issueFilter = mock(IssueFilter.class);
        IssueStatus openStatus = mock(IssueStatus.class);
        IssueStatus inProgressStatus = mock(IssueStatus.class);
        Finder<? extends Issue> issueFinder = mock(Finder.class);
        Finder<? extends DeviceAlarm> alarmFinder = mock(Finder.class);
        Issue issue = mock(Issue.class);
        IssueReason reason = mock(IssueReason.class);
        IssueType issueType = mock(IssueType.class);
        DeviceAlarm deviceAlarm = mock(DeviceAlarm.class);
        ProcessInstanceInfos infos = new ProcessInstanceInfos();
        IssueAssignee issueAssignee = mock(IssueAssignee.class);
        User user = mock(User.class);

        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.of(device));
        when(meteringService.findAmrSystem(anyLong())).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter(anyString())).thenReturn(Optional.of(meter));
        when(issueService.newIssueFilter()).thenReturn(issueFilter);
        when(issueService.findStatus(IssueStatus.OPEN)).thenReturn(Optional.of(openStatus));
        when(issueService.findStatus(IssueStatus.IN_PROGRESS)).thenReturn(Optional.of(inProgressStatus));
        doReturn(issueFinder).when(issueService).findIssues(any(IssueFilter.class), anyVararg());
        doReturn(Collections.singletonList(issue)).when(issueFinder).find();
        doReturn(Collections.singletonList(issue).stream()).when(issueFinder).stream();
        when(issue.getReason()).thenReturn(reason);
        when(reason.getIssueType()).thenReturn(issueType);
        when(issueType.getKey()).thenReturn("issueType.key");
        when(issueType.getName()).thenReturn("issueType.name");
        when(issue.getId()).thenReturn(1L);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getStatus()).thenReturn(openStatus);
        when(openStatus.getName()).thenReturn(IssueStatus.OPEN);
        when(serviceCallService.findServiceCalls(any(), any())).thenReturn(new HashSet<>());
        doReturn(alarmFinder).when(deviceAlarmService).findAlarms(any(DeviceAlarmFilter.class), anyVararg());
        doReturn(Collections.singletonList(deviceAlarm).stream()).when(alarmFinder).stream();
        when(bpmService.getRunningProcesses(any(), any(), any())).thenReturn(infos);
        when(deviceAlarm.getId()).thenReturn(1L);
        when(deviceAlarm.getReason()).thenReturn(reason);
        when(deviceAlarm.getDueDate()).thenReturn(null);
        when(deviceAlarm.getStatus()).thenReturn(openStatus);
        when(issue.getAssignee()).thenReturn(issueAssignee);
        when(issueAssignee.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("Admin");
        when(user.getId()).thenReturn(1L);
        when(deviceAlarm.getAssignee()).thenReturn(issueAssignee);
        when(securityContext.getUserPrincipal()).thenReturn(user);

        Map<String, Object> response = target("/devices/SPE01/whatsgoingon").request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);
        List<?> data = (List<?>) response.get("goingsOn");
        assertThat(data).hasSize(2);
        Map<?, ?> issueMap = (Map<?, ?>) data.get(0);
        assertThat(issueMap.get("type")).isEqualTo("issue");
        assertThat(issueMap.get("id")).isEqualTo(1);
        assertThat(issueMap.get("isMyWorkGroup")).isEqualTo(false);
        assertThat(issueMap.get("userAssigneeIsCurrentUser")).isEqualTo(true);
        assertThat(issueMap.get("status")).isEqualTo("status.open");
        assertThat(issueMap.get("issueType")).isEqualTo("issueType.key");
        assertThat(issueMap.get("userAssignee")).isEqualTo("Admin");

        Map<?, ?> alarmMap = (Map<?, ?>) data.get(1);
        assertThat(alarmMap.get("type")).isEqualTo("alarm");
        assertThat(alarmMap.get("id")).isEqualTo(1);
        assertThat(alarmMap.get("isMyWorkGroup")).isEqualTo(false);
        assertThat(alarmMap.get("userAssigneeIsCurrentUser")).isEqualTo(true);
        assertThat(alarmMap.get("status")).isEqualTo("status.open");
        assertThat(issueMap.get("userAssignee")).isEqualTo("Admin");

    }

}
