/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

@HasUniqueDetailsForInterval(groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.UNIQUE_DETAILS + "}")
public abstract class UsagePointDetailImpl implements UsagePointDetail {

    static final Map<String, Class<? extends UsagePointDetail>> IMPLEMENTERS = createImplementers();

    static Map<String, Class<? extends UsagePointDetail>> createImplementers() {
        return ImmutableMap.<String, Class<? extends UsagePointDetail>> builder()
                .put("E", ElectricityDetailImpl.class)
                .put("G", GasDetailImpl.class)
                .put("W", WaterDetailImpl.class)
                .put("H", HeatDetailImpl.class)
                .put("D", DefaultDetailImpl.class).build();
    }

    private YesNoAnswer collar;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private Interval interval;
    private final Clock clock;

    private DataModel dataModel;

    // Associations
    private Reference<UsagePoint> usagePoint = ValueReference.absent();

    @Inject
    UsagePointDetailImpl(Clock clock, DataModel dataModel) {
        this.clock = clock;
        this.dataModel = dataModel;
    }

    UsagePointDetailImpl init(UsagePoint usagePoint, Interval interval) {
        this.usagePoint.set(usagePoint);
        this.interval = Objects.requireNonNull(interval);
        return this;
    }

    @Override
    public void update() {
        dataModel.update(this);
    }

    @Override
    public boolean conflictsWith(UsagePointDetail other) {
        return overlaps(other.getRange());
    }

    @Override
    public boolean isCurrent() {
        return isEffectiveAt(clock.instant());
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return usagePoint.get();
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("usagePoint", usagePoint).add("interval", interval).toString();
    }

    void terminate(Instant date) {
        if (!interval.toClosedOpenRange().contains(date)) {
            throw new IllegalArgumentException();
        }
        interval = Interval.of(Range.closedOpen(getRange().lowerEndpoint(), date));
    }

    @Override
    public YesNoAnswer isCollarInstalled() {
        return collar;
    }

    public void setCollar(YesNoAnswer collar) {
        this.collar = collar;
    }
}
