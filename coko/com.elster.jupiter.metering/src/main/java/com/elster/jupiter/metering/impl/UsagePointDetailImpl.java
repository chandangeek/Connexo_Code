package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;

public abstract class UsagePointDetailImpl implements UsagePointDetail {

    static final Map<String, Class<? extends UsagePointDetail>> IMPLEMENTERS = createImplementers();

    static Map<String, Class<? extends UsagePointDetail>> createImplementers() {
        return ImmutableMap.<String, Class<? extends UsagePointDetail>> builder()
                .put("E", ElectricityDetailImpl.class)
                .put("G", GasDetailImpl.class)
                .put("W", WaterDetailImpl.class)
                .put("D", DefaultDetailImpl.class).build();
    }

    private AmiBillingReadyKind amiBillingReady;
    private boolean checkBilling;
    private UsagePointConnectedKind connectionState;
    private boolean minimalUsageExpected;
    private String serviceDeliveryRemark;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private UtcInstant createTime;
    @SuppressWarnings("unused")
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private Interval interval;
    private final Clock clock;

    private DataModel dataModel;

    //Associations
    private Reference<UsagePoint> usagePoint = ValueReference.absent();


    @Inject
    UsagePointDetailImpl(Clock clock, DataModel dataModel) {
        this.clock = clock;
        this.dataModel = dataModel;
    }

    UsagePointDetailImpl init(UsagePoint usagePoint, Interval interval) {
        this.usagePoint.set(usagePoint);
        this.interval = Objects.requireNonNull(interval);
        this.amiBillingReady = AmiBillingReadyKind.UNKNOWN;
        this.connectionState = UsagePointConnectedKind.UNKNOWN;
        return this;
    }

    @Override
    public void update() {
        dataModel.update(this);
    }

    @Override
    public boolean conflictsWith(UsagePointDetail other) {
        return interval.overlaps(other.getInterval());
    }

    @Override
    public boolean isCurrent() {
        return interval.isCurrent(clock);
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public AmiBillingReadyKind getAmiBillingReady() {
        return amiBillingReady;
    }

    @Override
    public boolean isCheckBilling() {
        return checkBilling;
    }

    @Override
    public UsagePointConnectedKind getConnectionState() {
        return connectionState;
    }

    @Override
    public boolean isMinimalUsageExpected() {
        return minimalUsageExpected;
    }

    @Override
    public String getServiceDeliveryRemark() {
        return serviceDeliveryRemark;
    }

    @Override
    public void setAmiBillingReady(AmiBillingReadyKind amiBillingReady) {
        this.amiBillingReady = amiBillingReady;
    }

    @Override
    public void setCheckBilling(boolean checkBilling) {
        this.checkBilling = checkBilling;
    }

    @Override
    public void setConnectionState(UsagePointConnectedKind connectionState) {
        this.connectionState = connectionState;
    }

    @Override
    public void setMinimalUsageExpected(boolean minimalUsageExpected) {
        this.minimalUsageExpected = minimalUsageExpected;
    }

    @Override
    public void setServiceDeliveryRemark(String serviceDeliveryRemark) {
        this.serviceDeliveryRemark = serviceDeliveryRemark;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return usagePoint.get();
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("usagePoint", usagePoint).add("interval", interval).toString();
    }

    void terminate(Date date) {
        if (!interval.isEffective(date)) {
            throw new IllegalArgumentException();
        }
        interval = interval.withEnd(date);
    }
}
