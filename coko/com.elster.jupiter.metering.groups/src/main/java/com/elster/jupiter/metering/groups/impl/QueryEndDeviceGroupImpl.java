package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.SearchCriteria;
import com.elster.jupiter.metering.groups.impl.query.QueryBuilder;
import com.elster.jupiter.metering.groups.impl.query.SimpleConditionOperation;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import javax.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryEndDeviceGroupImpl extends AbstractEndDeviceGroup implements QueryEndDeviceGroup {

    private List<EndDeviceQueryBuilderOperation> operations;
    private transient QueryBuilder queryBuilder;

    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupService;
    private final DataModel dataModel;

    @Inject
    public QueryEndDeviceGroupImpl(DataModel dataModel, MeteringService meteringService, MeteringGroupsService meteringGroupService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.meteringGroupService = meteringGroupService;
    }

    @Override
    public List<EndDevice> getMembers(Instant instant) {
        return meteringGroupService.getEndDeviceQueryProvider(getQueryProviderName()).findEndDevices(instant, getCondition());
    }

    @Override
    public List<EndDeviceMembership> getMembers(Range<Instant> range) {
        RangeSet<Instant> ranges = ImmutableRangeSet.of(range);
        List<EndDeviceMembership> memberships = new ArrayList<>();
        for (EndDevice endDevice : getMembers((Instant) null)) {
            memberships.add(new EndDeviceMembershipImpl(endDevice, ranges));
        }
        return memberships;
    }

    @Override
    public boolean isMember(EndDevice endDevice, Instant instant) {
        return getMembers(instant).contains(endDevice);
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

    public Condition getCondition() {
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

    public List<SearchCriteria> getSearchCriteria() {
        List<SearchCriteria> result = new ArrayList<>();
        List<EndDeviceQueryBuilderOperation> operations = getOperations();
        for (EndDeviceQueryBuilderOperation operation : operations) {
            if (operation instanceof SimpleConditionOperation) {
                SimpleConditionOperation condition = (SimpleConditionOperation) operation;
                String fieldName = condition.getFieldName();
                Object[] possibleValues = condition.getValues();
                List<Object> values = new ArrayList<>();
                Collections.addAll(values, possibleValues);
                SearchCriteria searchCriteriaInfo = new SearchCriteria(fieldName, values);
                result.add(searchCriteriaInfo);
            }
        }
        return result;
    }

}