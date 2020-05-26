package com.energyict.mdc.issue.datavalidation.impl.template;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.*;
import com.elster.jupiter.properties.rest.DeviceConfigurationPropertyFactory;
import com.elster.jupiter.properties.rest.RelativePeriodWithCountPropertyFactory;
import com.elster.jupiter.properties.rest.ValidationRulePropertyFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.MessageSeeds;
import com.energyict.mdc.issue.datavalidation.impl.TranslationKeys;

import com.google.common.collect.ImmutableList;
import net.minidev.json.JSONObject;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * SuspectCreationRuleTemplate is responsible for creating issues when a suspect value is found during the data validation process.
 * <p>
 * Link to confluence page where you can find functional design related to code below:
 * {@link} https://confluence.eict.vpdc/display/COMU/Create+Validation+issues+exceeding+a+threshold+within+a+given+time+frame
 *
 * @author edragutan
 */

public class SuspectCreatedIssueCreationRuleTemplate implements CreationRuleTemplate {

    public static final String NAME = "SuspectCreationRuleTemplate";

    public static final String DEVICE_CONFIGURATIONS = NAME + ".deviceConfigurations";
    public static final String VALIDATION_RULES = NAME + ".validationRules";
    public static final String THRESHOLD = NAME + ".threshold";

    private static final int DEFAULT_NUMERICAL_VALUE = 0;
    private static final String SEPARATOR = ":";

    private final PropertySpecService propertySpecService;
    private final IssueDataValidationService issueDataValidationService;
    private final IssueService issueService;
    private final Thesaurus thesaurus;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final MeteringTranslationService meteringTranslationService;
    private final TimeService timeService;
    private final ValidationService validationService;

    @Inject
    public SuspectCreatedIssueCreationRuleTemplate(final PropertySpecService propertySpecService,
                                                   final IssueDataValidationService issueDataValidationService,
                                                   final IssueService issueService,
                                                   final NlsService nlsService,
                                                   final DeviceConfigurationService deviceConfigurationService,
                                                   final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                                   final MeteringTranslationService meteringTranslationService,
                                                   final TimeService timeService,
                                                   final ValidationService validationService) {
        this.issueDataValidationService = issueDataValidationService;
        this.issueService = issueService;
        this.propertySpecService = propertySpecService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.meteringTranslationService = meteringTranslationService;
        this.thesaurus = nlsService.getThesaurus(IssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN);
        this.timeService = timeService;
        this.validationService = validationService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return TranslationKeys.SUSCPECT_CREATION_RULE_TEMPLATE_NAME.getTranslated(thesaurus);
    }

    @Override
    public String getDescription() {
        return TranslationKeys.SUSCPECT_CREATION_RULE_TEMPLATE_DESCRIPTION.getTranslated(thesaurus);
    }

