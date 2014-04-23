package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplateParameter;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.List;

@Component(name = "com.elster.jupiter.issue.datacollection.BasicMeterRuleTemplate", property = {"uuid=" + BasicMeterRuleTemplate.BASIC_TEMPLATE_UUID}, service = CreationRuleTemplate.class, immediate = true)
public class BasicMeterRuleTemplate implements CreationRuleTemplate {
    public static final String BASIC_TEMPLATE_UUID = "e29b-41d4-a717";

    @Override
    public String getUUID() {
        return BasicMeterRuleTemplate.BASIC_TEMPLATE_UUID;
    }

    @Override
    public String getName() {
        return "Basic meter issues";
    }

    @Override
    public String getDescription() {
        return "Creates issue based on specified meter ";
    }

    @Override
    public String getContent() {
        return
            "package com.elster.jupiter.issue.datacollection\n" +
            "import com.elster.jupiter.issue.datacollection.MeterIssueEvent;\n" +
            "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
            "rule \"Basic meter issues @{ruleId}\"\n"+
            "when\n"+
            "\tevent : MeterIssueEvent( endDeviceEventType == \"@{endDeviceEventType}\" )\n"+
            "then\n"+
            "\tSystem.out.println(\"Basic meter issues @{ruleId}\");\n"+
            "\tissueCreationService.processCreationEvent(@{ruleId}, event);\n"+
            "end";
    }

    @Override
    public String getIssueType() {
        return ModuleConstants.ISSUE_TYPE_UUID;
    }

    @Override
    public List<CreationRuleTemplateParameter> getParameters() {
        CreationRuleTemplateParameter parameter = new EndDeviceEventTypeParameter();
        return Collections.singletonList(parameter);
    }
}
