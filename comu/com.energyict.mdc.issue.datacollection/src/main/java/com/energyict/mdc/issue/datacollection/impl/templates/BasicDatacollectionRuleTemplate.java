package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.impl.templates.params.DataCollectionEventsParameter;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.issue.datacollection.BasicDatacollectionRuleTemplate",
           property = {"uuid=" + BasicDatacollectionRuleTemplate.BASIC_TEMPLATE_UUID},
           service = CreationRuleTemplate.class,
           immediate = true)
public class BasicDatacollectionRuleTemplate extends AbstractTemplate {
    public static final String BASIC_TEMPLATE_UUID = "e29b-41d4-a716";

    private volatile MeteringService meteringService;
    private volatile IssueDataCollectionService issueDataCollectionService;

    public BasicDatacollectionRuleTemplate() {
    }

    @Inject
    public BasicDatacollectionRuleTemplate(MeteringService meteringService, IssueDataCollectionService issueDataCollectionService, NlsService nlsService) {
        setMeteringService(meteringService);
        setIssueDataCollectionService(issueDataCollectionService);
        setNlsService(nlsService);
        activate();
    }

    @Activate
    public void activate(){
        addParameterDefinition(new DataCollectionEventsParameter(getThesaurus()));
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
               "\tevent : DataCollectionEvent( eventType == \"@{eventType}\" )\n"+
               "then\n"+
               "\tSystem.out.println(\"Basic datacollection rule @{ruleId}\");\n"+
               "\tissueCreationService.createIssue(@{ruleId}, event);\n"+
               "end";
    }

    @Override
    public Optional<? extends Issue> createIssue(Issue baseIssue, IssueEvent event) {
        if(event.findExistingIssue(baseIssue).isPresent()){
            // TODO change status or do other stuff
        } else {
            OpenIssueDataCollection issue = issueDataCollectionService.createIssue(baseIssue);
            event.apply(issue);
            issue.save();
            return Optional.of(issue);
        }
        return Optional.absent();
    }
}
