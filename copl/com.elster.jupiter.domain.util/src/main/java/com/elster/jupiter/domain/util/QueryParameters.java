/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Order;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by bvn on 5/6/15.
 */
@ConsumerType
public interface QueryParameters {
    Optional<Integer> getStart();

    Optional<Integer> getLimit();

    List<Order> getSortingColumns();

    default <T> QueryStream<T> apply(QueryStream<T> stream) {
        stream = getStart().map(stream::skip).orElse(stream);
        stream = getLimit().map(i -> i + 1).map(stream::limit).orElse(stream);
        List<Order> sortingColumns = getSortingColumns();
        if (Objects.nonNull(sortingColumns) && !sortingColumns.isEmpty()) {
            stream = stream.sorted(sortingColumns);
        }
        return stream;
    }
}
