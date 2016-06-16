package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MetrologyContract;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public class UsagePointFilter {
    private boolean accountabilityOnly;
    private String mrid;
    private MetrologyContract metrologyContract;

    public boolean isAccountabilityOnly() {
        return accountabilityOnly;
    }

    public void setAccountabilityOnly(boolean accountabilityOnly) {
        this.accountabilityOnly = accountabilityOnly;
    }

    public String getMrid() {
        return mrid;
    }

    public void setMrid(String name) {
        this.mrid = name;
    }

    public MetrologyContract getMetrologyContract() {
        return metrologyContract;
    }

    public void setMetrologyContract(MetrologyContract metrologyContract) {
        this.metrologyContract = metrologyContract;
    }
}
