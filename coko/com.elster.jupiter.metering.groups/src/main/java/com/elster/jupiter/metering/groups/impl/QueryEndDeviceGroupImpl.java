/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.time.ExecutionTimer;

import javax.inject.Inject;
import java.util.function.Supplier;

class QueryEndDeviceGroupImpl extends AbstractQueryGroup<EndDevice> implements QueryEndDeviceGroup {
    private final MeteringService meteringService;

    @Inject
    QueryEndDeviceGroupImpl(DataModel dataModel, EventService eventService, MeteringService meteringService,
                            MeteringGroupsService meteringGroupService, SearchService searchService,
                            ExecutionTimer endDeviceGroupMemberCountTimer, Thesaurus thesaurus) {
        super(dataModel, eventService, meteringGroupService, searchService, endDeviceGroupMemberCountTimer, thesaurus);
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
    Class<QueryEndDeviceGroupCondition> getConditionApiClass() {
        return QueryEndDeviceGroupCondition.class;
    }

    static class QueryEndDeviceGroupCondition extends QueryGroupCondition {
        @Inject
        QueryEndDeviceGroupCondition(DataModel dataModel) {
            super(dataModel);
        }

        @Override
        Class<QueryEndDeviceGroupConditionValue> getConditionValueApiClass() {
            return QueryEndDeviceGroupConditionValue.class;
        }
    }

    static class QueryEndDeviceGroupConditionValue extends QueryGroupConditionValue {
        @Inject
        QueryEndDeviceGroupConditionValue() {
        }
    }
}
