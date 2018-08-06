/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.MetrologyConfigurationPropertyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.elster.insight.issue.datavalidation.UsagePointOpenIssueDataValidation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.insight.issue.datavalidation.impl.UsagePointDataValidationIssueCreationRuleTemplate",
        property = {"name=" + UsagePointDataValidationIssueCreationRuleTemplate.NAME},
        service = CreationRuleTemplate.class, immediate = true)
public class UsagePointDataValidationIssueCreationRuleTemplate implements CreationRuleTemplate {

    static final String NAME = "UsagePointDataValidationIssueCreationRuleTemplate";

    public static final String METROLOGY_CONFIGS = NAME + ".metrologyConfigurations";

    private volatile UsagePointIssueDataValidationService usagePointIssueDataValidationService;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile MetrologyConfigurationService metrologyConfigurationService;

    //for OSGI
    public UsagePointDataValidationIssueCreationRuleTemplate() {
    }

    @Inject
    public UsagePointDataValidationIssueCreationRuleTemplate(UsagePointIssueDataValidationService issueDataValidationIssueService, IssueService issueService,
                                                             NlsService nlsService, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService) {
        this();
        setUsagePointIssueDataValidationService(issueDataValidationIssueService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setMetrologyConfigurationService(metrologyConfigurationService);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return TranslationKeys.DATA_VALIDATION_ISSUE_RULE_TEMPLATE_NAME.getTranslated(thesaurus);
    }

    @Override
    public String getDescription() {
        return TranslationKeys.DATA_VALIDATION_ISSUE_RULE_TEMPLATE_DESCRIPTION.getTranslated(thesaurus);
    }

    @Override
    public String getContent() {
        return "package com.elster.insight.issue.datavalidation\n" +
                "import com.elster.insight.issue.datavalidation.impl.event.CannotEstimateUsagePointDataEvent;\n" +
                "import com.elster.insight.issue.datavalidation.impl.event.UsagePointSuspectDeletedEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Data validation rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : CannotEstimateUsagePointDataEvent(metrologyConfigurationId in (@{" + METROLOGY_CONFIGS + "}))\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by usage point datavalidation rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n" +
                "\n" +
                "rule \"Autoresolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent: UsagePointSuspectDeletedEvent(metrologyConfigurationId in (@{" + METROLOGY_CONFIGS + "}))\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve issue by usage point datavalidation rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
                "end\n";
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(UsagePointIssueDataValidationService.ISSUE_TYPE_NAME).get();
    }

    @Override
    public UsagePointOpenIssueDataValidation createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        return usagePointIssueDataValidationService.createIssue(baseIssue, issueEvent);
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            UsagePointOpenIssueDataValidation issueDataValidation = (UsagePointOpenIssueDataValidation) issue.get();
            event.apply(issueDataValidation);
            if (issueDataValidation.getNotEstimatedBlocks().isEmpty()) {
                return Optional.of(issueDataValidation.close(issueService.findStatus(IssueStatus.RESOLVED).get()));
            } else {
                issueDataValidation.update();
                return Optional.of(issueDataValidation);
            }
        }
        return issue;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointIssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setUsagePointIssueDataValidationService(UsagePointIssueDataValidationService usagePointIssueDataValidationService) {
        this.usagePointIssueDataValidationService = usagePointIssueDataValidationService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        MetrologyConfigurationInfo[] possibleValues =
                metrologyConfigurationService
                        .findAllMetrologyConfigurations()
                        .stream()
                        .filter(MetrologyConfiguration::isActive)
                        //.flatMap(type -> type.getContracts().stream())
                        .map(MetrologyConfigurationInfo::new)
                        .toArray(MetrologyConfigurationInfo[]::new);
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(
                propertySpecService
                        .specForValuesOf(new MetrologyConfigurationInfoValueFactory())
                        .named(METROLOGY_CONFIGS, TranslationKeys.METROLOGY_CONFIGURATIONS_PROPERTY)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .markMultiValued(",")
                        .addValues(possibleValues)
                        .markExhaustive(PropertySelectionMode.LIST)
                        .finish());
        return builder.build();
    }

    @XmlRootElement
    static class MetrologyConfigurationInfo extends HasIdAndName {

        private transient MetrologyConfiguration metrologyConfiguration;

        MetrologyConfigurationInfo(MetrologyConfiguration metrologyConfiguration) {
            this.metrologyConfiguration = metrologyConfiguration;
        }

        @Override
        public Long getId() {
            return metrologyConfiguration.getId();
        }

        @Override
        public String getName() {
            return metrologyConfiguration.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            MetrologyConfigurationInfo that = (MetrologyConfigurationInfo) o;

            return metrologyConfiguration.getId() == that.metrologyConfiguration.getId();

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Long.hashCode(metrologyConfiguration.getId());
            return result;
        }

        public boolean isActive() {
            return metrologyConfiguration.isActive();
        }
    }

    private class MetrologyConfigurationInfoValueFactory implements ValueFactory<HasIdAndName>, MetrologyConfigurationPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return metrologyConfigurationService
                    .findMetrologyConfiguration(Long.parseLong(stringValue))
                    //by name findMetrologyConfiguration(stringValue)
                    .filter(MetrologyConfiguration::isActive)
                    .map(MetrologyConfigurationInfo::new)
                    .orElse(null);
        }

        @Override
        public String toStringValue(HasIdAndName object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<HasIdAndName> getValueType() {
            return HasIdAndName.class;
        }

        @Override
        public HasIdAndName valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(HasIdAndName object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, HasIdAndName value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, HasIdAndName value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }
}
