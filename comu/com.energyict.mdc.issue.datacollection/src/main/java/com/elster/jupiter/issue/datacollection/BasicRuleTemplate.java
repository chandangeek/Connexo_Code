package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplateParameter;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.List;

/**
 * This class is an example of implementation for CreationRuleTemplate Parameter interface.
 */
@Component(name = "com.elster.jupiter.issue.datacollection.BasicRuleTemplate", property = {"uuid=" + BasicRuleTemplate.BASIC_TEMPLATE_UUID}, service = CreationRuleTemplate.class, immediate = true)
public class BasicRuleTemplate implements CreationRuleTemplate {
    public static final String BASIC_TEMPLATE_UUID = "e29b-41d4-a716";

    @Override
    public String getUUID() {
        return BasicRuleTemplate.BASIC_TEMPLATE_UUID;
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
        "rule \"" + getName() + "\n" +
        "when \n" +
        "    $event: DataCollectionIssueEvent( topic == @{eventTopic} )\n" +
        "then \n"+
        "    RuleExecutor.createIssue( @{ruleId}, $event ); \n" +
        "end";
    }

    @Override
    public List<CreationRuleTemplateParameter> getParameters() {
        CreationRuleTemplateParameter parameter = new IssueEventTopicParameter();
        return Collections.singletonList(parameter);
    }
}
