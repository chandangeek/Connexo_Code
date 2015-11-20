package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePointReadingTypeConfiguration;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;

public class UsagePointReadingTypeConfigurationImpl implements UsagePointReadingTypeConfiguration {
    private Reference<UsagePointConfigurationImpl> usagePointConfiguration = ValueReference.absent();
    private Reference<MultiplierTypeImpl> multiplierType = ValueReference.absent();

    private Reference<ReadingType> measured = ValueReference.absent();
    private Reference<ReadingType> calculated = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant obsoleteTime;
    private Instant modTime;
    private String userName;

    @Inject
    UsagePointReadingTypeConfigurationImpl() {
    }

    static UsagePointReadingTypeConfigurationImpl from(UsagePointConfigurationImpl usagePointConfiguration, ReadingType readingType) {
        return new UsagePointReadingTypeConfigurationImpl().init(usagePointConfiguration, readingType);
    }

    private UsagePointReadingTypeConfigurationImpl init(UsagePointConfigurationImpl usagePointConfiguration, ReadingType readingType) {
        this.usagePointConfiguration.set(usagePointConfiguration);
        this.measured.set(readingType);
        return this;
    }

    @Override
    public ReadingType getMeasured() {
        return measured.get();
    }

    @Override
    public ReadingType getCalculated() {
        return calculated.get();
    }

    @Override
    public MultiplierType getMultiplierType() {
        return multiplierType.get();
    }

    void setMultiplication(ReadingType calculated, MultiplierTypeImpl multiplierType) {
        this.calculated.set(calculated);
        this.multiplierType.set(multiplierType);

    }
}
