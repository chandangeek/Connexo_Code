package com.elster.jupiter.servicecall;


import com.elster.jupiter.orm.QueryStream;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface ServiceCallFinder {
    ServiceCallFinder setStart(Integer start);

    ServiceCallFinder setLimit(Integer limit);

    ServiceCallFinder setReference(String reference);

    ServiceCallFinder setType(int type);

    ServiceCallFinder setState(int state);

    ServiceCallFinder withCreationTimeIn(Range<Instant> interval);

    ServiceCallFinder withModTimeIn(Range<Instant> interval);

    List<ServiceCall> find();

    QueryStream<ServiceCall> stream();
}
