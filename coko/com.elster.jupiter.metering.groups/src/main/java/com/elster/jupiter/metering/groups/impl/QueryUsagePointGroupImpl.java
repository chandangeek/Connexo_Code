package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.impl.search.UsagePointGroupSearchableProperty;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.time.ExecutionTimer;

import javax.inject.Inject;
import java.util.function.Supplier;

import static com.elster.jupiter.util.conditions.Where.where;

class QueryUsagePointGroupImpl extends AbstractQueryGroup<UsagePoint> implements QueryUsagePointGroup {
    private final MeteringService meteringService;

    @Inject
    QueryUsagePointGroupImpl(DataModel dataModel, EventService eventService, MeteringService meteringService,
                             MeteringGroupsService meteringGroupService, SearchService searchService,
                             ExecutionTimer endDeviceGroupMemberCountTimer, Thesaurus thesaurus) {
        super(dataModel, eventService, meteringGroupService, searchService, endDeviceGroupMemberCountTimer, thesaurus);
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
    Class<QueryUsagePointGroupCondition> getConditionApiClass() {
        return QueryUsagePointGroupCondition.class;
    }

    static class QueryUsagePointGroupCondition extends QueryGroupCondition {
        @Inject
        QueryUsagePointGroupCondition(DataModel dataModel) {
            super(dataModel);
        }

        @Override
        Class<QueryUsagePointGroupConditionValue> getConditionValueApiClass() {
            return QueryUsagePointGroupConditionValue.class;
        }
    }

    static class QueryUsagePointGroupConditionValue extends QueryGroupConditionValue {
        @Inject
        QueryUsagePointGroupConditionValue() {
        }
    }

    @Override
    public void delete() {
        getDataModel().query(QueryUsagePointGroupConditionValue.class, QueryUsagePointGroupCondition.class)
                .select(where(QueryUsagePointGroupConditionValue.Fields.VALUE.fieldName()).isEqualTo(String.valueOf(getId()))
                        .and(where(QueryUsagePointGroupConditionValue.Fields.GROUP_CONDITION.fieldName() + "." + QueryUsagePointGroupCondition.Fields.PROPERTY.fieldName())
                                .isEqualTo(UsagePointGroupSearchableProperty.PROPERTY_NAME)))
                .stream().findFirst().ifPresent(group -> {
            throw new VetoDeleteUsagePointGroupException(this.getThesaurus());
        });
        super.delete();
    }
}
