package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingType;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Dimension;

public class FullySpecifiedReadingTypeImpl extends ReadingTypeRequirementImpl implements FullySpecifiedReadingType {
    public static final String TYPE_IDENTIFIER = "FUL";

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();

    public FullySpecifiedReadingTypeImpl init(MetrologyConfiguration metrologyConfiguration, MeterRole meterRole, String name, ReadingType readingType) {
        super.init(metrologyConfiguration, meterRole, name);
        this.readingType.set(readingType);
        return this;
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType.get();
    }

    @Override
    public Dimension getDimension() {
        return getReadingType().getUnit().getUnit().getDimension();
    }

    @Override
    public boolean matches(ReadingType candidate) {
        return getReadingType().equals(candidate);
    }
}
