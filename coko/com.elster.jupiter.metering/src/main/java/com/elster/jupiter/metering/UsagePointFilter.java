package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public class UsagePointFilter {
    private boolean accountabilityOnly;
    private String mrid;

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
}