    /**
     * Return rule template for Drools (Business Rules Management System).
     * <p>
     * Since currently the only way to write down rules for drools is just hardcoding them with a string
     *
     * @return a string with rules for Drools
     */
    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datavalidation\n" +
                "import com.energyict.mdc.issue.datavalidation.impl.event.SuspectValueCreatedEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Data validation rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : SuspectValueCreatedEvent(deviceConfigurationId in (@{" + DEVICE_CONFIGURATIONS + "}))\n" +
                "\teval( event.checkValidationRule(\"@{" + THRESHOLD + "}\", \"@{" + VALIDATION_RULES + "}\") == true )\n" +
                "\teval( event.checkOccurrenceConditions(\"@{" + THRESHOLD + "}\") == true )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create suspect created issue by datavalidation rule [id = @{ruleId}]\");\n" +
                "\tevent.setCreationRule(@{ruleId});\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n";
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get();
    }

    @Override
    public OpenIssue createIssue(final OpenIssue openIssue, final IssueEvent issueEvent) {
        return issueDataValidationService.createIssue(openIssue, issueEvent);
    }

    @Override
    public Optional<? extends Issue> resolveIssue(final IssueEvent event) {
        return Optional.empty();
    }

    /**
     * Obtain list of properties that are introduced by this {@link CreationRuleTemplate}
     * <p>
     * Propertie is an object that represents a field on issue creation rule.
     *
     * @return list of properties
     */
    @Override
    public List<PropertySpec> getPropertySpecs() {
        final ImmutableList.Builder<PropertySpec> properties = ImmutableList.builder();

        properties.add(propertySpecService
                .specForValuesOf(new DeviceLifeCycleInDeviceTypeInfoValueFactory(deviceConfigurationService, deviceLifeCycleConfigurationService, meteringTranslationService))
                .named(DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, TranslationKeys.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES)
                .fromThesaurus(thesaurus)
                .markRequired()
                .markMultiValued(";")
                .addValues(deviceConfigurationService.getDeviceLifeCycleInDeviceTypeInfoPossibleValues())
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());

        properties.add(propertySpecService
                .specForValuesOf(new DeviceConfigurationInfoValueFactory())
                .named(DEVICE_CONFIGURATIONS, TranslationKeys.DEVICE_CONFIGURATIONS_PROPERTY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .markMultiValued(",")
                .addValues(getPossibleValuesForDeviceConfiguration())
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());

        properties.add(propertySpecService
                .specForValuesOf(new ValidationRuleInfoValueFactory())
                .named(VALIDATION_RULES, TranslationKeys.VALIDATION_RULES_PROPERTY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .markMultiValued(";")
                .addValues(getPossibleValuesForValidationRule())
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());

        properties.add(propertySpecService
                .specForValuesOf(new RelativePeriodWithCountInfoValueFactory())
                .named(THRESHOLD, TranslationKeys.EVENT_TEMPORAL_THRESHOLD)
                .fromThesaurus(thesaurus)
                .markRequired()
                .setDefaultValue(new RelativePeriodWithCountInfo(DEFAULT_NUMERICAL_VALUE, timeService.getAllRelativePeriod()))
                .finish());

        return properties.build();
    }

    private DeviceConfigurationInfo[] getPossibleValuesForDeviceConfiguration() {
        return deviceConfigurationService
                .findAllDeviceTypes()
                .stream()
                .flatMap(type -> type.getConfigurations().stream())
                .map(DeviceConfigurationInfo::new)
                .toArray(DeviceConfigurationInfo[]::new);
    }

    private ValidationRuleInfo[] getPossibleValuesForValidationRule() {
        return validationService
                .getValidationRuleSets()
                .stream()
                .map(ValidationRuleSet::getRules)
                .flatMap(Collection::stream)
                .filter(ValidationRule::isActive)
                .map(ValidationRuleInfo::new)
                .toArray(ValidationRuleInfo[]::new);
    }

    public class ValidationRuleInfoValueFactory implements ValueFactory<HasIdAndName>, ValidationRulePropertyFactory {

        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return validationService.findValidationRule(Long.parseLong(stringValue)).map(ValidationRuleInfo::new).orElse(null);
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
    public static class ValidationRuleInfo extends HasIdAndName {

        private transient ValidationRule validationRule;

        public ValidationRuleInfo(final ValidationRule validationRule) {
            this.validationRule = validationRule;
        }

        @Override
        public Object getId() {
            return validationRule.getId();
        }

        @Override
        public String getName() {
            return validationRule.getName();
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
            ValidationRuleInfo that = (ValidationRuleInfo) o;
            return Objects.equals(validationRule.getId(), that.validationRule.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), validationRule.getId());
        }
    }

    public class DeviceConfigurationInfoValueFactory implements ValueFactory<HasIdAndName>, DeviceConfigurationPropertyFactory {
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
    public static class DeviceConfigurationInfo extends HasIdAndName {

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

    private class RelativePeriodWithCountInfoValueFactory implements ValueFactory<HasIdAndName>, RelativePeriodWithCountPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            List<String> values = Arrays.asList(stringValue.split(SEPARATOR));
            if (values.size() != 2) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS,
                        "properties." + THRESHOLD,
                        String.valueOf(2),
                        String.valueOf(values.size()));
            }
            int count = Integer.parseInt(values.get(0));
            RelativePeriod relativePeriod = timeService.findRelativePeriod(Long.parseLong(values.get(1))).orElse(null);
            return new RelativePeriodWithCountInfo(count, relativePeriod);
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

    public static class RelativePeriodWithCountInfo extends HasIdAndName {

        private RelativePeriod relativePeriod;
        private int occurrenceCount;

        RelativePeriodWithCountInfo(int occurrenceCount, RelativePeriod relativePeriod) {
            this.relativePeriod = relativePeriod;
            this.occurrenceCount = occurrenceCount;
        }

        @Override
        public String getId() {
            return occurrenceCount + SEPARATOR + relativePeriod.getId();
        }

        @Override
        public String getName() {
            JSONObject jsonId = new JSONObject();
            jsonId.put("occurrenceCount", occurrenceCount);
            jsonId.put("relativePeriod", relativePeriod.getName());
            return jsonId.toString();
        }

        public long getRelativePeriodId() {
            return relativePeriod.getId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RelativePeriodWithCountInfo)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            RelativePeriodWithCountInfo that = (RelativePeriodWithCountInfo) o;

            return occurrenceCount == that.occurrenceCount && relativePeriod.equals(that.relativePeriod);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + relativePeriod.hashCode();
            result = 31 * result + occurrenceCount;
            return result;
        }
    }
}
