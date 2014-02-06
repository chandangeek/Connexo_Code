package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointMembership;
import com.elster.jupiter.metering.groups.impl.query.QueryBuilder;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueryUsagePointGroupImpl extends AbstractUsagePointGroup implements QueryUsagePointGroup {

    private List<UsagePointQueryBuilderOperation> operations;
    private transient QueryBuilder queryBuilder;

    private final MeteringService meteringService;
    private final DataModel dataModel;

    @Inject
    public QueryUsagePointGroupImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    @Override
    public List<UsagePoint> getMembers(Date date) {
        return meteringService.getUsagePointQuery().select(getCondition());
    }

    @Override
    public List<UsagePointMembership> getMembers(Interval interval) {
        IntermittentInterval intervals = new IntermittentInterval(interval);
        List<UsagePointMembership> memberships = new ArrayList<>();
        for (UsagePoint usagePoint : getMembers((Date) null)) {
            memberships.add(new UsagePointMembershipImpl(usagePoint, intervals));
        }
        return memberships;
    }

    @Override
    public boolean isMember(UsagePoint usagePoint, Date date) {
        return getMembers(date).contains(usagePoint);
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
        if (id == 0) {
            groupFactory().persist(this);
        } else {
            groupFactory().update(this);
            operationFactory().remove(readOperations());
        }
        for (UsagePointQueryBuilderOperation operation : getOperations()) {
            operation.setGroupId(id);
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
        return operationFactory().find("groupId", id);
    }
}
