/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueStatusInfo;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.util.conditions.Condition;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatusResourceTest extends DeviceAlarmApplicationTest{

    @Test
    public void testGetEmptyStatuses() {
        List<IssueStatus> statuses = new ArrayList<>();
        Query<IssueStatus> query = mock(Query.class);
        when(query.select(Condition.TRUE)).thenReturn(statuses);
        when(issueService.query(IssueStatus.class)).thenReturn(query);

        List<IssueStatusInfo> statusInfos = target("/statuses").request().get(List.class);

        assertThat(statusInfos.size()).isEqualTo(0);
    }

    @Test
    public void testGetStatuses(){
        List<IssueStatus> statuses = new ArrayList<>();
        statuses.add(getDefaultStatus());
        statuses.add(mockStatus("2", "close", true));

        Query<IssueStatus> query = mock(Query.class);
        when(query.select(Condition.TRUE)).thenReturn(statuses);
        when(issueService.query(IssueStatus.class)).thenReturn(query);

        List<IssueStatusInfo> statusInfos  = target("/statuses").request().get(List.class);
        assertThat(statusInfos.size()).isEqualTo(2);
    }

    @Test
    public void testGetUnexistingStatus(){
        when(issueService.findStatus("not-exsist")).thenReturn(Optional.empty());

        Response response = target("/statuses/not-exsist").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

}
