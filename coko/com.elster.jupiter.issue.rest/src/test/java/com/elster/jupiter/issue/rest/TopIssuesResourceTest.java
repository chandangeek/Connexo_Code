/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TopIssuesResourceTest extends IssueRestApplicationJerseyTest {

    @Mock
    OpenIssue issue;
    @Mock
    private Query<OpenIssue> issueQuery;
    @Mock
    private Query<IssueReason> issueReasonQuery;
    @Mock
    private Map<IssueTypes, Long> openIssueCountMap;

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
        when(status.isHistorical()).thenReturn(false);
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

        Priority priority = Priority.DEFAULT;
        when(issue.getPriority()).thenReturn(priority);
        when(issue.getId()).thenReturn(1L);
        when(issue.getReason()).thenReturn(reason);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getAssignee()).thenReturn(assignee);
        when(issue.getDevice()).thenReturn(meter);
        when(issue.getCreateTime()).thenReturn(Instant.EPOCH);
        when(issue.getCreateDateTime()).thenReturn(Instant.EPOCH);
        when(issue.getModTime()).thenReturn(Instant.EPOCH);
        when(issue.getVersion()).thenReturn(1L);
        when(issue.getUsagePoint()).thenReturn(Optional.empty());
        when(issueService.query(OpenIssue.class, IssueReason.class, IssueType.class)).thenReturn(issueQuery);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        doReturn(Collections.singletonList(issue)).when(issueQuery)
                .select(any(Condition.class), anyInt(), anyInt(), orderCaptor.capture());
        when(issueService.query(IssueReason.class)).thenReturn(issueReasonQuery);
        when(issueService.findIssueType(anyString())).thenReturn(Optional.of(issueType));
        when(issueService.getUserOpenIssueCount(user)).thenReturn(new HashMap<IssueTypes, Long>() {{
            put(IssueTypes.DATA_COLLECTION, 1L);
        }});
        when(issueService.getWorkGroupWithoutUserOpenIssueCount(user)).thenReturn(new HashMap<IssueTypes, Long>() {{
            put(IssueTypes.DATA_COLLECTION, 0L);
        }});
        doReturn(Collections.singletonList(issue)).when(issueReasonQuery)
                .select(where(ISSUE_TYPE).isNotEqual(anyObject()));
    }

    @Test
    public void getTopIssues() {
        Query<Issue> issueQuery = mock(Query.class);
        doReturn(issueQuery).when(issueService).query(same(OpenIssue.class), anyVararg());
        when(issueService.findStatus(anyString())).thenReturn(Optional.empty());
        when(issue.getSnoozeDateTime()).thenReturn(Optional.empty());
        when(issue.getDevice().getLocation()).thenReturn(Optional.empty());
        List<? extends Issue> issues = Collections.singletonList(issue);
        doReturn(issues).when(issueQuery).select(any(Condition.class), anyInt(), anyInt(), anyVararg());
        Map<?, ?> response = target("/topissues/issues").request().header("X-CONNEXO-APPLICATION-NAME", "MDC").get(Map.class);
        defaultTopTaskAsserts(response);
    }

    private void defaultTopTaskAsserts(Map<?, ?> response) {
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
