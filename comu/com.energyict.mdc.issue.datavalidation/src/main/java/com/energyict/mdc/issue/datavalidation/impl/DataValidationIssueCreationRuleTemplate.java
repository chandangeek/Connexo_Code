/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.DeviceConfigurationPropertyFactory;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfo;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;

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

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;

@Component(name = "com.energyict.mdc.issue.datavalidation.impl.DataValidationIssueCreationRuleTemplate",
        property = {"name=" + DataValidationIssueCreationRuleTemplate.NAME},
        service = CreationRuleTemplate.class, immediate = true)
public class DataValidationIssueCreationRuleTemplate implements CreationRuleTemplate {

    static final String NAME = "DataValidationIssueCreationRuleTemplate";

    public static final String DEVICE_CONFIGURATIONS = NAME + ".deviceConfigurations";

    private volatile IssueDataValidationService issueDataValidationService;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile MeteringTranslationService meteringTranslationService;

    //for OSGI
    public DataValidationIssueCreationRuleTemplate() {
    }

    @Inject
    public DataValidationIssueCreationRuleTemplate(IssueDataValidationService issueDataValidationIssueService, IssueService issueService,
                                                   NlsService nlsService, PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                                   MeteringTranslationService meteringTranslationService) {
        this();
        setIssueDataValidationService(issueDataValidationIssueService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setMeteringTranslationService(meteringTranslationService);
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
        return "package com.energyict.mdc.issue.datavalidation\n" +
                "import com.energyict.mdc.issue.datavalidation.impl.event.CannotEstimateDataEvent;\n" +
                "import com.energyict.mdc.issue.datavalidation.impl.event.SuspectDeletedEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Data validation rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : CannotEstimateDataEvent(deviceConfigurationId in (@{" + DEVICE_CONFIGURATIONS + "}))\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by datavalidation rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n" +
                "\n" +
                "rule \"Autoresolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent: SuspectDeletedEvent(deviceConfigurationId in (@{" + DEVICE_CONFIGURATIONS + "}))\n" +
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

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        DeviceConfigurationInfo[] possibleValues =
                deviceConfigurationService
                        .findAllDeviceTypes()
                        .stream()
                        .flatMap(type -> type.getConfigurations().stream())
                        .map(DeviceConfigurationInfo::new)
                        .toArray(DeviceConfigurationInfo[]::new);
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(propertySpecService
                .specForValuesOf(new DeviceLifeCycleInDeviceTypeInfoValueFactory(deviceConfigurationService, deviceLifeCycleConfigurationService, meteringTranslationService))
                .named(DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, TranslationKeys.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .markMultiValued(";")
                .addValues(deviceConfigurationService.getDeviceLifeCycleInDeviceTypeInfoPossibleValues())
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        builder.add(
                propertySpecService
                        .specForValuesOf(new DeviceConfigurationInfoValueFactory())
                        .named(DEVICE_CONFIGURATIONS, TranslationKeys.DEVICE_CONFIGURATIONS_PROPERTY)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .markMultiValued(",")
                        .addValues(possibleValues)
                        .markExhaustive(PropertySelectionMode.LIST)
                        .finish());
        return builder.build();
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get();
    }

    @Override
    public OpenIssueDataValidation createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        return issueDataValidationService.createIssue(baseIssue, issueEvent);
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
                issueDataValidation.update();
                return Optional.of(issueDataValidation);
            }
        }
        return issue;
    }

    protected List<CreationRule> getExistingCreationRules() {
        Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue = where("template").isEqualTo(getName());
        return query.select(conditionIssue, Order.ascending("name"));
    }

    @Override
    public Optional<CreationRule> getCreationRuleWhichUsesDeviceType(Long deviceTypeId)
    {
        for (CreationRule creationRule : getExistingCreationRules()) {
            Object lifecycleStates = creationRule.getProperties().get(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES);
            if((lifecycleStates instanceof List) && ((List)lifecycleStates).stream()
                    .anyMatch(propertySpec -> (propertySpec instanceof DeviceLifeCycleInDeviceTypeInfo) &&
                            ((DeviceLifeCycleInDeviceTypeInfo)propertySpec).getDeviceTypeId() == deviceTypeId))
                return Optional.of(creationRule);
        }
        return Optional.empty();
    }

    private class DeviceConfigurationInfoValueFactory implements ValueFactory<HasIdAndName>, DeviceConfigurationPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return deviceConfigurationService
                    .findDeviceConfiguration(Long.parseLong(stringValue))
                    .map(DeviceConfigurationInfo::new)
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

    @XmlRootElement
    static class DeviceConfigurationInfo extends HasIdAndName {

        private transient DeviceConfiguration deviceConfiguration;

        DeviceConfigurationInfo(DeviceConfiguration deviceConfiguration) {
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

            DeviceConfigurationInfo that = (DeviceConfigurationInfo) o;

            return deviceConfiguration.getId() == that.deviceConfiguration.getId();

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Long.hashCode(deviceConfiguration.getId());
            return result;
        }
    }
}
