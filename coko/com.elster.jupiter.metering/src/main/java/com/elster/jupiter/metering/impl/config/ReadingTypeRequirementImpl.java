package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingType;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingType;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ReadingTypeRequirementImpl implements ReadingTypeRequirement {
    public static final Map<String, Class<? extends ReadingTypeRequirement>> IMPLEMENTERS = ImmutableMap.of(
            PartiallySpecifiedReadingTypeImpl.TYPE_IDENTIFIER, PartiallySpecifiedReadingTypeImpl.class,
            FullySpecifiedReadingTypeImpl.TYPE_IDENTIFIER, FullySpecifiedReadingTypeImpl.class);

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

    protected void init(MetrologyConfiguration metrologyConfiguration, String name) {
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.name = name;
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
        return this.metrologyConfiguration.get();
    }

    @Override
    public List<Channel> getMatchingChannelsFor(MeterActivation meterActivation) {
        return meterActivation.getChannels().stream()
                .filter(channel -> matches(channel.getMainReadingType()))
                .collect(Collectors.toList());
    }

    /**
     * <p>
     * For {@link FullySpecifiedReadingType} checks that candidate is the same reading type as
     * {@link FullySpecifiedReadingType#getReadingType()}
     * </p>
     * <p>
     * For {@link PartiallySpecifiedReadingType} checks that each candidate's attribute:
     * <ul>
     * <li>is equal to overridden attribute value (if it was overridden,
     * see {@link PartiallySpecifiedReadingType#overrideAttribute(ReadingTypeTemplateAttributeName, int)})</li>
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
}
