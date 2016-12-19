package com.energyict.mdc.device.alarms.rest;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.util.conditions.Condition;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReasonResourceTest extends DeviceAlarmApplicationTest {

    @Test
    public void testGetReasonsWithoutParams(){
        Query<IssueReason> query = mock(Query.class);
        when(issueService.query(IssueReason.class)).thenReturn(query);
        when(issueService.findIssueType(Matchers.<String>anyObject())).thenReturn(Optional.empty());
        List<Object> list = target("/reasons").request().get(List.class);
        assertThat(list.size()).isEqualTo(0);
    }

    @Test
    public void testGetReasons(){
        List<IssueReason> reasons = new ArrayList<>();
        reasons.add(mockReason("1", "Name 1"));
        reasons.add(mockReason("2", "Name 2"));

        Query<IssueReason> query = mock(Query.class);
        when(query.select(Matchers.<Condition>anyObject())).thenReturn(reasons);
        when(issueService.query(IssueReason.class)).thenReturn(query);

        List<Object> list = target("/reasons")
                .queryParam("like", "ame").request().get(List.class);

        assertThat(list.size()).isEqualTo(2);
        assertThat(((Map) list.get(1)).get("name")).isEqualTo("Name 2");
    }

    @Test
    public void testGetUnexistingReason(){
        when(issueService.findReason("not-exsist")).thenReturn(Optional.empty());

        Response response = target("/reasons/not-exsist").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

}
