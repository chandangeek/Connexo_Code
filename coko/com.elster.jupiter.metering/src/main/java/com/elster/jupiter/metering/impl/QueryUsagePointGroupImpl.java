package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.QueryUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Date;
import java.util.List;

public class QueryUsagePointGroupImpl implements QueryUsagePointGroup {

    private long id;

    private String name;
    private String mRID;
    private String description;
    private String aliasName;
    private String type;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    private List<QueryBuilderOperation> operations;
    private transient QueryBuilder queryBuilder;

    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<UsagePoint> getMembers(Date date) {
        QueryExecutor<UsagePoint> queryExecutor = Bus.getOrmClient().getUsagePointFactory().with();
        return Bus.getQueryService().wrap(queryExecutor).select(getCondition());
    }

    @Override
    public boolean isMember(UsagePoint usagePoint, Date date) {
        return getMembers(date).contains(usagePoint);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setCondition(Condition condition) {
        queryBuilder = QueryBuilder.parse(condition);
        operations = queryBuilder.getOperations();
    }

    @Override
    public void save() {
        if (id == 0) {
            Bus.getOrmClient().getQueryUsagePointGroupFactory().persist(this);
            for (QueryBuilderOperation operation : getOperations()) {
                operation.setGroupId(id);
                Bus.getOrmClient().getQueryBuilderOperationFactory().persist(operation);
            }
        }

    }

    private Condition getCondition() {
        if (queryBuilder == null) {
            queryBuilder = QueryBuilder.using(getOperations());
        }
        return queryBuilder.toCondition();
    }

    private List<QueryBuilderOperation> getOperations() {
        if (operations == null) {
            operations = Bus.getOrmClient().getQueryBuilderOperationFactory().find("groupId", id);
        }
        return operations;
    }
}
