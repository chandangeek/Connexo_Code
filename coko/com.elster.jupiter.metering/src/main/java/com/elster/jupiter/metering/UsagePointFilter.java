/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public class UsagePointFilter {
    private boolean accountabilityOnly;
    private String name;

    public boolean isAccountabilityOnly() {
        return accountabilityOnly;
    }

    public void setAccountabilityOnly(boolean accountabilityOnly) {
        this.accountabilityOnly = accountabilityOnly;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
