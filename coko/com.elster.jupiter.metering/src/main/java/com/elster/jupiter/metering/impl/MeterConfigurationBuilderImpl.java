/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Meter.MeterConfigurationBuilder;
import com.elster.jupiter.metering.Meter.MeterReadingTypeConfigurationBuilder;
import com.elster.jupiter.metering.Meter.MeterReadingTypeMultiplierConfigurationBuilder;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.graph.DiGraph;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class MeterConfigurationBuilderImpl implements MeterConfigurationBuilder {

    private final DataModel dataModel;
    private final MeterImpl meter;
    private final Instant startAt;
    private Instant endAt;
    private List<ReadingTypeConfigurationBuilder> readingTypes = new ArrayList<>();

    MeterConfigurationBuilderImpl(DataModel dataModel, MeterImpl meter, Instant startAt) {
        this.dataModel = dataModel;
        this.meter = meter;
        this.startAt = startAt;
    }

    @Override
    public MeterConfigurationBuilder endingAt(Instant endTime) {
        endAt = endTime;
        return this;
    }

    @Override
    public MeterReadingTypeConfigurationBuilder configureReadingType(ReadingType readingType) {
        ReadingTypeConfigurationBuilder readingTypeConfigurationBuilder = new ReadingTypeConfigurationBuilder(readingType);
        readingTypes.add(readingTypeConfigurationBuilder);
        return readingTypeConfigurationBuilder;
    }

    @Override
    public MeterConfiguration create() {
        MeterConfigurationImpl meterConfiguration = MeterConfigurationImpl.from(dataModel, meter, startAt);
        if (endAt != null) {
            meterConfiguration.setEnd(endAt);
        }

        DiGraph<ReadingType> multiplierGraph = new DiGraph<>();
        readingTypes.stream()
                .filter(builder -> builder.calculated != null)
                .forEach(builder -> multiplierGraph.addEdge(builder.measured, builder.calculated));
        if (!multiplierGraph.isForest()) {
            throw new IllegalArgumentException(); // TODO proper exception
        }

        readingTypes
                .forEach(builder -> {
                    MeterReadingTypeConfigurationImpl config = MeterReadingTypeConfigurationImpl.from(meterConfiguration, (IReadingType) builder.measured);
                    config.setOverflowValue(builder.overflowValue);
                    config.setNumberOfFractionDigits(builder.numberOfFractionDigits);
                    if (builder.calculated != null) {
                        config.setMultiplication((IReadingType) builder.calculated, builder.multiplierType);
                    }
                    meterConfiguration.add(config);
                });

        meter.addConfiguration(meterConfiguration);
        return meterConfiguration;
    }

    private class ReadingTypeConfigurationBuilder implements MeterReadingTypeConfigurationBuilder, MeterReadingTypeMultiplierConfigurationBuilder {
        private final ReadingType measured;
        private ReadingType calculated;
        private BigDecimal overflowValue;
        private Integer numberOfFractionDigits;
        private MultiplierTypeImpl multiplierType;

        ReadingTypeConfigurationBuilder(ReadingType readingType) {
            this.measured = readingType;
        }

        @Override
        public MeterReadingTypeConfigurationBuilder withOverflowValue(BigDecimal value) {
            this.overflowValue = value;
            return this;
        }

        @Override
        public MeterReadingTypeConfigurationBuilder withNumberOfFractionDigits(int digits) {
            this.numberOfFractionDigits = digits;
            return this;
        }

        @Override
        public MeterConfiguration create() {
            return MeterConfigurationBuilderImpl.this.create();
        }

        @Override
        public MeterReadingTypeMultiplierConfigurationBuilder withMultiplierOfType(MultiplierType multiplierOfType) {
            this.multiplierType = (MultiplierTypeImpl) multiplierOfType;
            return this;
        }

        @Override
        public MeterConfigurationBuilder calculating(ReadingType readingType) {
            this.calculated = readingType;
            return MeterConfigurationBuilderImpl.this;
        }

    }
}
