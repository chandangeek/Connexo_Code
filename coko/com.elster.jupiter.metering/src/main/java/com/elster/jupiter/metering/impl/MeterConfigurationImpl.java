/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MeterConfigurationImpl implements MeterConfiguration {

    private long id;
    private Reference<Meter> meter = ValueReference.absent();
    private Interval interval;
    private List<MeterReadingTypeConfigurationImpl> readingTypeConfigs = new ArrayList<>();

    private final DataModel dataModel;

    private long version;
    private Instant createTime;
    private Instant obsoleteTime;
    private Instant modTime;
    private String userName;

    @Inject
    MeterConfigurationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public List<MeterReadingTypeConfiguration> getReadingTypeConfigs() {
        return Collections.unmodifiableList(readingTypeConfigs);
    }

    public static MeterConfigurationImpl from(DataModel dataModel, MeterImpl meter, Instant startAt) {
        return dataModel.getInstance(MeterConfigurationImpl.class).init(meter, startAt);
    }

    private MeterConfigurationImpl init(MeterImpl meter, Instant startAt) {
        this.meter.set(meter);
        this.interval = Interval.of(Range.atLeast(startAt));
        return this;
    }

    @Override
    public void endAt(Instant endAt) {
        dataModel.mapper(MeterConfiguration.class).lockObjectIfVersion(version, id);
        setEnd(endAt);
        dataModel.mapper(MeterConfiguration.class).update(this);
    }

    void add(MeterReadingTypeConfigurationImpl config) {
        readingTypeConfigs.add(config);

    }

    void setEnd(Instant endAt) {
        interval = Interval.of(Ranges.copy(interval.toClosedRange()).withClosedUpperBound(endAt));
    }
}
