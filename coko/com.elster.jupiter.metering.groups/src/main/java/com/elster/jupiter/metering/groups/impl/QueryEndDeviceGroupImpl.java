package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
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

public class QueryEndDeviceGroupImpl extends AbstractEndDeviceGroup implements QueryEndDeviceGroup {

    private List<EndDeviceQueryBuilderOperation> operations;
    private transient QueryBuilder queryBuilder;

    private final MeteringService meteringService;
    private final DataModel dataModel;

    @Inject
    public QueryEndDeviceGroupImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    @Override
    public List<EndDevice> getMembers(Date date) {
        return meteringService.getEndDeviceQuery().select(getCondition());
    }

    @Override
    public List<EndDeviceMembership> getMembers(Interval interval) {
        IntermittentInterval intervals = new IntermittentInterval(interval);
        List<EndDeviceMembership> memberships = new ArrayList<>();
        for (EndDevice endDevice : getMembers((Date) null)) {
            memberships.add(new EndDeviceMembershipImpl(endDevice, intervals));
        }
        return memberships;
    }

    @Override
    public boolean isMember(EndDevice endDevice, Date date) {
        return getMembers(date).contains(endDevice);
    }

    public void setCondition(Condition condition) {
        queryBuilder = QueryBuilder.parse(condition);
        operations = convert(queryBuilder.getOperations());
    }

    @Override
    public void save() {
        if (id == 0) {
            groupFactory().persist(this);
        } else {
            groupFactory().update(this);
            operationFactory().remove(readOperations());
        }
        for (EndDeviceQueryBuilderOperation operation : getOperations()) {
            operation.setGroupId(id);
            operationFactory().persist(operation);
        }
    }

    private DataMapper<EndDeviceQueryBuilderOperation> operationFactory() {
        return dataModel.mapper(EndDeviceQueryBuilderOperation.class);
    }

    private DataMapper<QueryEndDeviceGroup> groupFactory() {
        return dataModel.mapper(QueryEndDeviceGroup.class);
    }

    private Condition getCondition() {
        if (queryBuilder == null) {
            queryBuilder = QueryBuilder.using(getOperations());
        }
        return queryBuilder.toCondition();
    }

    private List<EndDeviceQueryBuilderOperation> getOperations() {
        if (operations == null) {
            operations = readOperations();
        }
        return operations;
    }

    private List<EndDeviceQueryBuilderOperation> readOperations() {
        return operationFactory().find("groupId", id);
    }

    private List<EndDeviceQueryBuilderOperation> convert(List<QueryBuilderOperation> builderOperations) {
        List<EndDeviceQueryBuilderOperation> operations = new ArrayList<>();
        for (QueryBuilderOperation builderOperation : builderOperations) {
            operations.add((EndDeviceQueryBuilderOperation) builderOperation);
        }
        return operations;
    }

}
