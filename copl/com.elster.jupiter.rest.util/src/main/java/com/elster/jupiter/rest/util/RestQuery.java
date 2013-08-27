package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.conditions.Condition;

import java.util.List;

public interface RestQuery<T> {
    List<T> select(QueryParameters queryParameters);

    List<T> select(QueryParameters queryParameters, Condition condition);
}
