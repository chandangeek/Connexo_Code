/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Dimension;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import java.util.EnumSet;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

class FullySpecifiedReadingTypeRequirementImpl extends ReadingTypeRequirementImpl implements FullySpecifiedReadingTypeRequirement {
    public static final String TYPE_IDENTIFIER = "FUL";

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();

    @Inject
    FullySpecifiedReadingTypeRequirementImpl(ServerMetrologyConfigurationService metrologyConfigurationService) {
        super(metrologyConfigurationService);
    }

    public FullySpecifiedReadingTypeRequirementImpl init(MetrologyConfiguration metrologyConfiguration, String name, ReadingType readingType) {
        super.init(metrologyConfiguration, name);
        this.readingType.set(readingType);
        return this;
    }

    private boolean hasRequirementsWithTheSameReadingType() {
        return getMetrologyConfigurationService().getDataModel()
                .query(FullySpecifiedReadingTypeRequirement.class)
                .select(where(Fields.READING_TYPE.fieldName()).isEqualTo(getReadingType())
                        .and(where(Fields.METROLOGY_CONFIGURATION.fieldName()).isEqualTo(getMetrologyConfiguration())))
                .stream()
                .anyMatch(candidate -> candidate.getId() != getId());
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        boolean isValid = super.validate(context);
        if (getMetrologyConfiguration() != null
                && getReadingType() != null
                && hasRequirementsWithTheSameReadingType()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.REQUIREMENT_MUST_HAVE_UNIQUE_RT + "}")
                    .addPropertyNode(Fields.READING_TYPE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return isValid;
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType.orNull();
    }

    @Override
    public Dimension getDimension() {
        return getReadingType().getUnit().getUnit().getDimension();
    }

    @Override
    public boolean matches(ReadingType readingType) {
        return getReadingType().equals(readingType);
    }

    @Override
    public boolean isRegular() {
        return getReadingType().isRegular();
    }

    @Override
    public IntervalLength getIntervalLength() {
        return IntervalLength.from(readingType.get());
    }

    @Override
    public Set<ReadingTypeUnit> getUnits() {
        return EnumSet.of(getReadingType().getUnit());
    }

    @Override
    public String getDescription() {
        return readingType.map(ReadingType::getFullAliasName).orElse("");
    }

}
