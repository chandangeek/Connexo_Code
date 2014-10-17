package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.impl.templates.params.MaxSlopeParameter;
import com.energyict.mdc.issue.datacollection.impl.templates.params.ReadingTypeParameter;
import com.energyict.mdc.issue.datacollection.impl.templates.params.TrendPeriodParameter;
import com.energyict.mdc.issue.datacollection.impl.templates.params.TrendPeriodUnitParameter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.issue.datacollection.SlopeDetectionRuleTemplate",
        property = {"uuid=" + MeterReadingTemplate.SLOPE_DETECTION_ID},
        service = CreationRuleTemplate.class, immediate = true)
public class MeterReadingTemplate extends AbstractTemplate {
    public static final String SLOPE_DETECTION_ID = "7b1c7ccc-f248-47c6-81f3-18d123870133";

    private volatile MeteringService meteringService;

    @Activate
    @SuppressWarnings("unused")
    public void activate() {
        addParameterDefinition(new ReadingTypeParameter(getThesaurus()));
        addParameterDefinition(new TrendPeriodParameter(getThesaurus()));
        addParameterDefinition(new MaxSlopeParameter(getThesaurus(), meteringService));
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        setThesaurus(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public String getUUID() {
        return MeterReadingTemplate.SLOPE_DETECTION_ID;
    }

    @Override
    public String getName() {
        return getString(MessageSeeds.SLOPE_DETECTION_TEMPLATE_NAME);
    }

    @Override
    public String getDescription() {
        return getString(MessageSeeds.SLOPE_DETECTION_TEMPLATE_DESCRIPTION);
    }

    @Override
    public Map<String, ParameterDefinition> getParameterDefinitionsForValidation() {
        Map<String, ParameterDefinition> copy = super.getParameterDefinitionsForValidation();
        TrendPeriodUnitParameter unitParameter = new TrendPeriodUnitParameter(getThesaurus());
        copy.put(unitParameter.getKey(), unitParameter);
        return copy;
    }

    @Override
    public String getContent() {
        return
            "package com.energyict.mdc.issue.datacollection;\n" +
            "import com.energyict.mdc.issue.datacollection.event.MeterReadingEvent; \n" +
            "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService; \n" +
            "rule \"Slope detection @{ruleId}\"\n" +
            "when\n" +
            "\tevent : MeterReadingEvent( readingType.getMRID() == \"@{readingType}\", computeMaxSlope(@{trendPeriod}, @{trendPeriodUnit}) >= @{maxSlope} )\n" +
            "then\n" +
            "\tSystem.out.println(\"Slope detection @{ruleId}\");\n" +
            "\tissueCreationService.processIssueEvent(@{ruleId}, event);\n" +
            "end;";
    }

    @Override
    public Optional<? extends Issue> createIssue(Issue issue, IssueEvent event) {
        return null;
    }

}