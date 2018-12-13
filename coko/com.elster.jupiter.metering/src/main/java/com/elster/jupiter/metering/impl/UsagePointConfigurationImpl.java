/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointReadingTypeConfiguration;
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

public class UsagePointConfigurationImpl implements UsagePointConfiguration {

    private long id;
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    private Interval interval;
    private List<UsagePointReadingTypeConfigurationImpl> readingTypeConfigs = new ArrayList<>();

    private final DataModel dataModel;

    private long version;
    private Instant createTime;
    private Instant obsoleteTime;
    private Instant modTime;
    private String userName;

    @Inject
    UsagePointConfigurationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public List<UsagePointReadingTypeConfiguration> getReadingTypeConfigs() {
        return Collections.unmodifiableList(readingTypeConfigs);
    }

    public static UsagePointConfigurationImpl from(DataModel dataModel, UsagePointImpl usagePoint, Instant startAt) {
        return dataModel.getInstance(UsagePointConfigurationImpl.class).init(usagePoint, startAt);
    }

    private UsagePointConfigurationImpl init(UsagePointImpl usagePoint, Instant startAt) {
        this.usagePoint.set(usagePoint);
        this.interval = Interval.of(Range.atLeast(startAt));
        return this;
    }

    @Override
    public void endAt(Instant endAt) {
        dataModel.mapper(UsagePointConfiguration.class).lockObjectIfVersion(version, id);
        setEnd(endAt);
        dataModel.mapper(UsagePointConfiguration.class).update(this);
    }

    void add(UsagePointReadingTypeConfigurationImpl config) {
        readingTypeConfigs.add(config);

    }

    void setEnd(Instant endAt) {
        interval = Interval.of(Ranges.copy(interval.toClosedRange()).withClosedUpperBound(endAt));
    }
}
