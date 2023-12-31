/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for {@link Boolean} values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:23)
 */
public class ThreeStateFactory extends AbstractValueFactory<Boolean> {

    public Class<Boolean> getValueType() {
        return Boolean.class;
    }

    public int getJdbcType() {
        return Types.INTEGER;
    }

    @Override
    public Boolean valueFromDatabase(Object object) {
        if (object == null) {
            return null;
        }
        return ((Number) object).intValue() != 0;
    }

    @Override
    public Object valueToDatabase(Boolean object) {
        if (object == null) {
            return null;
        }
        else if (object) {
            return 1;
        }
        else {
            return 0;
        }
    }

    @Override
    public Boolean fromStringValue(String stringValue) {
        if (   !is(stringValue).emptyOrOnlyWhiteSpace()
            && "1".equals(stringValue.trim())) {
            return Boolean.TRUE;
        }
        else {
            return Boolean.FALSE;
        }
    }

    @Override
    public String toStringValue(Boolean object) {
        if (Boolean.TRUE.equals(object)) {
            return "1";
        }
        else {
            return "0";
        }
    }

    @Override
    public void bind(SqlBuilder builder, Boolean value) {
        if (value != null) {
            builder.addInt(value ? 1 : 0);
        }
        else {
            builder.addNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, Boolean value) throws SQLException {
        if (value == null) {
            statement.setNull(offset, this.getJdbcType());
        }
        else if (value) {
            statement.setInt(offset, 1);
        }
        else {
            statement.setInt(offset, 0);
        }
    }
}
