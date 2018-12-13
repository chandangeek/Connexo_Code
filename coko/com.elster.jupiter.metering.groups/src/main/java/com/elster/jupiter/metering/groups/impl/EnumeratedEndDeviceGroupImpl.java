/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.time.ExecutionTimer;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.function.Supplier;

import static com.elster.jupiter.util.conditions.Where.where;

class EnumeratedEndDeviceGroupImpl extends AbstractEnumeratedGroup<EndDevice> implements EnumeratedEndDeviceGroup {
    private final MeteringService meteringService;

    @Inject
    EnumeratedEndDeviceGroupImpl(DataModel dataModel, EventService eventService, QueryService queryService,
                                 MeteringService meteringService, ExecutionTimer endDeviceGroupMemberCountTimer) {
        super(dataModel, eventService, queryService, endDeviceGroupMemberCountTimer);
        this.meteringService = meteringService;
    }

    @Override
    EventType onDeleteAttempt() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED;
    }

    @Override
    Supplier<Query<EndDevice>> getBasicQuerySupplier() {
        return meteringService::getEndDeviceQuery;
    }

    @Override
    Class<EndDevice> getParameterApiClass() {
        return EndDevice.class;
    }

    @Override
    Class<EndDeviceGroup> getApiClass() {
        return EndDeviceGroup.class;
    }

    @Override
    Class<EndDeviceEntryImpl> getEntryApiClass() {
        return EndDeviceEntryImpl.class;
    }

    @Override
    public Subquery getAmrIdSubQuery(AmrSystem... amrSystems) {
        MeteringService service = getDataModel().getInstance(MeteringService.class);
        Query<EndDevice> endDeviceQuery = service.getEndDeviceQuery();

        QueryExecutor<EndDeviceEntryImpl> query = getDataModel().query(EndDeviceEntryImpl.class);
        Subquery subQueryEndDeviceIdInGroup = query.asSubquery(where("group").isEqualTo(this), "member");
        Condition condition = ListOperator.IN.contains(subQueryEndDeviceIdInGroup, "id");
        if (amrSystems.length > 0) {
            condition = condition.and(ListOperator.IN.contains("amrSystem", Arrays.asList(amrSystems)));
        }

        return endDeviceQuery.asSubquery(condition, "amrId");
    }

    static class EndDeviceEntryImpl extends AbstractEntry<EndDevice> {
        @Inject
        EndDeviceEntryImpl(DataModel dataModel) {
            super(dataModel);
        }

        @Override
        public EnumeratedEndDeviceGroup getGroup() {
            return (EnumeratedEndDeviceGroup) super.getGroup();
        }
    }
}
