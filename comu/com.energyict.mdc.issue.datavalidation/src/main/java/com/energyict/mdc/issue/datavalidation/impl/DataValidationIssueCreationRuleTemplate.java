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
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.MessageSeeds;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@Component(name = "com.energyict.mdc.issue.datavalidation.impl.DataValidationIssueCreationRuleTemplate",
           property = { "name=" + DataValidationIssueCreationRuleTemplate.NAME },
           service = CreationRuleTemplate.class, immediate = true)
public class DataValidationIssueCreationRuleTemplate implements CreationRuleTemplate {

    static final String NAME = "DataValidationIssueCreationRuleTemplate";
    
    public static final String DEVICE_CONFIGURATIONS = NAME + ".deviceConfigurations";
    
    private volatile IssueDataValidationService issueDataValidationService;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    
    //for OSGI
    public DataValidationIssueCreationRuleTemplate() {
    }
    
    @Inject
    public DataValidationIssueCreationRuleTemplate(IssueDataValidationService issueDataValidationIssueService, IssueService issueService, NlsService nlsService, PropertySpecService propertySpecService) {
        setIssueDataValidationService(issueDataValidationIssueService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
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
               "import com.energyict.mdc.issue.datavalidation.SuspectDeletedEvent;\n" +
               "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
               "rule \"Data validation rule @{ruleId}\"\n" +
               "when\n" +
               "\tevent : CannotEstimateDataEvent(device.deviceConfiguration.id in (@{" + DEVICE_CONFIGURATIONS +"}))\n" +
               "then\n" +
               "\tSystem.out.println(\"Trying to create issue by datavalidation rule=@{ruleId}\");\n" +
               "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
               "end\n" +
               "\n" +
               "rule \"Autoresolution section @{ruleId}\"\n" +
               "when\n" +
               "\tevent: SuspectDeletedEvent(device.deviceConfiguration.id in (@{" + DEVICE_CONFIGURATIONS +"}))\n" +
               "then\n" +
               "\tissueCreationService.processIssueResolveEvent(@{ruleId}, event);\n" +
               "end\n";
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
    
    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(propertySpecService.stringPropertySpec(DEVICE_CONFIGURATIONS, true, null));
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
        Optional<? extends Issue> foundIssue = event.findExistingIssue();
        if (!foundIssue.isPresent()) {
            OpenIssueDataValidation issue = issueDataValidationService.createIssue(baseIssue);
            event.apply(issue);
            issue.save();
            return Optional.of(issue);
        }
        // if found then append/insert suspect intervals
        return foundIssue;
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        // TODO Auto-generated method stub
        //event.findExistingIssue()
        // if found then remove/insert suspect intervals, or close issue completely
        return null;
    }
}
