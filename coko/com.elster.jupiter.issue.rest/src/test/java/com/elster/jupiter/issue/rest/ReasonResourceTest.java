package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReasonResourceTest extends Mocks {

    @Test
    public void testGetReasonsWithoutParams(){
        Response response = target("/reasons").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetReasonsWithoutLike(){
        IssueType issueType = getDefaultIssueType();
        List<IssueReason> reasons = new ArrayList<>();

        Query<IssueReason> query = mock(Query.class);
        when(query.select(Matchers.<Condition>anyObject())).thenReturn(reasons);
        when(issueService.query(IssueReason.class)).thenReturn(query);
        when(issueService.findIssueType(issueType.getUUID())).thenReturn(Optional.of(issueType));

        Map<String, Object> map = target("/reasons").queryParam(ISSUE_TYPE, issueType.getUUID()).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }

    @Test
    public void testGetReasons(){
        IssueType issueType = getDefaultIssueType();
        List<IssueReason> reasons = new ArrayList<>();
        reasons.add(mockReason(1, "Name 1", issueType));
        reasons.add(mockReason(2, "Name 2", issueType));

        Query<IssueReason> query = mock(Query.class);
        when(query.select(Matchers.<Condition>anyObject())).thenReturn(reasons);
        when(issueService.query(IssueReason.class)).thenReturn(query);
        when(issueService.findIssueType(issueType.getUUID())).thenReturn(Optional.of(issueType));

        Map<String, Object> map = target("/reasons")
                .queryParam(ISSUE_TYPE, issueType.getUUID())
                .queryParam(LIKE, "ame").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List data = (List) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(1)).get("name")).isEqualTo("Name 2");
    }


    @Test
    public void testGetUnexistingReason(){
        when(issueService.findReason(9999)).thenReturn(Optional.<IssueReason>absent());

        Response response = target("/reasons/9999").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetReason(){
        IssueReason reason = getDefaultReason();
        when(issueService.findReason(1)).thenReturn(Optional.of(reason));

        Map<String, Object> map = target("/reasons/1").request().get(Map.class);
        assertThat(((Map)map.get("data")).get("id")).isEqualTo(1);
    }
}
