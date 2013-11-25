package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.QueryUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Date;
import java.util.List;

public class QueryUsagePointGroupImpl extends AbstractUsagePointGroup implements QueryUsagePointGroup {

    private List<QueryBuilderOperation> operations;
    private transient QueryBuilder queryBuilder;

    @Override
    public List<UsagePoint> getMembers(Date date) {
        QueryExecutor<UsagePoint> queryExecutor = Bus.getOrmClient().getUsagePointFactory().with();
        return Bus.getQueryService().wrap(queryExecutor).select(getCondition());
    }

    @Override
    public boolean isMember(UsagePoint usagePoint, Date date) {
        return getMembers(date).contains(usagePoint);
    }

    public void setCondition(Condition condition) {
        queryBuilder = QueryBuilder.parse(condition);
        operations = queryBuilder.getOperations();
    }

    @Override
    public void save() {
        if (id == 0) {
            groupFactory().persist(this);
        } else {
            groupFactory().update(this);
            operationFactory().remove(readOperations());
        }
        for (QueryBuilderOperation operation : getOperations()) {
            operation.setGroupId(id);
            operationFactory().persist(operation);
        }
    }

    private DataMapper<QueryBuilderOperation> operationFactory() {
        return Bus.getOrmClient().getQueryBuilderOperationFactory();
    }

    private DataMapper<QueryUsagePointGroup> groupFactory() {
        return Bus.getOrmClient().getQueryUsagePointGroupFactory();
    }

    private Condition getCondition() {
        if (queryBuilder == null) {
            queryBuilder = QueryBuilder.using(getOperations());
        }
        return queryBuilder.toCondition();
    }

    private List<QueryBuilderOperation> getOperations() {
        if (operations == null) {
            operations = readOperations();
        }
        return operations;
    }

    private List<QueryBuilderOperation> readOperations() {
        return operationFactory().find("groupId", id);
    }
}
