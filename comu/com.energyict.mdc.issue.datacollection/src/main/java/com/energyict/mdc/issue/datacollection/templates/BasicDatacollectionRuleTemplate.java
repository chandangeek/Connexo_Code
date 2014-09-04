package com.energyict.mdc.issue.datacollection.templates;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.templates.params.EventTypeParameter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.issue.datacollection.BasicDatacollectionRuleTemplate", property = {"uuid=" + BasicDatacollectionRuleTemplate.BASIC_TEMPLATE_UUID}, service = CreationRuleTemplate.class, immediate = true)
public class BasicDatacollectionRuleTemplate extends AbstractTemplate {

    public static final String BASIC_TEMPLATE_UUID = "e29b-41d4-a716";

    private volatile MeteringService meteringService;

    @Activate
    public void activate(){
        addParameterDefinition(new EventTypeParameter(getThesaurus(), meteringService));
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        setThesaurus(nlsService.getThesaurus(ModuleConstants.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
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
        return "package com.energyict.mdc.issue.datacollection\n" +
                "import com.energyict.mdc.issue.datacollection.impl.AbstractEvent;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Basic datacollection rule @{ruleId}\"\n"+
                "when\n"+
                "\tevent : AbstractEvent( eventType == \"@{eventType}\" )\n"+
                "then\n"+
                "\tSystem.out.println(\"Basic datacollection rule @{ruleId}\");\n"+
                "\tissueCreationService.processCreationEvent(@{ruleId}, event);\n"+
                "end";
    }
}
