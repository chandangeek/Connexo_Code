/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePointReadingTypeConfiguration;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

public class UsagePointReadingTypeConfigurationImpl implements UsagePointReadingTypeConfiguration {
    private Reference<UsagePointConfigurationImpl> usagePointConfiguration = ValueReference.absent();
    private Reference<MultiplierTypeImpl> multiplierType = ValueReference.absent();

    private Reference<IReadingType> measured = ValueReference.absent();
    private Reference<IReadingType> calculated = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant obsoleteTime;
    private Instant modTime;
    private String userName;

    @Inject
    UsagePointReadingTypeConfigurationImpl() {
    }

    static UsagePointReadingTypeConfigurationImpl from(UsagePointConfigurationImpl usagePointConfiguration, IReadingType readingType) {
        return new UsagePointReadingTypeConfigurationImpl().init(usagePointConfiguration, readingType);
    }

    private UsagePointReadingTypeConfigurationImpl init(UsagePointConfigurationImpl usagePointConfiguration, IReadingType readingType) {
        this.usagePointConfiguration.set(usagePointConfiguration);
        this.measured.set(readingType);
        return this;
    }

    @Override
    public ReadingType getMeasured() {
        return measured.get();
    }

    @Override
    public Optional<ReadingType> getCalculated() {
        return calculated.getOptional().map(ReadingType.class::cast);
    }

    @Override
    public MultiplierType getMultiplierType() {
        return multiplierType.get();
    }

    void setMultiplication(IReadingType calculated, MultiplierTypeImpl multiplierType) {
        this.calculated.set(calculated);
        this.multiplierType.set(multiplierType);

    }
}
