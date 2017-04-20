/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class UsagePointPrivileges {

    private UsagePointPrivileges() {}

    private static final String LINK_METROLOGY_CONFIG = "usagepoint.action.link.metrology.configuration";

    public static List<String> getUsagePointPrivilegesBasedOnStage(UsagePoint usagePoint) {
        List<String> privileges = new ArrayList<>();
        if(EnumSet.of(UsagePointStage.Key.PRE_OPERATIONAL, UsagePointStage.Key.SUSPENDED).contains(usagePoint.getState().getStage().getKey())) {
            if(usagePoint.getEffectiveMetrologyConfigurations().stream()
                    .map(EffectiveMetrologyConfigurationOnUsagePoint::getRange)
                    .allMatch(Range::hasUpperBound)){
                privileges.add(LINK_METROLOGY_CONFIG);
            }
        }
        return privileges;
    }
}
