/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

class MeterReadingTypeConfigurationImpl implements MeterReadingTypeConfiguration {

    private Reference<MeterConfigurationImpl> meterConfiguration = ValueReference.absent();
    private BigDecimal overflowValue;
    private Integer numberOfFractionDigits;
    private Reference<MultiplierTypeImpl> multiplierType = ValueReference.absent();

    private Reference<IReadingType> measured = ValueReference.absent();
    private Reference<IReadingType> calculated = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant obsoleteTime;
    private Instant modTime;
    private String userName;

    @Inject
    MeterReadingTypeConfigurationImpl() {
    }

    static MeterReadingTypeConfigurationImpl from(MeterConfigurationImpl meterConfiguration, IReadingType readingType) {
        return new MeterReadingTypeConfigurationImpl().init(meterConfiguration, readingType);
    }

    private MeterReadingTypeConfigurationImpl init(MeterConfigurationImpl meterConfiguration, IReadingType readingType) {
        this.meterConfiguration.set(meterConfiguration);
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

    @Override
    public Optional<BigDecimal> getOverflowValue() {
        return Optional.ofNullable(overflowValue);
    }

    void setOverflowValue(BigDecimal overflowValue) {
        this.overflowValue = overflowValue;
    }

    @Override
    public OptionalInt getNumberOfFractionDigits() {
        return numberOfFractionDigits == null ? OptionalInt.empty() : OptionalInt.of(numberOfFractionDigits);
    }

    void setNumberOfFractionDigits(Integer numberOfFractionDigits) {
        this.numberOfFractionDigits = numberOfFractionDigits;
    }

    void setMultiplication(IReadingType calculated, MultiplierTypeImpl multiplierType) {
        this.calculated.set(calculated);
        this.multiplierType.set(multiplierType);

    }
}
