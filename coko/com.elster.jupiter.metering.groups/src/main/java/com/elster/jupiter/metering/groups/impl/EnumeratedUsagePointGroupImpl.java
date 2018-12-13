/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.ExecutionTimer;

import javax.inject.Inject;
import java.util.function.Supplier;

class EnumeratedUsagePointGroupImpl extends AbstractEnumeratedGroup<UsagePoint> implements EnumeratedUsagePointGroup {
    private final MeteringService meteringService;

    @Inject
    EnumeratedUsagePointGroupImpl(DataModel dataModel, EventService eventService, QueryService queryService,
                                  MeteringService meteringService, ExecutionTimer endDeviceGroupMemberCountTimer) {
        super(dataModel, eventService, queryService, endDeviceGroupMemberCountTimer);
        this.meteringService = meteringService;
    }

    @Override
    EventType onDeleteAttempt() {
        return EventType.USAGEPOINTGROUP_VALIDATE_DELETED;
    }

    @Override
    Supplier<Query<UsagePoint>> getBasicQuerySupplier() {
        return meteringService::getUsagePointQuery;
    }

    @Override
    Class<UsagePoint> getParameterApiClass() {
        return UsagePoint.class;
    }

    @Override
    Class<UsagePointGroup> getApiClass() {
        return UsagePointGroup.class;
    }

    @Override
    Class<UsagePointEntryImpl> getEntryApiClass() {
        return UsagePointEntryImpl.class;
    }

    static class UsagePointEntryImpl extends AbstractEntry<UsagePoint> {
        @Inject
        UsagePointEntryImpl(DataModel dataModel) {
            super(dataModel);
        }

        @Override
        public EnumeratedUsagePointGroup getGroup() {
            return (EnumeratedUsagePointGroup) super.getGroup();
        }
    }
}
