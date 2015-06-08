package com.elster.jupiter.issue.rest;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.mockito.Matchers;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;
import com.jayway.jsonpath.JsonModel;

public class ActionResourceTest extends IssueRestApplicationJerseyTest {

    @Test
    public void testGetAllActionPhases() {
        when(thesaurus.getString(MessageSeeds.ISSUE_ACTION_PHASE_CREATE.getKey(), MessageSeeds.ISSUE_ACTION_PHASE_CREATE.getDefaultFormat())).thenReturn("Create");
        when(thesaurus.getString(MessageSeeds.ISSUE_ACTION_PHASE_OVERDUE.getKey(), MessageSeeds.ISSUE_ACTION_PHASE_OVERDUE.getDefaultFormat())).thenReturn("Overdue");

        String response = target("/actions/phases").request().get(String.class);

        JsonModel json = JsonModel.model(response);

        assertThat(json.<Number> get("$.total")).isEqualTo(2);
        assertThat(json.<List<?>> get("$.creationRuleActionPhases")).hasSize(2);
        assertThat(json.<List<String>> get("$.creationRuleActionPhases[*].uuid")).containsExactly("CREATE", "OVERDUE");
        assertThat(json.<List<String>> get("$.creationRuleActionPhases[*].title")).containsExactly("Create", "Overdue");
    }

    @Test
    public void testGetAllActionTypesWOIssueType() {
        @SuppressWarnings("unchecked")
        Query<IssueActionType> query = mock(Query.class);
        when(query.select(Matchers.<Condition> anyObject())).thenReturn(Collections.emptyList());
        when(issueActionService.getActionTypeQuery()).thenReturn(query);
        when(issueService.findIssueType(null)).thenReturn(Optional.empty());
        when(issueService.findReason(null)).thenReturn(Optional.empty());
        
        Response response = target("/actions").request().get();
        
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetAllActionTypesWithUnexistingIssueType() {
        IssueType issueType = mockIssueType("unexisting", "name");
        @SuppressWarnings("unchecked")
        Query<IssueActionType> query = mock(Query.class);
        when(query.select(Matchers.<Condition> anyObject())).thenReturn(Collections.emptyList());
        when(issueActionService.getActionTypeQuery()).thenReturn(query);
        when(issueService.findIssueType(issueType.getKey())).thenReturn(Optional.empty());
        when(issueService.findReason(null)).thenReturn(Optional.empty());

        String response = target("/actions").queryParam(ISSUE_TYPE, issueType.getKey()).request().get(String.class);
        
        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number>get("$.total")).isEqualTo(0);
        assertThat(json.<List<?>>get("$.ruleActionTypes")).isEmpty();
    }

    @Test
    public void testGetAllActionTypesWithWrongIssueType() {
        IssueType issueType = mockIssueType("some", "name");
        @SuppressWarnings("unchecked")
        Query<IssueActionType> query = mock(Query.class);

        when(query.select(Matchers.<Condition> anyObject())).thenReturn(Collections.emptyList());
        when(issueActionService.getActionTypeQuery()).thenReturn(query);
        when(issueService.findIssueType(issueType.getKey())).thenReturn(Optional.of(issueType));
        when(issueService.findReason(null)).thenReturn(Optional.empty());

        String response = target("/actions").queryParam(ISSUE_TYPE, issueType.getKey()).request().get(String.class);
        
        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number>get("$.total")).isEqualTo(0);
        assertThat(json.<List<?>>get("$.ruleActionTypes")).isEmpty();
    }

    @Test
    public void testGetAllActionTypes() {
        IssueType issueType = getDefaultIssueType();
        List<IssueActionType> actionTypes = Arrays.asList(
                mockIssueActionType(1, "first", issueType),
                mockIssueActionType(2, "second", issueType));
        @SuppressWarnings("unchecked")
        Query<IssueActionType> query = mock(Query.class);

        when(query.select(Matchers.<Condition> anyObject())).thenReturn(actionTypes);
        when(issueActionService.getActionTypeQuery()).thenReturn(query);
        when(issueService.findIssueType(issueType.getKey())).thenReturn(Optional.of(issueType));
        when(issueService.findReason(null)).thenReturn(Optional.empty());

        String response = target("/actions").queryParam(ISSUE_TYPE, issueType.getKey()).request().get(String.class);

        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number>get("$.total")).isEqualTo(2);
        assertThat(json.<List<?>>get("$.ruleActionTypes")).hasSize(2);
        assertThat(json.<List<Number>>get("$.ruleActionTypes[*].id")).containsExactly(1, 2);
        assertThat(json.<List<String>>get("$.ruleActionTypes[*].name")).containsExactly("first", "second");
        assertThat(json.<List<?>>get("$.ruleActionTypes[0].properties")).hasSize(1);
        assertThat(json.<String>get("$.ruleActionTypes[0].properties[0].key")).isEqualTo("property");
    }

    @Test
    public void testGetActionsThatCanBeInstantiated() {
        IssueType issueType = getDefaultIssueType();
        List<IssueActionType> actionTypes = Arrays.asList(
                mockIssueActionType(1, "first", issueType),
                mockIssueActionType(2, "second", issueType));

        IssueActionType actionTypeNotLicensed = mockIssueActionType(3, "not licensed", issueType);
        when(actionTypeNotLicensed.createIssueAction()).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked")
        Query<IssueActionType> query = mock(Query.class);

        when(query.select(Matchers.<Condition> anyObject())).thenReturn(actionTypes);
        when(issueActionService.getActionTypeQuery()).thenReturn(query);
        when(issueService.findIssueType(issueType.getKey())).thenReturn(Optional.of(issueType));
        when(issueService.findReason(null)).thenReturn(Optional.empty());

        String response = target("/actions").queryParam(ISSUE_TYPE, issueType.getKey()).request().get(String.class);

        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number>get("$.total")).isEqualTo(2);
        assertThat(json.<List<?>>get("$.ruleActionTypes")).hasSize(2);
        assertThat(json.<List<Number>>get("$.ruleActionTypes[*].id")).containsExactly(1, 2);
        assertThat(json.<List<String>>get("$.ruleActionTypes[*].name")).containsExactly("first", "second");
    }
}
