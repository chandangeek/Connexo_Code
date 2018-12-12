/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class Contains extends Leaf {
    private final String fieldName;
    private final ListOperator operator;
    private final Collection<?> collection;

    Contains(String fieldName, ListOperator operator, Collection<?> collection) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.collection = collection;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ListOperator getOperator() {
        return operator;
    }

    public Collection<?> getCollection() {
        return collection;
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.visitContains(this);
    }

    @Override
    public String toString() {
        return collection.isEmpty() ? getSqlString(collection) : decorate(collection.stream()).partitionPer(1000).map(this::getSqlString).collect(Collectors.joining(" OR ", "(", ")"));
    }

    private String getSqlString(Collection<?> collection) {
        return fieldName + " " + operator.getSymbol() + " " + collection;
    }
}
