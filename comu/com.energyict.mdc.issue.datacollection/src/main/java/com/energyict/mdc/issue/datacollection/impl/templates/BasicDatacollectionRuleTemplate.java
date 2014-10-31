package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.impl.templates.params.AutoResolutionParameter;
import com.energyict.mdc.issue.datacollection.impl.templates.params.EventTypeParameter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Component(name = "com.energyict.mdc.issue.datacollection.BasicDatacollectionRuleTemplate",
           property = {"uuid=" + BasicDatacollectionRuleTemplate.BASIC_TEMPLATE_UUID},
           service = CreationRuleTemplate.class,
           immediate = true)
public class BasicDatacollectionRuleTemplate extends AbstractTemplate {
    public static final String BASIC_TEMPLATE_UUID = "e29b-41d4-a716";

    private volatile MeteringService meteringService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile IssueService issueService;

    public BasicDatacollectionRuleTemplate() {
    }

    @Inject
    public BasicDatacollectionRuleTemplate(MeteringService meteringService, IssueDataCollectionService issueDataCollectionService, NlsService nlsService, IssueService issueService) {
        setMeteringService(meteringService);
        setIssueDataCollectionService(issueDataCollectionService);
        setNlsService(nlsService);
        setIssueService(issueService);
    }

    @Activate
    public void activate(){
        addParameterDefinition(new EventTypeParameter(false, getThesaurus(), meteringService));
        addParameterDefinition(new AutoResolutionParameter(getThesaurus()));
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        setThesaurus(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public final void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
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
               "import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;\n" +
               "import com.energyict.mdc.issue.datacollection.event.ResolveEvent;\n" +
               "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
               "rule \"Basic datacollection rule @{ruleId}\"\n"+
               "when\n"+
               "\tevent : DataCollectionEvent( eventType == \"@{eventType}\" )\n"+
               "then\n"+
               "\tSystem.out.println(\"Trying to create issue by basic datacollection rule=@{ruleId}\");\n"+
               "\tissueCreationService.processIssueEvent(@{ruleId}, event);\n"+
               "end\n" +
               "rule \"Auto-resolution section @{ruleId}\"\n"+
               "when\n"+
               "\tevent : ResolveEvent( eventType == \"@{eventType}\", @{"+ AutoResolutionParameter.AUTO_RESOLUTION_PARAMETER_KEY + "} = true )\n"+
               "then\n"+
               "\tSystem.out.println(\"Trying to resolve issue by basic datacollection rule=@{ruleId}\");\n"+
               "\tissueCreationService.processIssueResolveEvent(@{ruleId}, event);\n"+
               "end";
    }

    @Override
    public Optional<? extends Issue> createIssue(Issue baseIssue, IssueEvent event) {
        if (!event.findExistingIssue().isPresent()) {
            OpenIssueDataCollection issue = issueDataCollectionService.createIssue(baseIssue);
            event.apply(issue);
            issue.save();
            return Optional.of(issue);
        } else {
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) event.findExistingIssue().get();
            if (IssueStatus.IN_PROGRESS.equals(dcIssue.getStatus().getKey())){
                dcIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).get());
                dcIssue.save();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<? extends Issue> resolveIssue(CreationRule rule, IssueEvent event) {
        List<CreationRuleParameter> ruleParameters = rule.getParameters();
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()){
            for (CreationRuleParameter parameter : ruleParameters) {
                if (AutoResolutionParameter.AUTO_RESOLUTION_PARAMETER_KEY.equalsIgnoreCase(parameter.getKey())){
                    OpenIssue openIssue = (OpenIssue) issue.get();
                    issue = Optional.of(openIssue.close(issueService.findStatus(IssueStatus.RESOLVED).get()));
                    break;
                }
            }
        }
        return issue;
    }
}
