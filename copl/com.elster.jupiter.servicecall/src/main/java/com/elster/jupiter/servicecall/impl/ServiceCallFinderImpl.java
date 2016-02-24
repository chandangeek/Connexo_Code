package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFinder;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;


public class ServiceCallFinderImpl implements ServiceCallFinder {
    private DataModel dataModel;
    private Condition condition;
    private Order order;
    private Integer start;
    private Integer limit;

    public ServiceCallFinderImpl() {

    }

    public ServiceCallFinderImpl(DataModel dataModel, Order order) {
        this.dataModel = dataModel;
       // this.condition = condition;
        this.order = order;
    }

    @Override
    public ServiceCallFinder setStart(Integer start) {
        this.start = start;
        return this;
    }

    @Override
    public ServiceCallFinder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public ServiceCallFinder setReference(String reference) {
        this.condition = this.condition.and(where(ServiceCallImpl.Fields.externalReference.fieldName()).like(reference));
        return this;
    }

    @Override
    public ServiceCallFinder setType(int type) {
        this.condition = this.condition.and(where(ServiceCallImpl.Fields.type.fieldName()).isEqualTo(type));
        return this;
    }

    @Override
    public ServiceCallFinder setState(int state) {
        this.condition = this.condition.and(where(ServiceCallImpl.Fields.state.fieldName()).isEqualTo(state));
        return this;
    }

    @Override
    public ServiceCallFinder withCreationTimeIn(Range<Instant> interval) {
        condition = this.condition.and(where(ServiceCallImpl.Fields.createTime.fieldName()).in(interval));
        return this;
    }

    @Override
    public ServiceCallFinder withModTimeIn(Range<Instant> interval) {
        condition = this.condition.and(where(ServiceCallImpl.Fields.modTime.fieldName()).in(interval));
        return this;
    }

    @Override
    public List<ServiceCall> find() {
        return stream().select();
    }

    @Override
    public QueryStream<ServiceCall> stream() {
        QueryStream<ServiceCall> queryStream = dataModel.stream(ServiceCall.class)
                .filter(condition)
                .sorted(order);
        if (start != null) {
            queryStream.skip(start);
        }
        if(limit != null) {
            queryStream.limit(limit);
        }
        return queryStream;
    }
}
