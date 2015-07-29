package com.elster.jupiter.issue.rest;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.mockito.Matchers;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.util.conditions.Condition;
import com.jayway.jsonpath.JsonModel;

public class RuleResourceTest extends IssueRestApplicationJerseyTest {

    @Test
    public void testGetAssignmentRulesEmpty() {
        @SuppressWarnings("unchecked")
        Query<AssignmentRule> query = mock(Query.class);
        when(query.select(Matchers.anyObject())).thenReturn(Collections.emptyList());
        when(issueAssignmentService.getAssignmentRuleQuery()).thenReturn(query);

        String response = target("/rules/assign").request().get(String.class);
        
        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number> get("$.total")).isEqualTo(0);
        assertThat(json.<List<?>> get("$.data")).isEmpty();
    }

    @Test
    public void testGetAssignmentRules() {
        IssueAssignee assignee = getDefaultAssignee();
        List<AssignmentRule> rules = new ArrayList<>();
        rules.add(mockAssignmentRule(1, "title 1", "description 1", 1, assignee));
        rules.add(mockAssignmentRule(2, "title 2", "description 2", 3, assignee));

        @SuppressWarnings("unchecked")
        Query<AssignmentRule> query = mock(Query.class);
        when(query.select(Matchers.<Condition> anyObject())).thenReturn(rules);
        when(issueAssignmentService.getAssignmentRuleQuery()).thenReturn(query);

        String response = target("/rules/assign").request().get(String.class);

        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number> get("$.total")).isEqualTo(2);
        assertThat(json.<List<?>> get("$.data")).hasSize(2);
        assertThat(json.<List<Number>> get("$.data[*].id")).containsExactly(1, 2);
        assertThat(json.<List<String>> get("$.data[*].name")).containsExactly("title 1", "title 2");
        assertThat(json.<List<String>> get("$.data[*].description")).containsExactly("description 1", "description 2");
        assertThat(json.<List<String>> get("$.data[*].assignee.type")).containsExactly("USER", "USER");
        assertThat(json.<List<Number>> get("$.data[*].assignee.id")).containsExactly(1, 1);
        assertThat(json.<List<String>> get("$.data[*].assignee.name")).containsExactly("Admin", "Admin");
    }

    @Test
    public void testGetCreationRulesTemplatesWOParams() {
        Response response = target("/rules/templates").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetCreationRulesTemplatesWrongIssueType() {
        IssueType issueType = getDefaultIssueType();
        List<CreationRuleTemplate> templates = new ArrayList<>();
        templates.add(mockCreationRuleTemplate("1", "descr1", issueType, null));
        templates.add(mockCreationRuleTemplate("2", "descr2", issueType, null));

        when(issueCreationService.getCreationRuleTemplates()).thenReturn(templates);

        String response = target("/rules/templates").queryParam(ISSUE_TYPE, "unexist").request().get(String.class);

        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number> get("$.total")).isEqualTo(0);
        assertThat(json.<List<?>> get("$.creationRuleTemplates")).isEmpty();
    }

    @Test
    public void testGetCreationRulesTemplates() {
        IssueType issueType = getDefaultIssueType();
        List<CreationRuleTemplate> templates = new ArrayList<>();
        templates.add(mockCreationRuleTemplate("Template #1", "descr 1", issueType, Collections.emptyList()));
        templates.add(mockCreationRuleTemplate("Template #2", "descr 2", issueType, Arrays.asList(mockProperty("property"))));

        when(issueCreationService.getCreationRuleTemplates()).thenReturn(templates);

        String response = target("/rules/templates").queryParam(ISSUE_TYPE, issueType.getKey()).request().get(String.class);
        
        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number>get("$.total")).isEqualTo(2);
        assertThat(json.<List<String>>get("$.creationRuleTemplates[*].name")).containsExactly("Template #1", "Template #2");
        assertThat(json.<List<String>>get("$.creationRuleTemplates[*].displayName")).containsExactly("Display Name: Template #1", "Display Name: Template #2");
        assertThat(json.<List<String>>get("$.creationRuleTemplates[*].description")).containsExactly("descr 1", "descr 2");
        assertThat(json.<List<?>>get("$.creationRuleTemplates[0].properties")).isEmpty();
        assertThat(json.<List<?>>get("$.creationRuleTemplates[1].properties")).hasSize(1);
        assertThat(json.<String>get("$.creationRuleTemplates[1].properties[0].key")).isEqualTo("property");
        assertThat(json.<String>get("$.creationRuleTemplates[1].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXTAREA");
    }

    @Test
    public void testGetUnexistingTemplate() {
        when(issueCreationService.findCreationRuleTemplate("9999-0")).thenReturn(Optional.empty());

        Response response = target("/rules/templates/9999-0").request().get();
        
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetTemplateByName() {
        CreationRuleTemplate template = getDefaultCreationRuleTemplate();
        when(issueCreationService.findCreationRuleTemplate("0-1-2")).thenReturn(Optional.of(template));

        String response = target("/rules/templates/0-1-2").request().get(String.class);
        
        JsonModel json = JsonModel.model(response);
        assertThat(json.<String>get("$.name")).isEqualTo("0-1-2");
    }

    private PropertySpec mockProperty(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        return propertySpec;
    }
}
