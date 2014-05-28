package com.elster.jupiter.issue.datacollection.templates;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.datacollection.templates.params.IssueEventTopicParameter;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.issue.datacollection.BasicDatacollectionRuleTemplate", property = {"uuid=" + BasicDatacollectionRuleTemplate.BASIC_TEMPLATE_UUID}, service = CreationRuleTemplate.class, immediate = true)
public class BasicDatacollectionRuleTemplate extends AbstractTemplate {
    public static final String BASIC_TEMPLATE_UUID = "e29b-41d4-a716";

    @Activate
    public void activate(){
        addParameterDefinition(new IssueEventTopicParameter(getThesaurus()));
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        setThesaurus(nlsService.getThesaurus(ModuleConstants.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Override
    public String getUUID() {
        return BasicDatacollectionRuleTemplate.BASIC_TEMPLATE_UUID;
    }

    @Override
    public String getName() {
        return getString(MessageSeeds.BASIC_TEMPLATE_DATACOLLECTION_NAME);
    }

    @Override
    public String getDescription() {
        return getString(MessageSeeds.BASIC_TEMPLATE_DATACOLLECTION_DESCRIPTION);
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
}
