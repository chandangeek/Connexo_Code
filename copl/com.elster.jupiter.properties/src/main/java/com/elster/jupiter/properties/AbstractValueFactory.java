/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides code reuse opportunities for components that implement the {@link ValueFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (16:07)
 */
public abstract class AbstractValueFactory<T> implements ValueFactory<T> {

    protected abstract int getJdbcType();

    @Override
    public void bind(SqlBuilder builder, T value) {
        if (value != null) {
            builder.addObject(valueToDatabase(value));
        } else {
            builder.addNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, T value) throws SQLException {
        if (value != null) {
            statement.setObject(offset, valueToDatabase(value));
        } else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

}