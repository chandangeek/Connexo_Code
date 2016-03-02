package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFinder;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;


public class ServiceCallFinderImpl implements ServiceCallFinder {
    private DataModel dataModel;
    private Condition condition = Condition.TRUE;
    private Order parentOrder;
    private Order modTimeOrder;
    private Integer start;
    private Integer limit;

    public ServiceCallFinderImpl() {

    }

    public ServiceCallFinderImpl(DataModel dataModel, Order parentOrder, Order modTimeOrder) {
        this.dataModel = dataModel;
        this.parentOrder = parentOrder;
        this.modTimeOrder = modTimeOrder;
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
        this.condition = this.condition.and(where(ServiceCallImpl.Fields.externalReference.fieldName()).like(reference).or(where("id").like(Long.parseLong(reference.substring(3)) + "")));
        return this;
    }

    @Override
    public ServiceCallFinder setType(List<String> types) {
        if (types.isEmpty()) {
            return this;
        }
        this.condition = this.condition.and(ofAnyType(types));
        return this;
    }

    private Condition ofAnyType(List<String> types) {
        return types.stream()
                .map(typeName -> where(ServiceCallImpl.Fields.type.fieldName() + "." + ServiceCallTypeImpl.Fields.name.fieldName())
                        .isEqualTo(typeName))
                .reduce(Condition.FALSE, Condition::or);
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
                .join(ServiceCallType.class)
                .filter(condition)
                .sorted(parentOrder, modTimeOrder);
        if (start != null) {
            queryStream.skip(start);
        }
        if(limit != null) {
            queryStream.limit(limit);
        }
        return queryStream;
    }
}
