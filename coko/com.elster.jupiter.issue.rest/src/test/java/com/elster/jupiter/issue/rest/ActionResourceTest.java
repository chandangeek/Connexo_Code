package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionResourceTest extends Mocks {
    @Test
    public void testGetAllActionPhases(){
        when(thesaurus.getString(MessageSeeds.ISSUE_ACTION_PHASE_CREATE.getKey(), MessageSeeds.ISSUE_ACTION_PHASE_CREATE.getDefaultFormat())).thenReturn("Create");
        when(thesaurus.getString(MessageSeeds.ISSUE_ACTION_PHASE_OVERDUE.getKey(), MessageSeeds.ISSUE_ACTION_PHASE_OVERDUE.getDefaultFormat())).thenReturn("Overdue");

        Map<String, Object> map = target("/actions/phases").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List data = (List) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(0)).get("title")).isEqualTo("Create");
        assertThat(((Map) data.get(0)).get("uuid")).isEqualTo("CREATE");
        assertThat(((Map) data.get(1)).get("title")).isEqualTo("Overdue");
        assertThat(((Map) data.get(1)).get("uuid")).isEqualTo("OVERDUE");
    }

    @Test
    public void testGetAllActionTypesWOIssueType(){
        Response response = target("/actions").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllActionTypesWithUnexistingIssueType(){
        IssueType issueType = mockIssueType("unexisting", "name");
        when(issueService.findIssueType(issueType.getUUID())).thenReturn(Optional.<IssueType>absent());

        Map<String, Object> map = target("/actions")
                .queryParam(ISSUE_TYPE, issueType.getUUID()).request().get(Map.class);
        assertThat((String)map.get("data")).isEmpty();
    }

    @Test
    public void testGetAllActionTypesWithWrongIssueType(){
        IssueType issueType = mockIssueType("some", "name");
        Query<IssueActionType> query = mock(Query.class);

        when(query.select(Matchers.<Condition>anyObject())).thenReturn(Collections.<IssueActionType>emptyList());
        when(issueService.query(IssueActionType.class, IssueType.class)).thenReturn(query);
        when(issueService.findIssueType(issueType.getUUID())).thenReturn(Optional.of(issueType));

        Map<String, Object> map = target("/actions")
                .queryParam(ISSUE_TYPE, issueType.getUUID()).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }

    @Test
    public void testGetAllActionTypes(){
        IssueType issueType = getDefaultIssueType();
        List<IssueActionType> actionTypes = new ArrayList<>(2);
        actionTypes.add(mockIssueActionType(1, "one", issueType));
        actionTypes.add(mockIssueActionType(2, "two", issueType));
        Query<IssueActionType> query = mock(Query.class);

        when(query.select(Matchers.<Condition>anyObject())).thenReturn(actionTypes);
        when(issueService.query(IssueActionType.class, IssueType.class)).thenReturn(query);
        when(issueService.findIssueType(issueType.getUUID())).thenReturn(Optional.of(issueType));

        Map<String, Object> map = target("/actions")
                .queryParam(ISSUE_TYPE, issueType.getUUID()).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List data = (List) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(0)).get("id")).isEqualTo(1);
        assertThat(((Map) data.get(0)).get("name")).isEqualTo("one");
    }
}
