package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import java.util.Objects;

public abstract class UsagePointDetailImpl implements UsagePointDetail {

    private AmiBillingReadyKind amiBillingReady;
    private boolean checkBilling;
    private UsagePointConnectedKind connectionState;
    private boolean minimalUsageExpected;
    private String serviceDeliveryRemark;

    private Interval interval;
    private final Clock clock;

    //Associations
    private Reference<UsagePoint> usagePoint = ValueReference.absent();


    @Inject
    UsagePointDetailImpl(Clock clock) {
        this.clock = clock;
    }

    UsagePointDetailImpl init(UsagePoint usagePoint, Interval interval) {
        this.usagePoint.set(usagePoint);
        this.interval = Objects.requireNonNull(interval);
        this.amiBillingReady = AmiBillingReadyKind.UNKNOWN;
        this.connectionState = UsagePointConnectedKind.UNKNOWN;
        return this;
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
    public boolean conflictsWith(UsagePointDetail other) {
        return interval.overlaps(other.getInterval());
    }

    @Override
    public UsagePoint getUsagePoint() {
        return usagePoint.get();
    }
}
