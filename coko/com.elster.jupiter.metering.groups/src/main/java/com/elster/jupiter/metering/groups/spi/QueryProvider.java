/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.spi;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.search.SearchablePropertyCondition;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

/**
 * Responsible for converting actual content of the group into a list of members.
 */
@ConsumerType
public interface QueryProvider<T> {

    QueryProvider<T> init(Supplier<Query<T>> basicQueryProvider);

    String getName();

    List<T> executeQuery(Instant instant, List<SearchablePropertyCondition> conditions);

    List<T> executeQuery(Instant instant, List<SearchablePropertyCondition> conditions, int start, int limit);

    Query<T> getQuery(List<SearchablePropertyCondition> conditions);

}
