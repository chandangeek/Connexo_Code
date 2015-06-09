package com.energyict.mdc.issue.datavalidation.impl;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.MessageSeeds;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@Component(name = "com.energyict.mdc.issue.datavalidation.impl.DataValidationIssueCreationRuleTemplate",
           property = { "name=" + DataValidationIssueCreationRuleTemplate.NAME },
           service = CreationRuleTemplate.class, immediate = true)
public class DataValidationIssueCreationRuleTemplate implements CreationRuleTemplate {

    static final String NAME = "DataValidationIssueCreationRuleTemplate";
    
    private volatile IssueDataValidationService issueDataValidationService;
    private volatile IssueService issueService;
    
    private Thesaurus thesaurus;
    
    //for OSGI
    public DataValidationIssueCreationRuleTemplate() {
    }
    
    @Inject
    public DataValidationIssueCreationRuleTemplate(IssueDataValidationService issueDataValidationIssueService, IssueService issueService, NlsService nlsService) {
        setIssueDataValidationService(issueDataValidationIssueService);
        setIssueService(issueService);
        setNlsService(nlsService);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return MessageSeeds.DATA_VALIDATION_ISSUE_RULE_TEMPLATE_NAME.getTranslated(thesaurus);
    }

    @Override
    public String getDescription() {
        return MessageSeeds.DATA_VALIDATION_ISSUE_RULE_TEMPLATE_DESCRIPTION.getTranslated(thesaurus);
    }

    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datavalidation\n" +
               "import com.energyict.mdc.issue.datavalidation.CannotEstimateDataEvent;\n" +
               "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
               "rule \"Data validation rule @{ruleId}\"\n" +
               "when\n" +
               "\tevent : CannotEstimateDataEvent()\n" +
               "then\n" +
               "\tSystem.out.println(\"Trying to create issue by datavalidation rule=@{ruleId}\");\n" +
               "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
               "end";
        //TODO add auto resolution section
    }
    
    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN);
    }
    
    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    
    @Reference
    public void setIssueDataValidationService(IssueDataValidationService issueDataValidationService) {
        this.issueDataValidationService = issueDataValidationService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        //TODO add device configuration property
        return builder.build();
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return getPropertySpecs().stream().filter(property -> property.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get();
    }

    @Override
    public Optional<? extends Issue> createIssue(Issue baseIssue, IssueEvent event) {
        // TODO Auto-generated method stub
        //event.findExistingIssue()
        // if found then append/insert suspect intervals
        return null;
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        // TODO Auto-generated method stub
        //event.findExistingIssue()
        // if found then remove/insert suspect intervals, or close issue completely
        return null;
    }
}
