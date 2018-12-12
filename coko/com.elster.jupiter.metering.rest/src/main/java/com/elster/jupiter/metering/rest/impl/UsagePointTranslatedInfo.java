/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.UsagePoint;

import java.time.Clock;

public class UsagePointTranslatedInfo extends UsagePointInfo {
    public String displayServiceCategory;
    public String displayMetrologyConfiguration;

    public UsagePointTranslatedInfo(UsagePoint usagePoint, Clock clock) {
        super(usagePoint, clock);

    }
}