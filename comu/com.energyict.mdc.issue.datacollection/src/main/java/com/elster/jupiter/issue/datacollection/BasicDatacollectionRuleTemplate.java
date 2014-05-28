package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.install.MessageSeeds;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.cep.ParameterDefinitionContext;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleParameter;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.issue.datacollection.BasicDatacollectionRuleTemplate", property = {"uuid=" + BasicDatacollectionRuleTemplate.BASIC_TEMPLATE_UUID}, service = CreationRuleTemplate.class, immediate = true)
public class BasicDatacollectionRuleTemplate implements CreationRuleTemplate {
    public static final String BASIC_TEMPLATE_UUID = "e29b-41d4-a716";

    private Map<String, ParameterDefinition> parameterDefinitions;

    public BasicDatacollectionRuleTemplate() {
        this.parameterDefinitions = new HashMap<>();

        ParameterDefinition eventType =  new IssueEventTopicParameter();
        parameterDefinitions.put(eventType.getKey(), eventType);
    }

    @Override
    public String getUUID() {
        return BasicDatacollectionRuleTemplate.BASIC_TEMPLATE_UUID;
    }

    @Override
    public String getName() {
        return "Basic data collection issues";
    }

    @Override
    public String getDescription() {
        return "Creates issue based on specified issue datacollection event topic";
    }

    @Override
    public String getContent() {
        return
            "package com.elster.jupiter.issue.datacollection\n" +
            "import com.elster.jupiter.issue.datacollection.DataCollectionEvent;\n" +
            "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
            "rule \"Basic data collection issues @{ruleId}\"\n"+
            "when\n"+
            "\tevent : DataCollectionEvent( eventType == \"@{eventTopic}\" )\n"+
            "then\n"+
            "\tSystem.out.println(\"Basic data collection issues @{ruleId}\");\n"+
            "\tissueCreationService.processCreationEvent(@{ruleId}, event);\n"+
            "end";
    }

    @Override
    public String getIssueType() {
        return ModuleConstants.ISSUE_TYPE_UUID;
    }

    @Override
    public Map<String, ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    @Override
    public List<ParameterViolation> validate(CreationRule rule) {
        List<ParameterViolation> errors = new ArrayList<>();
        if(rule == null){
            throw new IllegalArgumentException("Rule is missing");
        }

        Map<String, ParameterDefinition> parameterDefinitionsCopy = new HashMap<>(parameterDefinitions);
        for (CreationRuleParameter parameter : rule.getParameters()) {
            ParameterDefinition definition = parameterDefinitionsCopy.remove(parameter.getKey());
            errors.addAll(definition.validate(parameter.getValue(), ParameterDefinitionContext.RULE));
        }
        for (ParameterDefinition definition : parameterDefinitionsCopy.values()) {
            if (!definition.getConstraint().isOptional()) {
                errors.add(new ParameterViolation(ParameterDefinitionContext.RULE.wrapKey(definition.getKey()), MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT.getKey(), ModuleConstants.COMPONENT_NAME));
            }
        }
        return errors;
    }
}
