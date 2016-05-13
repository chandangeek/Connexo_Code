package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;

import com.google.common.collect.ImmutableMap;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@SelfValid
public abstract class ReadingTypeRequirementImpl implements ReadingTypeRequirement, SelfObjectValidator {
    public static final Map<String, Class<? extends ReadingTypeRequirement>> IMPLEMENTERS = ImmutableMap.of(
            PartiallySpecifiedReadingTypeRequirementImpl.TYPE_IDENTIFIER, PartiallySpecifiedReadingTypeRequirementImpl.class,
            FullySpecifiedReadingTypeRequirementImpl.TYPE_IDENTIFIER, FullySpecifiedReadingTypeRequirementImpl.class);

    public enum Fields {
        ID("id"),
        NAME("name"),
        METROLOGY_CONFIGURATION("metrologyConfiguration"),
        TEMPLATE("readingTypeTemplate"),
        ATTRIBUTES("overriddenAttributes"),
        READING_TYPE("readingType"),;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    private long id;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    public ReadingTypeRequirementImpl(ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    protected void init(MetrologyConfiguration metrologyConfiguration, String name) {
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.name = name;
    }

    protected ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return this.metrologyConfigurationService;
    }

    private boolean hasRequirementsWithTheSameName() {
        return this.metrologyConfigurationService.getDataModel()
                .query(ReadingTypeRequirement.class)
                .select(where(Fields.NAME.fieldName()).isEqualTo(getName())
                        .and(where(Fields.METROLOGY_CONFIGURATION.fieldName()).isEqualTo(getMetrologyConfiguration())))
                .stream()
                .anyMatch(candidate -> candidate.getId() != getId());
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        if (getMetrologyConfiguration() != null
                && !Checks.is(getName()).emptyOrOnlyWhiteSpace()
                && hasRequirementsWithTheSameName()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
                    .addPropertyNode(Fields.NAME.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguration.orNull();
    }

    @Override
    public List<Channel> getMatchingChannelsFor(MeterActivation meterActivation) {
        return meterActivation.getChannels().stream()
                .filter(channel -> channel.getMainReadingType().getMacroPeriod() != MacroPeriod.NOTAPPLICABLE
                        || channel.getMainReadingType().getMeasuringPeriod() != TimeAttribute.NOTAPPLICABLE)
                .filter(channel -> matches(channel.getMainReadingType()))
                .collect(Collectors.toList());
    }

    /**
     * <p>
     * For {@link FullySpecifiedReadingTypeRequirement} checks that candidate is the same reading type as
     * {@link FullySpecifiedReadingTypeRequirement#getReadingType()}
     * </p>
     * <p>
     * For {@link PartiallySpecifiedReadingTypeRequirement} checks that each candidate's attribute:
     * <ul>
     * <li>is equal to overridden attribute value (if it was overridden,
     * see {@link PartiallySpecifiedReadingTypeRequirement#overrideAttribute(ReadingTypeTemplateAttributeName, int)})</li>
     * <li>or is equal to template attribute value (if attribute has code or possible values,
     * see {@link ReadingTypeTemplateAttribute#matches(ReadingType)})</li>
     * <li>or has one of system allowed values, see
     * {@link ReadingTypeTemplateAttributeName.ReadingTypeAttribute#getPossibleValues()}</li>
     * </ul>
     * </p>
     *
     * @param readingType reading type for check
     * @return <code>true</code> if all attributes are within limits
     */
    abstract boolean matches(ReadingType readingType);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadingTypeRequirementImpl that = (ReadingTypeRequirementImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }

    public abstract IntervalLength getIntervalLength();
}
