/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone;

import com.elster.jupiter.util.conditions.Condition;

import java.util.List;

public interface ZoneFilter {
    ZoneFilter setZoneTypes(List<Long> zoneTypes);

    Condition toCondition();
}
