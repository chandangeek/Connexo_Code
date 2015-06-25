package com.elster.jupiter.issue.rest;

import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.mockito.Matchers;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionPhaseInfo;
import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo.DueInInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleTemplateInfo;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleActionBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleUpdater;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import com.jayway.jsonpath.JsonModel;

public class CreationRuleResourceTest extends IssueRestApplicationJerseyTest {

    @Test
    public void testGetCreationRulesWOParams() {
        @SuppressWarnings(value = { "unchecked" })
        Query<CreationRule> query = mock(Query.class);
        List<CreationRule> rules = Arrays.asList(mockCreationRule(1, "rule 1"), mockCreationRule(2, "rule 2"));
        when(query.select(Matchers.any(Condition.class), Matchers.anyInt(), Matchers.anyInt())).thenReturn(rules);
        when(issueCreationService.getCreationRuleQuery(IssueReason.class, IssueType.class)).thenReturn(query);

        Response response = target("/creationrules").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetCreationRulesEmpty() {
        @SuppressWarnings(value = { "unchecked" })
        Query<CreationRule> query = mock(Query.class);
        when(query.select(Matchers.any(Condition.class), Matchers.anyInt(), Matchers.anyInt())).thenReturn(Collections.<CreationRule> emptyList());
        when(issueCreationService.getCreationRuleQuery(IssueReason.class, IssueType.class)).thenReturn(query);

        String response = target("/creationrules").queryParam(START, 0).queryParam(LIMIT, 10).request().get(String.class);

        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number> get("$.total")).isEqualTo(0);
        assertThat(json.<List<?>> get("$.creationRules")).isEmpty();
    }

