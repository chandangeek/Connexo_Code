/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest;

import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.entity.IssueGroup;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HistoryResourceTest extends DeviceAlarmApplicationTest {

    @Test
    public void testGetAlarmHistory() {
        IssueGroup entity = mock(IssueGroup.class);
        when(entity.getGroupKey()).thenReturn(new BigDecimal(1L));
        when(entity.getGroupName()).thenReturn("Reason");
        when(entity.getCount()).thenReturn(5L);

        IssueGroupFilter groupFilter = mockIssueGroupFilter();
        when(issueService.newIssueGroupFilter()).thenReturn(groupFilter);
        when(deviceAlarmService.getDeviceAlarmGroupList(groupFilter)).thenReturn(Arrays.asList(entity));

        String filter = getFilter();
        String response = target("/history").queryParam("filter", filter).request().get(String.class);
        assertThat(response).isEqualTo("{\"data\":[{\"date\":\"1\",\"Reason\":5}],\"fields\":[\"Reason\"]}");
        verify(groupFilter).withReasons(Arrays.asList("Reason"));
        verify(groupFilter).groupBy("reasonsPerDay");
    }

    @Test
    public void testGetAlarmHistorySorted() {
        IssueGroup entity1 = mock(IssueGroup.class);
        when(entity1.getGroupKey()).thenReturn(new BigDecimal(1L));
        when(entity1.getGroupName()).thenReturn("Reason");
        when(entity1.getCount()).thenReturn(10L);

        IssueGroup entity2 = mock(IssueGroup.class);
        when(entity2.getGroupKey()).thenReturn(new BigDecimal(2L));
        when(entity2.getGroupName()).thenReturn("Reason");
        when(entity2.getCount()).thenReturn(20L);

        IssueGroup entity3 = mock(IssueGroup.class);
        when(entity3.getGroupKey()).thenReturn(new BigDecimal(3L));
        when(entity3.getGroupName()).thenReturn("Reason");
        when(entity3.getCount()).thenReturn(30L);


        IssueGroupFilter groupFilter = mockIssueGroupFilter();
        when(issueService.newIssueGroupFilter()).thenReturn(groupFilter);
        when(deviceAlarmService.getDeviceAlarmGroupList(groupFilter)).thenReturn(Arrays.asList(entity3, entity1, entity2));

        String filter = getFilterWithSort();
        String response = target("/history").queryParam("filter", filter).request().get(String.class);
        assertThat(response).isEqualTo("{\"data\":[{\"date\":\"1\",\"Reason\":10},{\"date\":\"2\",\"Reason\":20},{\"date\":\"3\",\"Reason\":30}],\"fields\":[\"Reason\"]}");
        verify(groupFilter).groupBy("reasonsPerDay");
    }

    private String getFilterWithSort()  {
        try {
            return URLEncoder.encode("[{\"property\":\"field\",\"value\":\"reasonsPerDay\"}]", "UTF-8");
        }
        catch (UnsupportedEncodingException e){
            return "";
        }
    }

    private String getFilter()  {
        try {
            return URLEncoder.encode("[{\"property\":\"reason\",\"value\":\"Reason\"},{\"property\":\"field\",\"value\":\"reasonsPerDay\"}]", "UTF-8");
        }
        catch (UnsupportedEncodingException e){
            return "";
        }
    }




}
