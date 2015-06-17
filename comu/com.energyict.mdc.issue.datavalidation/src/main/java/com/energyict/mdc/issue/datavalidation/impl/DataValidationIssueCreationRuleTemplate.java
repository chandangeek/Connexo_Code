package com.energyict.mdc.issue.datavalidation.impl;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
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
    private volatile DeviceConfigurationService deviceConfigurationService;
    
    private final PossibleDeviceConfigurations deviceConfigurations = new PossibleDeviceConfigurations();
    
    //for OSGI
    public DataValidationIssueCreationRuleTemplate() {
    }
    
    @Inject
    public DataValidationIssueCreationRuleTemplate(IssueDataValidationService issueDataValidationIssueService, IssueService issueService,
            NlsService nlsService, PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService) {
        setIssueDataValidationService(issueDataValidationIssueService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setDeviceConfigurationService(deviceConfigurationService);
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
               "import com.energyict.mdc.issue.datavalidation.impl.event.CannotEstimateDataEvent;\n" +
               "import com.energyict.mdc.issue.datavalidation.impl.event.SuspectDeletedEvent;\n" +
               "global java.util.logging.Logger LOGGER;\n" + 
               "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
               "rule \"Data validation rule @{ruleId}\"\n" +
               "when\n" +
               "\tevent : CannotEstimateDataEvent(device.deviceConfiguration.id in (@{" + DEVICE_CONFIGURATIONS +"}))\n" +
               "then\n" +
               "\tLOGGER.info(\"Trying to create issue by datavalidation rule [id = @{ruleId}]\");\n" +
               "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
               "end\n" +
               "\n" +
               "rule \"Autoresolution section @{ruleId}\"\n" +
               "when\n" +
               "\tevent: SuspectDeletedEvent(device.deviceConfiguration.id in (@{" + DEVICE_CONFIGURATIONS +"}))\n" +
               "then\n" +
               "\tLOGGER.info(\"Trying to resolve issue by datavalidation rule [id = @{ruleId}]\");\n" +
               "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
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
    
    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(propertySpecService.listValuePropertySpec(DEVICE_CONFIGURATIONS, true, deviceConfigurations, deviceConfigurations.getPossibleValues()));
        return builder.build();
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get();
    }

    @Override
    public Optional<? extends Issue> createIssue(Issue baseIssue, IssueEvent event) {
        Optional<? extends Issue> issueOptional = event.findExistingIssue();
        Issue issue = issueOptional.isPresent() ? issueOptional.get() : issueDataValidationService.createIssue(baseIssue);
        event.apply(issue);
        issue.save();
        return Optional.of(issue);
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssueDataValidation issueDataValidation = (OpenIssueDataValidation) issue.get();
            event.apply(issueDataValidation);
            if (issueDataValidation.getNotEstimatedBlocks().isEmpty()) {
                return Optional.of(issueDataValidation.close(issueService.findStatus(IssueStatus.RESOLVED).get()));
            } else {
                issueDataValidation.save();
                return Optional.of(issueDataValidation);
            }
        }
        return issue;
    }
    
    private class PossibleDeviceConfigurations implements CanFindByStringKey<DeviceConfigurationInfo> {

        @Override
        public Optional<DeviceConfigurationInfo> find(String key) {
            return deviceConfigurationService.findDeviceConfiguration(Long.parseLong(key)).map(DeviceConfigurationInfo::new);
        }

        @Override
        public Class<DeviceConfigurationInfo> valueDomain() {
            return DeviceConfigurationInfo.class;
        }
        
        public DeviceConfigurationInfo[] getPossibleValues() {
            return deviceConfigurationService.findAllDeviceTypes().stream()
                        .flatMap(type -> type.getConfigurations().stream())
                        .map(DeviceConfigurationInfo::new)
                        .toArray(DeviceConfigurationInfo[]::new);
        }
    }
    
    @XmlRootElement
    static class DeviceConfigurationInfo extends HasIdAndName {
        
        private transient DeviceConfiguration deviceConfiguration;
        
        public DeviceConfigurationInfo(DeviceConfiguration deviceConfiguration) {
            this.deviceConfiguration = deviceConfiguration;
        }

        @Override
        public Long getId() {
            return deviceConfiguration.getId();
        }

        @Override
        public String getName() {
            return deviceConfiguration.getName();
        }
        
        public Long getDeviceTypeId() {
            return deviceConfiguration.getDeviceType().getId();
        }
        
        public String getDeviceTypeName() {
            return deviceConfiguration.getDeviceType().getName();
        }
        
        public boolean isActive() {
            return deviceConfiguration.isActive();
        }
    }
}
