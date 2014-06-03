package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.*;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuleResourceTest extends Mocks {

    @Test
    public void testGetAssignmentRulesEmpty(){
        Query<AssignmentRule> query = mock(Query.class);
        when(query.select(Matchers.<Condition>anyObject())).thenReturn(Collections.<AssignmentRule>emptyList());
        when(issueAssignmentService.getAssignmentRuleQuery()).thenReturn(query);

        Map<String, Object> map = target("/rules/assign").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }

    @Test
    public void testGetAssignmentRules() {
        IssueAssignee assignee = getDefaultAssignee();
        List<AssignmentRule> rules = new ArrayList<>();
        rules.add(mockAssignmentRule(1, "title 1", null, 1, assignee));
        rules.add(mockAssignmentRule(2, "title 2", null, 3, assignee));

        Query<AssignmentRule> query = mock(Query.class);
        when(query.select(Matchers.<Condition>anyObject())).thenReturn(rules);
        when(issueAssignmentService.getAssignmentRuleQuery()).thenReturn(query);

        Map<String, Object> map = target("/rules/assign").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List data = (List) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(1)).get("name")).isEqualTo("title 2");
    }

    @Test
    public void testGetCreationRulesTemplatesWOParams(){
        Response response = target("/rules/templates").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetCreationRulesTemplatesWrongIssueType(){
        IssueType issueType = getDefaultIssueType();
        List<CreationRuleTemplate> templates = new ArrayList<>();
        templates.add(mockCreationRuleTemplate("1", "name 1", null, issueType, null));
        templates.add(mockCreationRuleTemplate("2", "name 2", null, issueType, null));

        when(issueCreationService.getCreationRuleTemplates()).thenReturn(templates);

        Map<String, Object> map = target("/rules/templates").queryParam(ISSUE_TYPE, "unexist").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }

    @Test
    public void testGetCreationRulesTemplates(){
        IssueType issueType = getDefaultIssueType();
        List<CreationRuleTemplate> templates = new ArrayList<>();
        templates.add(mockCreationRuleTemplate("1", "name 1", null, issueType, null));
        templates.add(mockCreationRuleTemplate("2", "name 2", null, issueType, null));

        when(issueCreationService.getCreationRuleTemplates()).thenReturn(templates);

        Map<String, Object> map = target("/rules/templates").queryParam(ISSUE_TYPE, issueType.getUUID()).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List data = (List) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(1)).get("name")).isEqualTo("name 2");
    }

    @Test
    public void testGetUnexistingTemplate(){
        when(issueCreationService.findCreationRuleTemplate("9999-0")).thenReturn(Optional.<CreationRuleTemplate>absent());

        Response response = target("/rules/templates/9999-0").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetTemplate(){
        CreationRuleTemplate template = getDefaultCreationRuleTemplate();
        when(issueCreationService.findCreationRuleTemplate("0-1-2")).thenReturn(Optional.of(template));

        Map<String, Object> map = target("/rules/templates/0-1-2").request().get(Map.class);
        assertThat(((Map)map.get("data")).get("uid")).isEqualTo("0-1-2");
    }

    @Test
    public void testGetAllParametersValuesForUnexistingTemplate(){
        Map<String, String> params = new HashMap<>(2);
        params.put("param1", "value1");
        params.put("param2", "value2");
        Entity<Map<String, String>> json = Entity.json(params);

        when(issueCreationService.findCreationRuleTemplate("9999-0")).thenReturn(Optional.<CreationRuleTemplate>absent());

        Response response = target("/rules/templates/9999-0/parameters").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllParametersValue(){
        CreationRuleTemplate template = getDefaultCreationRuleTemplate();
        Map<String, String> params = new HashMap<>(2);
        params.put("param1", "value1");
        params.put("param2", "value2");
        Entity<Map<String, String>> json = Entity.json(params);

        when(issueCreationService.findCreationRuleTemplate("0-1-2")).thenReturn(Optional.of(template));

        Map<String, Object> map = target("/rules/templates/0-1-2/parameters").request().put(json, Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }

    // TODO test parameters
}
