package com.elster.jupiter.issue.rest;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TopIssuesResourceTest extends IssueRestApplicationJerseyTest{

    @Mock
    IssueProvider issueProvider;
    @Mock
    Issue issue;

    @Before
    public void beforeTest() {
        User user = getDefaultUser();
        when(securityContext.getUserPrincipal()).thenReturn(user);
        IssueAssignee assignee = mock(IssueAssignee.class);
        WorkGroup workGroup = mock(WorkGroup.class);
        when(workGroup.getId()).thenReturn(1L);
        when(workGroup.getName()).thenReturn("workgroup");
        when(assignee.getUser()).thenReturn(user);
        when(assignee.getWorkGroup()).thenReturn(workGroup);

        IssueType issueType = mock(IssueType.class);
        when(issueType.getKey()).thenReturn("key");
        when(issueType.getName()).thenReturn("name");
        when(issueType.getPrefix()).thenReturn("key.name");

        IssueReason reason = mock(IssueReason.class);
        when(reason.getKey()).thenReturn("key");
        when(reason.getName()).thenReturn("name");
        when(reason.getIssueType()).thenReturn(issueType);

        IssueStatus status = mock(IssueStatus.class);
        when(status.isHistorical()).thenReturn(true);
        when(status.getName()).thenReturn("name");
        when(status.getKey()).thenReturn("key");

        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(1L);
        when(meter.getName()).thenReturn("device");
        when(meter.getSerialNumber()).thenReturn("0.0.0.0.0.0.0.0");
        when(meter.getAmrId()).thenReturn(String.valueOf(1L));
        doReturn(Optional.empty()).when(meter).getCurrentMeterActivation();
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);

        when(issue.getId()).thenReturn(1L);
        when(issue.getReason()).thenReturn(reason);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getAssignee()).thenReturn(assignee);
        when(issue.getDevice()).thenReturn(meter);
        when(issue.getCreateTime()).thenReturn(Instant.EPOCH);
        when(issue.getModTime()).thenReturn(Instant.EPOCH);
        when(issue.getVersion()).thenReturn(1L);
    }

    //FixMe Move test to alarms.rest
    /*@Test
    public void getTopAlarms(){
        Finder<? extends Issue> issueFinder = mock(Finder.class);
        doReturn(issueFinder).when(issueService).findAlarms();
        List<? extends Issue> issues = Collections.singletonList(issue);
        doReturn(issues).when(issueFinder).find();
        List<IssueProvider> issueProviders = Arrays.asList(issueProvider);
        doReturn(issueProviders).when(issueService).getIssueProviders();
        Optional<? extends Issue> issueRef = Optional.of(issues.get(0));
        doReturn(issueRef).when(issueProvider).findIssue(1L);

        Map response = target("/topissues/alarms").request().get(Map.class);
        defaultTopTaskAsserts(response);
    }*/

    @Test
    public void getTopIssues(){
        Finder<? extends Issue> issueFinder = mock(Finder.class);
        doReturn(issueFinder).when(issueService).findIssues(any(IssueFilter.class), anyVararg());
        List<? extends Issue> issues = Collections.singletonList(issue);
        doReturn(issues).when(issueFinder).find();
        List<IssueProvider> issueProviders = Arrays.asList(issueProvider);
        doReturn(issueProviders).when(issueService).getIssueProviders();
        Optional<? extends Issue> issueRef = Optional.of(issues.get(0));
        doReturn(issueRef).when(issueProvider).findIssue(1L);

        Map response = target("/topissues/issues").request().get(Map.class);
        defaultTopTaskAsserts(response);
    }

    private void defaultTopTaskAsserts(Map<?, ?> response){
        assertThat(response.get("totalUserAssigned")).isEqualTo(1);
        assertThat(response.get("totalWorkGroupAssigned")).isEqualTo(0);
        assertThat(response.get("total")).isEqualTo(1);

        List<?> data = (List<?>) response.get("items");
        Map<?, ?> topIssueMap = (Map<?, ?>) data.get(0);

        assertThat(topIssueMap.get("id")).isEqualTo(1);

        Map<?, ?> reasonMap = (Map<?, ?>) topIssueMap.get("reason");
        assertThat(reasonMap.get("id")).isEqualTo("key");
        assertThat(reasonMap.get("name")).isEqualTo("name");

        Map<?, ?> statusMap = (Map<?, ?>) topIssueMap.get("status");
        assertThat(statusMap.get("id")).isEqualTo("key");
        assertThat(statusMap.get("name")).isEqualTo("name");

        Map<?, ?> assigneeMap = (Map<?, ?>) topIssueMap.get("userAssignee");
        assertThat(assigneeMap.get("id")).isEqualTo(1);
        assertThat(assigneeMap.get("name")).isEqualTo("Admin");

        Map<?, ?> workGroupAssignee = (Map<?, ?>) topIssueMap.get("workGroupAssignee");
        assertThat(workGroupAssignee.get("id")).isEqualTo(1);
        assertThat(workGroupAssignee.get("name")).isEqualTo("workgroup");
    }

}
