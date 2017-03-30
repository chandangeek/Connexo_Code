/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.orm.DataModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UsagePointConfigurationBuilderImpl implements UsagePoint.UsagePointConfigurationBuilder {
    private final DataModel dataModel;
    private final UsagePointImpl usagePoint;
    private final Instant startAt;
    private Instant endAt;
    private List<ReadingTypeConfigurationBuilder> readingTypes = new ArrayList<>();

    UsagePointConfigurationBuilderImpl(DataModel dataModel, UsagePointImpl usagePoint, Instant startAt) {
        this.dataModel = dataModel;
        this.usagePoint = usagePoint;
        this.startAt = startAt;
    }

    @Override
    public UsagePoint.UsagePointConfigurationBuilder endingAt(Instant endTime) {
        endAt = endTime;
        return this;
    }

    @Override
    public UsagePoint.UsagePointReadingTypeConfigurationBuilder configureReadingType(ReadingType readingType) {
        ReadingTypeConfigurationBuilder readingTypeConfigurationBuilder = new ReadingTypeConfigurationBuilder((IReadingType) readingType);
        readingTypes.add(readingTypeConfigurationBuilder);
        return readingTypeConfigurationBuilder;
    }

    @Override
    public UsagePointConfiguration create() {
        UsagePointConfigurationImpl usagePointConfiguration = UsagePointConfigurationImpl.from(dataModel, usagePoint, startAt);
        if (endAt != null) {
            usagePointConfiguration.setEnd(endAt);
        }

        readingTypes.stream()
                .forEach(builder -> {
                    UsagePointReadingTypeConfigurationImpl config = UsagePointReadingTypeConfigurationImpl.from(usagePointConfiguration, builder.measured);
                    if (builder.calculated != null) {
                        config.setMultiplication(builder.calculated, builder.multiplierType);
                    }
                    usagePointConfiguration.add(config);
                });

        usagePoint.addConfiguration(usagePointConfiguration);
        return usagePointConfiguration;
    }

    private class ReadingTypeConfigurationBuilder implements UsagePoint.UsagePointReadingTypeConfigurationBuilder, UsagePoint.UsagePointReadingTypeMultiplierConfigurationBuilder {
        private final IReadingType measured;
        private IReadingType calculated;
        private MultiplierTypeImpl multiplierType;


        public ReadingTypeConfigurationBuilder(IReadingType readingType) {
            this.measured = readingType;
        }

        @Override
        public UsagePointConfiguration create() {
            return UsagePointConfigurationBuilderImpl.this.create();
        }

        @Override
        public UsagePoint.UsagePointReadingTypeMultiplierConfigurationBuilder withMultiplierOfType(MultiplierType multiplierOfType) {
            this.multiplierType = (MultiplierTypeImpl) multiplierOfType;
            return this;
        }

        @Override
        public UsagePoint.UsagePointConfigurationBuilder calculating(ReadingType readingType) {
            this.calculated = (IReadingType) readingType;
            return UsagePointConfigurationBuilderImpl.this;
        }

    }
}
