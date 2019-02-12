/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.ZoneFilter;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import java.util.List;

public class ZoneFilterImpl implements ZoneFilter {

    private List<Long> zoneTypes;

    @Override
    public ZoneFilter setZoneTypes(List<Long> zoneTypes) {
        this.zoneTypes = zoneTypes;
        return this;
    }

    @Override
    public Condition toCondition() {
        Condition condition = Condition.TRUE;
        if (zoneTypes != null) {
            condition = condition.and(Where.where("zoneType.id").in(zoneTypes));
        }
        return condition;
    }
}
