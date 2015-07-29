package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.util.conditions.Condition;
import java.util.Optional;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatusResourceTest extends IssueRestApplicationJerseyTest{

    @Test
    public void testGetEmptyStatuses() {
        List<IssueStatus> statuses = new ArrayList<>();
        Query<IssueStatus> query = mock(Query.class);
        when(query.select(Condition.TRUE)).thenReturn(statuses);
        when(issueService.query(IssueStatus.class)).thenReturn(query);

        Map<String, Object> map = target("/statuses").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }

    @Test
    public void testGetStatuses(){
        List<IssueStatus> statuses = new ArrayList<>();
        statuses.add(getDefaultStatus());
        statuses.add(mockStatus("2", "close", true));

        Query<IssueStatus> query = mock(Query.class);
        when(query.select(Condition.TRUE)).thenReturn(statuses);
        when(issueService.query(IssueStatus.class)).thenReturn(query);

        Map<String, Object> map  = target("/statuses").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List data = (List) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(1)).get("allowForClosing")).isEqualTo(true);
    }

    @Test
    public void testGetUnexistingStatus(){
        when(issueService.findStatus("not-exsist")).thenReturn(Optional.empty());

        Response response = target("/statuses/not-exsist").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetStatus(){
        IssueStatus status = getDefaultStatus();
        when(issueService.findStatus("status")).thenReturn(Optional.of(status));

        Map<String, Object> map = target("/statuses/status").request().get(Map.class);
        assertThat(((Map)map.get("data")).get("id")).isEqualTo("1");
    }
}
