/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides support for multi-valued {@link com.elster.jupiter.properties.PropertySpec}s.
 * @see com.elster.jupiter.properties.PropertySpec#supportsMultiValues()
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-17 (16:34)
 */
public class ListValueFactory<T> implements ValueFactory<List> {

    private final ValueFactory<T> actualFactory;
    private final String separator;

    public ListValueFactory(ValueFactory<T> actualFactory) {
        this(actualFactory, PropertySpecBuilder.DEFAULT_MULTI_VALUE_SEPARATOR);
    }

    public ListValueFactory(ValueFactory<T> actualFactory, String separator) {
        super();
        this.actualFactory = actualFactory;
        this.separator = separator;
    }

    public ValueFactory<T> getActualFactory() {
        return actualFactory;
    }

    public List fromValues(List<Object> values) {
        return values
                .stream()
                .map(Object::toString)
                .map(this.actualFactory::fromStringValue)
                .collect(Collectors.toList());
    }

    @Override
    public List fromStringValue(String stringValue) {
        if (!Checks.is(stringValue).emptyOrOnlyWhiteSpace()) {
            return Stream
                        .of(stringValue.split(this.separator))
                        .map(this.actualFactory::fromStringValue)
                        .collect(Collectors.toList());
        }
        else {
           return Collections.emptyList();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toStringValue(List object) {
        if (object != null) {
            List<T> tees = (List<T>) object;
            return tees
                    .stream()
                    .map(this.actualFactory::toStringValue)
                    .collect(Collectors.joining(this.separator));
        }
        else {
            return null;
        }
    }

    @Override
    public Class<List> getValueType() {
        return List.class;
    }

    @Override
    public List valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(List object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, List value) throws SQLException {
        if (!this.isNull(value)) {
            statement.setObject(offset, valueToDatabase(value));
        }
        else {
            statement.setNull(offset, Types.VARCHAR);
        }
    }

    @Override
    public void bind(SqlBuilder builder, List value) {
        if (!this.isNull(value)) {
            builder.addObject(valueToDatabase(value));
        }
        else {
            builder.addNull(Types.VARCHAR);
        }
    }

    @Override
    public boolean isNull(List value) {
        return ValueFactory.super.isNull(value) || value.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(List value) {
        if (!this.isNull(value)) {
            List<T> tees = (List<T>) value;
            return tees.stream().allMatch(this.actualFactory::isValid);
        }
        else {
            return true;
        }
    }
}