/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (14:12)
 */
public abstract class AbstractValueFactory<T> implements ValueFactory<T> {
    private final Class<T> valueType;

    public AbstractValueFactory(Class<T> valueType) {
        super();
        this.valueType = valueType;
    }

    @Override
    public Class<T> getValueType() {
        return this.valueType;
    }

    @Override
    public T valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(T object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, T value) throws SQLException {
        if (value != null) {
            statement.setObject(offset, valueToDatabase(value));
        }
        else {
            statement.setNull(offset, Types.VARCHAR);
        }
    }

    @Override
    public void bind(SqlBuilder builder, T value) {
        if (value != null) {
            builder.addObject(valueToDatabase(value));
        }
        else {
            builder.addNull(Types.VARCHAR);
        }
    }

}