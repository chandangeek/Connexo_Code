package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.OptionalInt;
import java.util.OptionalLong;

class MeterReadingTypeConfigurationImpl implements MeterReadingTypeConfiguration {

    private Reference<MeterConfigurationImpl> meterConfiguration = ValueReference.absent();
    private Long overflowValue;
    private Integer numberOfFractionDigits;
    private Reference<MultiplierTypeImpl> multiplierType = ValueReference.absent();

    private Reference<ReadingType> measured = ValueReference.absent();
    private Reference<ReadingType> calculated = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant obsoleteTime;
    private Instant modTime;
    private String userName;

    @Inject
    MeterReadingTypeConfigurationImpl() {
    }

    static MeterReadingTypeConfigurationImpl from(MeterConfigurationImpl meterConfiguration, ReadingType readingType) {
        return new MeterReadingTypeConfigurationImpl().init(meterConfiguration, readingType);
    }

    private MeterReadingTypeConfigurationImpl init(MeterConfigurationImpl meterConfiguration, ReadingType readingType) {
        this.meterConfiguration.set(meterConfiguration);
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

    @Override
    public OptionalLong getOverflowValue() {
        return overflowValue == null ? OptionalLong.empty() : OptionalLong.of(overflowValue);
    }

    void setOverflowValue(Long overflowValue) {
        this.overflowValue = overflowValue;
    }

    @Override
    public OptionalInt getNumberOfFractionDigits() {
        return numberOfFractionDigits == null ? OptionalInt.empty() : OptionalInt.of(numberOfFractionDigits);
    }

    void setNumberOfFractionDigits(Integer numberOfFractionDigits) {
        this.numberOfFractionDigits = numberOfFractionDigits;
    }

    void setMultiplication(ReadingType calculated, MultiplierTypeImpl multiplierType) {
        this.calculated.set(calculated);
        this.multiplierType.set(multiplierType);

    }
}
