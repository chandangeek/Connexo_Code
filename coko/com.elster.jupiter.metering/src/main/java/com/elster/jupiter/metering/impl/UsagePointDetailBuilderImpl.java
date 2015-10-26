package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.UsagePointDetailBuilder;

public abstract class UsagePointDetailBuilderImpl implements UsagePointDetailBuilder {

    protected AmiBillingReadyKind amiBillingReady;
    protected boolean checkBilling;
    protected UsagePointConnectedKind connectionState;
    protected boolean minimalUsageExpected;
    protected String serviceDeliveryRemark;

    @Override
    public UsagePointDetailBuilder withAmiBillingReady(AmiBillingReadyKind amiBillingReady) {
        this.amiBillingReady = amiBillingReady;
        return this;
    }

    @Override
    public UsagePointDetailBuilder withCheckBilling(boolean checkBilling) {
        this.checkBilling = checkBilling;
        return this;
    }

    @Override
    public UsagePointDetailBuilder withConnectionState(UsagePointConnectedKind connectionState) {
        this.connectionState = connectionState;
        return this;
    }

    @Override
    public UsagePointDetailBuilder withMinimalUsageExpected(boolean minimalUsageExpected) {
        this.minimalUsageExpected = minimalUsageExpected;
        return this;
    }

    @Override
    public UsagePointDetailBuilder withServiceDeliveryRemark(String serviceDeliveryRemark) {
        this.serviceDeliveryRemark = serviceDeliveryRemark;
        return this;
    }

}
