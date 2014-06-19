package com.elster.jupiter.issue.datacollection.templates;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.datacollection.templates.params.EventTypeParameter;
import com.elster.jupiter.issue.datacollection.templates.params.ThresholdParameter;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.issue.datacollection.EventAggregationRuleTemplate", property = {"uuid=" + EventAggregationRuleTemplate.TEMPLATE_UUID}, service = CreationRuleTemplate.class, immediate = true)
public class EventAggregationRuleTemplate extends AbstractTemplate {
    
    public static final String TEMPLATE_UUID = "2f20a62e-3361-33c9-afc4-2f68618a6af7";

    private volatile MeteringService meteringService;
    @Activate
    public void activate(){
        addParameterDefinition(new EventTypeParameter(getThesaurus(), meteringService));
        addParameterDefinition(new ThresholdParameter(getThesaurus()));
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
        return TEMPLATE_UUID;
    }

    @Override
    public String getName() {
        return getString(MessageSeeds.TEMPLATE_EVT_AGGREGATION_NAME);
    }

    @Override
    public String getDescription() {
        return getString(MessageSeeds.TEMPLATE_EVT_AGGREGATION_DESCRIPTION);
    }

    @Override
    public String getContent() {
        return  "package com.elster.jupiter.issue.datacollection\n" +
                "import com.elster.jupiter.issue.datacollection.impl.AbstractEvent;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Events from meters of concentrator @{ruleId}\"\n"+
                "when\n"+
                "\tevent : AbstractEvent( eventType == \"@{eventType}\" )\n"+
                "\teval( event.computeCurrentThreshold() > @{threshold} )\n"+
                "then\n"+
                "\tSystem.out.println(\"Events from meters of concentrator @{ruleId}\");\n"+
                "\tissueCreationService.processCreationEvent(@{ruleId}, event);\n"+
                "end";
    }
}
