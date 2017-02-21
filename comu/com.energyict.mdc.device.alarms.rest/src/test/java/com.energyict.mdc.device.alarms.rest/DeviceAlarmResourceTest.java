/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceAlarmResourceTest extends DeviceAlarmApplicationTest{

    @Test
    public void testGetAlarmById() {
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.empty());
        Optional<DeviceAlarm> alarm = Optional.of(getDefaultAlarm());
        doReturn(alarm).when(deviceAlarmService).findAlarm(1);

        Map<?, ?> alarmMap = target("/alarms/1").request().get(Map.class);
        assertDefaultAlarmMap(alarmMap);
    }

    @Test
    public void testGetUnexistingAlarmById() {
        when(deviceAlarmService.findAlarm(1)).thenReturn(Optional.empty());

        Response response = target("/alarms/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllAlarms(){
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.empty());
        Finder<? extends DeviceAlarm> alarmFinder = mock(Finder.class);
        doReturn(alarmFinder).when(deviceAlarmService).findAlarms(any(DeviceAlarmFilter.class), anyVararg());
        List<? extends DeviceAlarm> alarms = Collections.singletonList(getDefaultAlarm());
        doReturn(alarms).when(alarmFinder).find();
        Optional<IssueStatus> status = Optional.of(getDefaultStatus());
        when(issueService.findStatus("open")).thenReturn(status);

        String filter = URLEncoder.encode("[{\"property\":\"status\",\"value\":[\"open\"]}]");
        Map<?, ?> map = target("/alarms").queryParam("filter", filter).queryParam("start", "0").queryParam("limit", "1").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(1);

        List<?> data = (List<?>) map.get("data");
        assertThat(data).hasSize(1);

        Map<?, ?> alarmMap = (Map<?, ?>) data.get(0);
        assertDefaultAlarmMap(alarmMap);
    }

    @Test
    public void testGroupedList() {
        IssueGroup entity = mock(IssueGroup.class);
        when(entity.getGroupKey()).thenReturn(1L);
        when(entity.getGroupName()).thenReturn("Reason 1");
        when(entity.getCount()).thenReturn(5L);

        List<IssueGroup> groupedList = Arrays.asList(entity);
        IssueGroupFilter issueGroupFilter = mock(IssueGroupFilter.class);
        when(issueGroupFilter.using(Matchers.<Class>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.onlyGroupWithKey(Matchers.<String>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.withIssueTypes(Matchers.<List<String>>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.withStatuses(Matchers.<List<String>>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.withMeterName(Matchers.<String>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.withClearedStatuses(anyCollection())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.groupBy(Matchers.<String>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.withId(Matchers.<String>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.setAscOrder(false)).thenReturn(issueGroupFilter);
        when(issueGroupFilter.from(1L)).thenReturn(issueGroupFilter);
        when(issueGroupFilter.to(2L)).thenReturn(issueGroupFilter);
        when(issueService.newIssueGroupFilter()).thenReturn(issueGroupFilter);
        doReturn(groupedList).when(issueService).getIssueGroupList(issueGroupFilter);
        TransactionContext context = mock(TransactionContext.class);
        when(transactionService.getContext()).thenReturn(context);

        String filter = URLEncoder.encode("[{\"property\":\"id\",\"value\":\"1\"},{\"property\":\"field\",\"value\":\"reason\"},{\"property\":\"issueType\",\"value\":[\"datacollection\"]}]");
        Query<IssueType> query = mock(Query.class);
        when(query.select(Matchers.<Condition>anyObject())).thenReturn(Collections.<IssueType>emptyList());
        when(issueService.query(IssueType.class)).thenReturn(query);
        Map<?, ?> map = target("alarms/groupedlist")
                .queryParam("start", 0).queryParam("limit", 1).queryParam("filter", filter).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);

        List<?> groups = (List<?>) map.get("alarmGroups");
        assertThat(groups).hasSize(1);

        Map<?, ?> groupMap = (Map<?, ?>) groups.get(0);
        assertThat(groupMap.get("id")).isEqualTo(1);
        assertThat(groupMap.get("description")).isEqualTo("Reason 1");
        assertThat(groupMap.get("number")).isEqualTo(5);
    }

    private void assertDefaultAlarmMap(Map<?, ?> alarmMap) {
        assertThat(alarmMap.get("id")).isEqualTo(1);
        assertThat(alarmMap.get("alarmId")).isEqualTo("ALM-001");

        Map<?, ?> reasonMap = (Map<?, ?>) alarmMap.get("reason");
        assertThat(reasonMap.get("id")).isEqualTo("1");
        assertThat(reasonMap.get("name")).isEqualTo("Reason");

        Map<?, ?> statusMap = (Map<?, ?>) alarmMap.get("status");
        assertThat(statusMap.get("id")).isEqualTo("1");
        assertThat(statusMap.get("name")).isEqualTo("open");

        Map<?, ?> userAssignee = (Map<?, ?>) alarmMap.get("userAssignee");
        assertThat(userAssignee.get("id")).isEqualTo(1);
        assertThat(userAssignee.get("name")).isEqualTo("Admin");

        Map<?, ?> workGroupAssignee = (Map<?, ?>) alarmMap.get("workGroupAssignee");
        assertThat(workGroupAssignee.get("id")).isEqualTo(1);
        assertThat(workGroupAssignee.get("name")).isEqualTo("WorkGroup");

        Map<?, ?> logBook = (Map<?, ?>) alarmMap.get("logBook");
        assertThat(logBook.get("id")).isEqualTo(1);
        assertThat(logBook.get("name")).isEqualTo("LogBookName");

        List<?> releatedEvents = (List<?>) alarmMap.get("relatedEvents");
        Map<?, ?> releatedEventMap = (Map<?, ?>) releatedEvents.get(0);
        assertThat(releatedEventMap.get("deviceType")).isEqualTo("Collector (10)");
        assertThat(releatedEventMap.get("domain")).isEqualTo("Battery (2)");
        assertThat(releatedEventMap.get("subDomain")).isEqualTo("Activation (283)");
        assertThat(releatedEventMap.get("eventDate")).isEqualTo(1451606400000L);

    }

}
