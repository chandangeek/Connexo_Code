package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.QueryUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataMapper;
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

    @SuppressWarnings("unused")
	private long version;
    @SuppressWarnings("unused")
	private UtcInstant createTime;
    @SuppressWarnings("unused")
	private UtcInstant modTime;
    @SuppressWarnings("unused")
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
        return Bus.getMeteringService().getUsagePointQuery().select(getCondition());
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
