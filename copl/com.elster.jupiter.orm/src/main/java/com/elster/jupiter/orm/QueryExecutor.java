/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Hint;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * This interface is only intended for use by domain classes,
 * who typically use the BasicQuery api.
 * Service layer classes (Rest) should use com.elster.jupiter.domain.util.Query.
 */
@ProviderType
public interface QueryExecutor<T> extends BasicQuery<T> {

    // creation api
    void setRestriction(Condition condition);

    // domain util query support
    List<T> select(Condition condition, Order[] orderBy, boolean eager, String[] exceptions);

    List<T> select(Condition condition, Order[] orderBy, boolean eager, String[] exceptions, int from, int to);

    List<T> select(Condition condition, Hint[] hints, Order[] orderBy, boolean eager, String[] exceptions, int from, int to);

    Object convert(String fieldName, String value);

    Subquery asSubquery(Condition condition, String... fieldNames);

    SqlFragment asFragment(Condition condition, String... fieldNames);

    SqlFragment asFragment(Condition condition, String[] fieldNames, Order[] orderBy);

    Optional<T> get(Object[] key, boolean eager, String[] exceptions);

    long count(Condition condition);

    boolean hasField(String fieldName);

    Class<?> getType(String fieldName);

    List<String> getQueryFieldNames();

    Instant getEffectiveDate();

    void setEffectiveDate(Instant date);

}
