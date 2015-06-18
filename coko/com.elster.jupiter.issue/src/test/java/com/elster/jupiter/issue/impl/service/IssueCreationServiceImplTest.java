package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.IssueActionTypeImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.util.conditions.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IssueCreationServiceImplTest extends BaseTest {
    
    private Map<String, PropertySpec> propertySpecs;
    private PropertySpec decimalProp;
    private PropertySpec stringProp;
    
    private CreationRuleTemplate template;
    
    private IssueActionType actionType;
    
    @Before
    public void setUp() throws Exception {
        decimalProp = getPropertySpecService().basicPropertySpec("decimal_property", true, new BigDecimalFactory());
        stringProp = getPropertySpecService().basicPropertySpec("string_property", false, new StringFactory());
        propertySpecs = new HashMap<>();
        propertySpecs.put(decimalProp.getName(), decimalProp);
        propertySpecs.put(stringProp.getName(), stringProp);
        
        template = mockCreationRuleTemplate();
        ((IssueServiceImpl)getIssueService()).addCreationRuleTemplate(template);
        IssueActionFactory issueActionFactory = mockIssueActionFactory("action");
        ((IssueServiceImpl)getIssueService()).addIssueActionFactory(issueActionFactory);
        mockIssueAction();
        
        IssueActionTypeImpl actionTypeImpl = getDataModel().getInstance(IssueActionTypeImpl.class);
        actionTypeImpl.init(issueActionFactory.getId(), "action", getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).get(), CreationRuleActionPhase.CREATE);
        actionTypeImpl.save();
        actionType = actionTypeImpl;
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "name", strict = true)
    public void testCreateRuleNoName() {
        getSimpleCreationRule(null, template);
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80+ "}", property = "name", strict = true)
    public void testCreateRuleWithTooLongName() {
        getSimpleCreationRule("-aaaaaaaaaabbbbbbbbbbccccccccccddddddddddaaaaaaaaaabbbbbbbbbbccccccccccdddddddddd", template);
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "reason", strict = true)
    public void testCreateRuleNoReason() {
        CreationRuleBuilder builder = getIssueCreationService().newCreationRule();
        builder.setName("Name");
        builder.setIssueType(getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).orElse(null));
        builder.setTemplate(template.getName());
        Map<String, Object> props = new HashMap<>();
        props.put("decimal_property", BigDecimal.valueOf(10));
        props.put("string_property", "string");
        builder.setProperties(props);
        CreationRule rule = builder.complete();
        rule.save();
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "template", strict = true)
    public void testCreateRuleNoTemplate() {
        CreationRuleBuilder builder = getIssueCreationService().newCreationRule();
        builder.setName("Name");
        builder.setIssueType(getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).orElse(null));
        builder.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        builder.setTemplate(null);
        CreationRule rule = builder.complete();
        rule.save();
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_MISSING +"}", property = "properties.decimal_property", strict = true)
    public void testCreateRuleNoMandatoryProperties() {
        CreationRuleBuilder builder = getIssueCreationService().newCreationRule();
        builder.setName("Name");
        builder.setIssueType(getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).orElse(null));
        builder.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        builder.setTemplate(template.getName());
        CreationRule rule = builder.complete();
        rule.save();
    }
    
    @Test
    @Transactional
    public void testFindCreationRule() {
        CreationRule creationRule = getSimpleCreationRule("Creation Rule", template);
        
        Optional<CreationRule> foundCreationRule = getIssueCreationService().findCreationRuleById(creationRule.getId());
        
        assertThat(foundCreationRule.isPresent()).isTrue();
        assertThat(foundCreationRule.get().getId()).isEqualTo(creationRule.getId());
        assertThat(foundCreationRule.get().getName()).isEqualTo("Creation Rule");
        assertThat(foundCreationRule.get().getComment()).isEqualTo(creationRule.getComment());
        assertThat(foundCreationRule.get().getContent()).isEqualTo(creationRule.getContent());
        assertThat(foundCreationRule.get().getData()).isEqualTo(creationRule.getData());
        assertThat(foundCreationRule.get().getReason().getId()).isEqualTo(creationRule.getReason().getId());
        assertThat(foundCreationRule.get().getDueInType()).isEqualTo(creationRule.getDueInType());
        assertThat(foundCreationRule.get().getDueInValue()).isEqualTo(creationRule.getDueInValue());
        assertThat(foundCreationRule.get().getTemplateImpl()).isEqualTo(creationRule.getTemplateImpl());
        assertThat(foundCreationRule.get().getTemplate().getDisplayName()).isEqualTo(creationRule.getTemplate().getDisplayName());
        assertThat(foundCreationRule.get().getPropertySpecs()).hasSize(2);
        assertThat(foundCreationRule.get().getProperties()).hasSize(2);
        assertThat(foundCreationRule.get().getActions()).isEmpty();
        assertThat(foundCreationRule.get().getObsoleteTime()).isNull();
    }
    
    @Test
    @Transactional
    public void testEditProperties() {
        //create rule with two properties
        CreationRule rule = getSimpleCreationRule("Creation Rule", template);
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        
        List<CreationRuleProperty> properties = rule.getCreationRuleProperties();
        
        assertThat(properties).hasSize(2);
        assertThat(properties.get(0).getName()).isEqualTo("string_property");
        assertThat(properties.get(0).getValue()).isEqualTo("string");
        assertThat(properties.get(1).getName()).isEqualTo("decimal_property");
        assertThat(properties.get(1).getValue()).isEqualTo(BigDecimal.valueOf(10));

        //update both properties
        Map<String, Object> props = new HashMap<>();
        props.put("string_property", "new string");
        props.put("decimal_property", BigDecimal.valueOf(12));
        rule.startUpdate().setProperties(props).complete();
        rule.save();
        
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        properties = rule.getCreationRuleProperties();
        assertThat(properties).hasSize(2);
        assertThat(properties.get(0).getName()).isEqualTo("string_property");
        assertThat(properties.get(0).getValue()).isEqualTo("new string");
        assertThat(properties.get(1).getName()).isEqualTo("decimal_property");
        assertThat(properties.get(1).getValue()).isEqualTo(BigDecimal.valueOf(12));
        
        //update one property and remove another one
        props = new HashMap<>();
        props.put("decimal_property", BigDecimal.valueOf(15));
        
        rule.startUpdate().setProperties(props).complete();
        rule.save();
        
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        properties = rule.getCreationRuleProperties();
        assertThat(properties).hasSize(1);
        assertThat(properties.get(0).getName()).isEqualTo("decimal_property");
        assertThat(properties.get(0).getValue()).isEqualTo(BigDecimal.valueOf(15));
        
        //add property
        props = new HashMap<>();
        props.put("decimal_property", BigDecimal.valueOf(17));
        props.put("string_property", "string again");
        
        rule.startUpdate().setProperties(props).complete();
        rule.save();
        
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        properties = rule.getCreationRuleProperties();
        assertThat(properties).hasSize(2);
        assertThat(properties.get(0).getName()).isEqualTo("string_property");
        assertThat(properties.get(0).getValue()).isEqualTo("string again");
        assertThat(properties.get(1).getName()).isEqualTo("decimal_property");
        assertThat(properties.get(1).getValue()).isEqualTo(BigDecimal.valueOf(17));
    }
    
    @Test
    @Transactional
    public void testUpdateContent() {
        CreationRule rule = getSimpleCreationRule("Creation rule", template);
        
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        assertThat(rule.getContent()).isEqualTo("bla bla bla 10 bla bla string bla " + rule.getId());

        Map<String, Object> props = new HashMap<>();
        props.put("string_property", "new string");
        props.put("decimal_property", BigDecimal.valueOf(12));
        rule.startUpdate().setProperties(props).complete();
        rule.save();
        
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        assertThat(rule.getContent()).isEqualTo("bla bla bla 12 bla bla new string bla " + rule.getId());        
    }

    @Test
    @Transactional
    public void testCreationRuleDelete() {
        // Simple deletion
        CreationRule rule = getSimpleCreationRule("Creation Rule 1", template);
        rule.delete();
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        assertThat(rule).isNull();

        // Delete when some issue has reference
        rule = getSimpleCreationRule("Creation Rule 2", template);
        OpenIssue issue = getDataModel().getInstance(OpenIssueImpl.class);
        issue.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        issue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).orElse(null));
        issue.setRule(rule);
        issue.save();
        rule.delete();
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        assertThat(rule).isNotNull();
        assertThat(rule.getObsoleteTime()).isNotNull();

        // Delete when closed issue has reference
        rule = getSimpleCreationRule("Creation Rule 3", template);
        issue.setRule(rule);
        issue.close(getIssueService().findStatus(IssueStatus.WONT_FIX).orElse(null));
        rule.delete();
        assertThat(rule).isNotNull();
        assertThat(rule.getObsoleteTime()).isNotNull();

        // Check all rules after deletion
        List<CreationRule> rules = getIssueCreationService().getCreationRuleQuery().select(Condition.TRUE);
        for (CreationRule creationRule : rules) {
            assertThat(creationRule.getObsoleteTime()).isNull();
        }
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CREATION_RULE_UNIQUE_NAME + "}", property = "name", strict = true)
    public void testRuleUniqueName() {
        getSimpleCreationRule("NonUniqueName", template);
        getSimpleCreationRule("NonUniqueName", template);
    }

    @Test
    @Transactional
    public void testRuleUniqueNameNoExceptionInCaseSensitiveCheck() {
        getSimpleCreationRule("NonUniqueName".toUpperCase(), template);
        getSimpleCreationRule("NonUniqueName".toLowerCase(), template);
    }

    @Test
    @Transactional
    public void testRuleActions() {
        CreationRule rule = getSimpleCreationRule("Creation rule", template);

        rule.startUpdate()
            .newCreationRuleAction()
            .setActionType(actionType)
            .setPhase(CreationRuleActionPhase.CREATE)
            .addProperty("decimal_property", BigDecimal.valueOf(10))
            .addProperty("string_property", "string")
            .complete();
        rule.save();

        CreationRule foundRule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);

        assertThat(foundRule.getActions()).hasSize(1);
        CreationRuleAction creationRuleAction = foundRule.getActions().get(0);
        assertThat(creationRuleAction.getRule().getId()).isEqualTo(rule.getId());
        assertThat(creationRuleAction.getAction().getId()).isEqualTo(actionType.getId());
        assertThat(creationRuleAction.getProperties()).hasSize(2);
        assertThat(creationRuleAction.getCreationRuleActionProperties().get(0).getName()).isEqualTo("decimal_property");
        assertThat(creationRuleAction.getCreationRuleActionProperties().get(0).getValue()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(creationRuleAction.getCreationRuleActionProperties().get(1).getName()).isEqualTo("string_property");
        assertThat(creationRuleAction.getCreationRuleActionProperties().get(1).getValue()).isEqualTo("string");
    }
    
    @Test
    @Transactional
    public void testEditActions() {
        CreationRule rule = getSimpleCreationRule("Creation rule", template);

        rule.startUpdate()
            .newCreationRuleAction()
            .setPhase(CreationRuleActionPhase.CREATE)
            .setActionType(actionType)
            .addProperty("decimal_property", BigDecimal.valueOf(10))
            .addProperty("string_property", "string")
            .complete();
        rule.save();
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        assertThat(rule.getActions()).hasSize(1);
        
        rule.startUpdate()
            .removeActions()
            .complete()
            .save();
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        assertThat(rule.getActions()).isEmpty();
        
        rule.startUpdate()
            .newCreationRuleAction()
            .setActionType(actionType)
            .setPhase(CreationRuleActionPhase.OVERDUE)
            .addProperty("decimal_property", BigDecimal.valueOf(10))
            .complete();
        rule.save();
        rule = getIssueCreationService().findCreationRuleById(rule.getId()).orElse(null);
        assertThat(rule.getActions()).hasSize(1);
        assertThat(rule.getActions().get(0).getPhase()).isEqualTo(CreationRuleActionPhase.OVERDUE);
        assertThat(rule.getActions().get(0).getAction().getId()).isEqualTo(actionType.getId());
        assertThat(rule.getActions().get(0).getProperties()).hasSize(1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "actions[0].phase", strict = true)
    public void testRuleActionNoPhase() {
        CreationRule rule = getSimpleCreationRule("Creation rule", template);
        
        rule.startUpdate()
            .newCreationRuleAction()
            .setActionType(actionType)
            .addProperty("decimal_property", BigDecimal.valueOf(10))
            .complete();
        rule.save();
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "actions[0].type", strict = true)
    public void testRuleActionNoActionType() {
        CreationRule rule = getSimpleCreationRule("Creation rule", template);
        
        rule.startUpdate()
            .newCreationRuleAction()
            .setPhase(CreationRuleActionPhase.CREATE)
            .complete();
        rule.save();
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_MISSING +"}", property = "actions[0].properties.decimal_property", strict = true)
    public void testRuleActionNoProperties() {
        CreationRule rule = getSimpleCreationRule("Creation rule", template);
        
        rule.startUpdate()
            .newCreationRuleAction()
            .setActionType(actionType)
            .setPhase(CreationRuleActionPhase.CREATE)
            .complete();
        rule.save();
    }
    
    @Test
    @Transactional
    public void testCreationRuleTemplates() {
        assertThat(getIssueCreationService().getCreationRuleTemplates()).hasSize(1);
        
        Optional<CreationRuleTemplate> fakeTemplate = getIssueCreationService().findCreationRuleTemplate("fake");
        assertThat(fakeTemplate.isPresent()).isFalse();

        assertThat(getIssueCreationService().reReadRules()).isTrue();

        ((IssueServiceImpl)getIssueService()).removeCreationRuleTemplate(template);
        
        assertThat(getIssueCreationService().getCreationRuleTemplates()).isEmpty();
    }

    @Test
    @Transactional
    public void testCreationEvents() {
        CreationRule rule = getSimpleCreationRule("Creation Rule", template);
        
        IssueCreationServiceImpl impl = IssueCreationServiceImpl.class.cast(getIssueCreationService());
        IssueEvent event = getMockIssueEvent();
        impl.dispatchCreationEvent(Collections.singletonList(event));

        impl.processIssueCreationEvent(rule.getId(), event);

        Query<? extends Issue> query = getIssueService().query(OpenIssue.class, IssueReason.class);
        Condition condition = where("reason").isEqualTo(rule.getReason());
        List<? extends Issue> list = query.select(condition);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0).getRule().getId()).isEqualTo(rule.getId());
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY +"}", property = "phase", strict = true)
    public void testValidateCreationRuleActionNoPhase() {
        getIssueCreationService().newCreationRule()
                                 .newCreationRuleAction()
                                 .setActionType(actionType)
                                 .addProperty("decimal_property", BigDecimal.valueOf(10))
                                 .complete()
                                 .validate();
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY +"}", property = "type", strict = true)
    public void testValidateCreationRuleActionNoActionType() {
        getIssueCreationService().newCreationRule()
                                 .newCreationRuleAction()
                                 .setPhase(CreationRuleActionPhase.CREATE)
                                 .complete()
                                 .validate();
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_MISSING +"}", property = "properties.decimal_property", strict = true)
    public void testValidateCreationRuleActionNoMandatoryProperties() {
        getIssueCreationService().newCreationRule()
                                 .newCreationRuleAction()
                                 .setActionType(actionType)
                                 .setPhase(CreationRuleActionPhase.CREATE)
                                 .complete()
                                 .validate();
    }
    
    private CreationRuleTemplate mockCreationRuleTemplate() {
        CreationRuleTemplate template = mock(CreationRuleTemplate.class);
        when(template.getName()).thenReturn("Creation rule template");
        when(template.getPropertySpecs()).thenReturn(Arrays.asList(decimalProp, stringProp));
        when(template.getContent()).thenReturn("bla bla bla @{decimal_property} bla bla @{string_property} bla @{ruleId}");
        IssueType issueType = getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).get();
        when(template.getIssueType()).thenReturn(issueType);
        when(template.createIssue(Matchers.any(), Matchers.any())).thenAnswer(invocation -> {
                invocation.getArgumentAt(0, Issue.class).save();
                return Optional.of(invocation.getArgumentAt(0, Issue.class));
            });
        return template;
    }
    
    private IssueActionFactory mockIssueActionFactory(String actionName) {
        IssueActionFactory issueActionFactory = mock(IssueActionFactory.class);
        when(issueActionFactory.getId()).thenReturn("test issue action factory");
        IssueAction issueAction = mockIssueAction();
        when(issueActionFactory.createIssueAction(actionName)).thenReturn(issueAction);
        return issueActionFactory;
    }
    
    private IssueAction mockIssueAction() {
        IssueAction issueAction = mock(IssueAction.class);
        when(issueAction.getPropertySpecs()).thenReturn(Arrays.asList(decimalProp, stringProp));
        return issueAction;
    }
    
    private CreationRule getSimpleCreationRule(String name, CreationRuleTemplate template) {
        CreationRuleBuilder builder = getIssueCreationService().newCreationRule();
        builder.setName(name);
        builder.setComment("Comment for rule");
        builder.setIssueType(getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).orElse(null));
        builder.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        builder.setDueInTime(DueInType.DAY, 15L);
        builder.setTemplate(template.getName());
        Map<String, Object> props = new HashMap<>();
        props.put("decimal_property", BigDecimal.valueOf(10));
        props.put("string_property", "string");
        builder.setProperties(props);
        CreationRule rule = builder.complete();
        rule.save();
        return rule;
    }
}
