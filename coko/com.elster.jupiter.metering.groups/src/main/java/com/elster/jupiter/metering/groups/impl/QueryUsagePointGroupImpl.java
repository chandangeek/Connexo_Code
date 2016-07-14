package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointMembership;
import com.elster.jupiter.metering.groups.impl.query.QueryBuilder;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class QueryUsagePointGroupImpl extends AbstractUsagePointGroup implements QueryUsagePointGroup {

    private List<UsagePointQueryBuilderOperation> operations;
    private transient QueryBuilder queryBuilder;

    private final MeteringService meteringService;
    private final DataModel dataModel;

    @Inject
    QueryUsagePointGroupImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public List<UsagePoint> getMembers(Instant instant) {
        return meteringService.getUsagePointQuery().select(getCondition());
    }

    @Override
    public List<UsagePointMembership> getMembers(Range<Instant> range) {
        RangeSet<Instant> ranges = ImmutableRangeSet.of(range);
        List<UsagePointMembership> memberships = new ArrayList<>();
        for (UsagePoint usagePoint : getMembers((Instant) null)) {
            memberships.add(new UsagePointMembershipImpl(usagePoint, ranges));
        }
        return memberships;
    }

    @Override
    public boolean isMember(UsagePoint usagePoint, Instant instant) {
        return getMembers(instant).contains(usagePoint);
    }

    public void setCondition(Condition condition) {
        queryBuilder = QueryBuilder.parse(condition);
        List<QueryBuilderOperation> builderOperations = queryBuilder.getOperations();
        this.operations = convert(builderOperations);
    }

    private List<UsagePointQueryBuilderOperation> convert(List<QueryBuilderOperation> builderOperations) {
        List<UsagePointQueryBuilderOperation> operations = new ArrayList<>();
        for (QueryBuilderOperation builderOperation : builderOperations) {
            operations.add((UsagePointQueryBuilderOperation) builderOperation);
        }
        return operations;
    }

    @Override
    public void save() {
        if (getId() == 0) {
            groupFactory().persist(this);
        } else {
            groupFactory().update(this);
            operationFactory().remove(readOperations());
        }
        for (UsagePointQueryBuilderOperation operation : getOperations()) {
            operation.setGroupId(getId());
            operationFactory().persist(operation);
        }
    }

    private DataMapper<UsagePointQueryBuilderOperation> operationFactory() {
        return dataModel.mapper(UsagePointQueryBuilderOperation.class);
    }

    private DataMapper<QueryUsagePointGroup> groupFactory() {
        return dataModel.mapper(QueryUsagePointGroup.class);
    }

    private Condition getCondition() {
        if (queryBuilder == null) {
            queryBuilder = QueryBuilder.using(getOperations());
        }
        return queryBuilder.toCondition();
    }

    private List<UsagePointQueryBuilderOperation> getOperations() {
        if (operations == null) {
            operations = readOperations();
        }
        return operations;
    }

    private List<UsagePointQueryBuilderOperation> readOperations() {
        return operationFactory().find("groupId", getId());
    }
}