    @Test
    public void testGetCreationRules() {
        @SuppressWarnings(value = { "unchecked" })
        Query<CreationRule> query = mock(Query.class);
        List<CreationRule> rules = Arrays.asList(mockCreationRule(1, "rule 1"), mockCreationRule(2, "rule 2"));
        when(query.select(Matchers.any(Condition.class), Matchers.anyInt(), Matchers.anyInt())).thenReturn(rules);
        when(issueCreationService.getCreationRuleQuery(IssueReason.class, IssueType.class)).thenReturn(query);

        String response = target("/creationrules").queryParam(START, 0).queryParam(LIMIT, 10).request().get(String.class);

        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number> get("$.total")).isEqualTo(2);
        assertThat(json.<List<?>> get("$.creationRules")).hasSize(2);
    }

    @Test
    public void testGetCreationRuleUnexisting() {
        when(issueCreationService.findCreationRuleById(9999)).thenReturn(Optional.empty());

        Response response = target("/creationrules/9999").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetCreationRuleById() {
        List<PropertySpec> propertySpecs = mockPropertySpecs();
        CreationRuleTemplate template = mockCreationRuleTemplate("Template", "Template description", mockIssueType("issueType", "Issue Type"), propertySpecs);

        CreationRuleAction action = mock(CreationRuleAction.class);
        IssueActionType actionType = getDefaultIssueActionType();
        when(action.getAction()).thenReturn(actionType);
        when(action.getPhase()).thenReturn(CreationRuleActionPhase.CREATE);
        when(action.getPropertySpecs()).thenReturn(propertySpecs);
        Map<String, Object> actionProps = new HashMap<>();
        actionProps.put("property", "value_for_action");
        when(action.getProperties()).thenReturn(actionProps);

        Instant instant = Instant.now();
        IssueReason reason = getDefaultReason();
        CreationRule rule = mock(CreationRule.class);
        when(rule.getId()).thenReturn(13L);
        when(rule.getName()).thenReturn("Test rule");
        when(rule.getComment()).thenReturn("comment");
        when(rule.getReason()).thenReturn(reason);
        when(rule.getDueInType()).thenReturn(DueInType.DAY);
        when(rule.getDueInValue()).thenReturn(5L);
        when(rule.getActions()).thenReturn(Arrays.asList(action));
        when(rule.getPropertySpecs()).thenReturn(propertySpecs);
        Map<String, Object> ruleProps = new HashMap<>();
        ruleProps.put("property", "value_for_rule");
        when(rule.getProperties()).thenReturn(ruleProps);
        when(rule.getTemplate()).thenReturn(template);
        when(rule.getModTime()).thenReturn(instant);
        when(rule.getCreateTime()).thenReturn(instant);
        when(rule.getVersion()).thenReturn(2L);
        when(issueCreationService.findCreationRuleById(1)).thenReturn(Optional.of(rule));

        String response = target("/creationrules/1").request().get(String.class);

        JsonModel json = JsonModel.model(response);
        assertThat(json.<Number> get("$.id")).isEqualTo(13);
        assertThat(json.<String> get("$.name")).isEqualTo("Test rule");
        assertThat(json.<String> get("$.comment")).isEqualTo("comment");
        assertThat(json.<String> get("$.reason.id")).isEqualTo("1");
        assertThat(json.<String> get("$.reason.name")).isEqualTo("Reason");
        assertThat(json.<String> get("$.template.name")).isEqualTo("Template");
        assertThat(json.<String> get("$.template.description")).isEqualTo("Template description");
        assertThat(json.<String> get("$.template.properties[0].key")).isEqualTo("property");
        assertThat(json.<Number> get("$.dueIn.number")).isEqualTo(5);
        assertThat(json.<String> get("$.dueIn.type")).isEqualTo("days");
        assertThat(json.<List<?>> get("$.properties")).hasSize(1);
        assertThat(json.<String> get("$.properties[0].key")).isEqualTo("property");
        assertThat(json.<String> get("$.properties[0].propertyValueInfo.value")).isEqualTo("value_for_rule");

        assertThat(json.<List<?>> get("$.actions")).hasSize(1);
        assertThat(json.<String> get("$.actions[0].phase.uuid")).isEqualTo("CREATE");
        assertThat(json.<String> get("$.actions[0].type.name")).isEqualTo("send");
        assertThat(json.<List<?>> get("$.actions[0].properties")).hasSize(1);
        assertThat(json.<String> get("$.actions[0].properties[0].key")).isEqualTo("property");
        assertThat(json.<String> get("$.actions[0].properties[0].propertyValueInfo.value")).isEqualTo("value_for_action");
    }

    @Test
    public void testAddCreationRule() {
        TransactionContext context = mock(TransactionContext.class);
        when(transactionService.getContext()).thenReturn(context);
        CreationRule rule = mock(CreationRule.class);
        CreationRuleBuilder builder = mock(CreationRuleBuilder.class);
        when(issueCreationService.newCreationRule()).thenReturn(builder);
        when(builder.setName("rule name")).thenReturn(builder);
        when(builder.setComment("comment")).thenReturn(builder);
        when(builder.setDueInTime(DueInType.DAY, 5L)).thenReturn(builder);
        CreationRuleActionBuilder actionBuilder = mock(CreationRuleActionBuilder.class);
        when(builder.newCreationRuleAction()).thenReturn(actionBuilder);
        when(builder.setTemplate("Template")).thenReturn(builder);
        when(builder.complete()).thenReturn(rule);
        when(actionBuilder.setPhase(CreationRuleActionPhase.CREATE)).thenReturn(actionBuilder);
        when(actionBuilder.addProperty("property2", "value")).thenReturn(actionBuilder);

        CreationRuleTemplate template = mock(CreationRuleTemplate.class);
        List<PropertySpec> propertySpecs = mockPropertySpecs("property1", "property2");
        when(template.getPropertySpecs()).thenReturn(propertySpecs);
        when(issueCreationService.findCreationRuleTemplate("Template")).thenReturn(Optional.of(template));

        IssueActionType actionType = mock(IssueActionType.class);
        when(issueActionService.findActionType(5L)).thenReturn(Optional.of(actionType));
        IssueAction action = mock(IssueAction.class);
        when(actionType.createIssueAction()).thenReturn(Optional.of(action));
        propertySpecs = mockPropertySpecs("property1", "property2");
        when(action.getPropertySpecs()).thenReturn(propertySpecs);

        when(issueService.findReason("reason")).thenReturn(Optional.empty());

        CreationRuleInfo info = new CreationRuleInfo();
        info.name = "rule name";
        info.comment = "comment";
        info.dueIn = new DueInInfo("days", 5L);
        info.reason = new IssueReasonInfo();
        info.reason.id = "reason";
        info.template = new CreationRuleTemplateInfo();
        info.template.name = "Template";
        PropertyInfo rulePropertyInfo = new PropertyInfo();
        rulePropertyInfo.key = "property1";
        rulePropertyInfo.propertyValueInfo = new PropertyValueInfo<String>("value", null);
        info.properties = Arrays.asList(rulePropertyInfo);
        CreationRuleActionInfo actionInfo = new CreationRuleActionInfo();
        actionInfo.phase = new CreationRuleActionPhaseInfo(CreationRuleActionPhase.CREATE, thesaurus);
        actionInfo.type = new IssueActionTypeInfo();
        actionInfo.type.id = 5L;
        PropertyInfo actionPropertyInfo = new PropertyInfo();
        actionPropertyInfo.key = "property2";
        actionPropertyInfo.propertyValueInfo = new PropertyValueInfo<String>("value", null);
        actionInfo.properties = Arrays.asList(actionPropertyInfo);
        info.actions = Arrays.asList(actionInfo);

        Response response = target("/creationrules").request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(actionBuilder).complete();
        verify(rule).save();
        verify(issueCreationService).reReadRules();
        verify(context).commit();
    }

    @Test
    public void testEditCreationRule() {
        TransactionContext context = mock(TransactionContext.class);
        when(transactionService.getContext()).thenReturn(context);
        CreationRule rule = mock(CreationRule.class);
        CreationRuleUpdater updater = mock(CreationRuleUpdater.class);
        when(rule.startUpdate()).thenReturn(updater);
        when(issueCreationService.findAndLockCreationRuleByIdAndVersion(13L, 5L)).thenReturn(Optional.of(rule));
        when(updater.setName("rule name")).thenReturn(updater);
        when(updater.setComment("comment")).thenReturn(updater);
        when(updater.setDueInTime(DueInType.DAY, 5L)).thenReturn(updater);
        CreationRuleActionBuilder actionBuilder = mock(CreationRuleActionBuilder.class);
        when(updater.newCreationRuleAction()).thenReturn(actionBuilder);
        when(updater.setTemplate("Template")).thenReturn(updater);
        when(updater.complete()).thenReturn(rule);
        when(actionBuilder.setPhase(CreationRuleActionPhase.CREATE)).thenReturn(actionBuilder);
        when(actionBuilder.addProperty("property2", "value")).thenReturn(actionBuilder);

        CreationRuleTemplate template = mock(CreationRuleTemplate.class);
        List<PropertySpec> propertySpecs = mockPropertySpecs("property1", "property2");
        when(template.getPropertySpecs()).thenReturn(propertySpecs);
        when(issueCreationService.findCreationRuleTemplate("Template")).thenReturn(Optional.of(template));

        IssueActionType actionType = mock(IssueActionType.class);
        when(issueActionService.findActionType(5L)).thenReturn(Optional.of(actionType));
        IssueAction action = mock(IssueAction.class);
        when(actionType.createIssueAction()).thenReturn(Optional.of(action));
        propertySpecs = mockPropertySpecs("property1", "property2");
        when(action.getPropertySpecs()).thenReturn(propertySpecs);

        when(issueService.findReason("reason")).thenReturn(Optional.empty());

        CreationRuleInfo info = new CreationRuleInfo();
        info.name = "rule name";
        info.comment = "comment";
        info.dueIn = new DueInInfo("days", 5L);
        info.reason = new IssueReasonInfo();
        info.reason.id = "reason";
        info.template = new CreationRuleTemplateInfo();
        info.template.name = "Template";
        PropertyInfo rulePropertyInfo = new PropertyInfo();
        rulePropertyInfo.key = "property1";
        rulePropertyInfo.propertyValueInfo = new PropertyValueInfo<String>("value", null);
        info.properties = Arrays.asList(rulePropertyInfo);
        CreationRuleActionInfo actionInfo = new CreationRuleActionInfo();
        actionInfo.phase = new CreationRuleActionPhaseInfo(CreationRuleActionPhase.CREATE, thesaurus);
        actionInfo.type = new IssueActionTypeInfo();
        actionInfo.type.id = 5L;
        PropertyInfo actionPropertyInfo = new PropertyInfo();
        actionPropertyInfo.key = "property2";
        actionPropertyInfo.propertyValueInfo = new PropertyValueInfo<String>("value", null);
        actionInfo.properties = Arrays.asList(actionPropertyInfo);
        info.actions = Arrays.asList(actionInfo);
        info.version = 5L;
        info.id = 13L;

        Response response = target("/creationrules/13").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(actionBuilder).complete();
        verify(updater).removeActions();
        verify(rule).save();
        verify(issueCreationService).reReadRules();
        verify(context).commit();
    }

    @Test
    public void testDeleteCreationRule() {
        TransactionContext context = mock(TransactionContext.class);
        when(transactionService.getContext()).thenReturn(context);
        CreationRule rule = mock(CreationRule.class);
        when(issueCreationService.findAndLockCreationRuleByIdAndVersion(13L, 5L)).thenReturn(Optional.of(rule));

        CreationRuleInfo info = new CreationRuleInfo();
        info.id = 13L;
        info.version = 5L;
        
        Response response = target("/creationrules/13").request().method("DELETE", Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(rule).delete();
        verify(issueCreationService).reReadRules();
        verify(context).commit();
    }
    
    @Test
    public void testValidateAction() {
        CreationRuleBuilder ruleBuilder = mock(CreationRuleBuilder.class);
        when(issueCreationService.newCreationRule()).thenReturn(ruleBuilder);
        CreationRuleActionBuilder actionBuilder = mock(CreationRuleActionBuilder.class);
        when(ruleBuilder.newCreationRuleAction()).thenReturn(actionBuilder);
        CreationRuleAction action = mock(CreationRuleAction.class);
        when(actionBuilder.complete()).thenReturn(action);

        CreationRuleActionInfo info = new CreationRuleActionInfo();
        
        Response response = target("/creationrules/validateaction").request().post(Entity.json(info));
        
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        verify(action).validate();
    }

    protected List<PropertySpec> mockPropertySpecs(String... keys) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        for (String key : keys) {
            PropertySpec propertySpec = mock(PropertySpec.class);
            when(propertySpec.getName()).thenReturn(key);
            when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
            when(propertySpec.isRequired()).thenReturn(true);
            propertySpecs.add(propertySpec);
        }
        return propertySpecs;
    }
}
