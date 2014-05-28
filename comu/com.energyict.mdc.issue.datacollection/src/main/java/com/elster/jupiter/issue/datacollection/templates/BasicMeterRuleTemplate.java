package com.elster.jupiter.issue.datacollection.templates;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.datacollection.templates.params.EndDeviceEventTypeParameter;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.issue.datacollection.BasicMeterRuleTemplate", property = {"uuid=" + BasicMeterRuleTemplate.METER_TEMPLATE_ID}, service = CreationRuleTemplate.class, immediate = true)
public class BasicMeterRuleTemplate extends AbstractTemplate {
    public static final String METER_TEMPLATE_ID = "e29b-41d4-a717";

    @Activate
    public void activate(){
        addParameterDefinition(new EndDeviceEventTypeParameter(getThesaurus()));
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        setThesaurus(nlsService.getThesaurus(ModuleConstants.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Override
    public String getUUID() {
        return BasicMeterRuleTemplate.METER_TEMPLATE_ID;
    }

    @Override
    public String getName() {
        return getString(MessageSeeds.METER_TEMPLATE_NAME);
    }

    @Override
    public String getDescription() {
        return getString(MessageSeeds.METER_TEMPLATE_DESCRIPTION);
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
}
