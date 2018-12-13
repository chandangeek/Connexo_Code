/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.util.Checks.is;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:26)
 */
public class BooleanFactory extends AbstractValueFactory<Boolean> {

    public Class<Boolean> getValueType() {
        return Boolean.class;
    }

    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    @Override
    public Boolean valueFromDatabase(Object object) {
        if (object == null) {
            return Boolean.FALSE;
        }
        else {
            return ((Number) object).intValue() != 0;
        }
    }

    @Override
    public Object valueToDatabase(Boolean object) {
        if (object == null) {
            return 0;
        }
        else {
            return this.valueToDb(object);
        }
    }

    private int valueToDb(Boolean object) {
        if (object) {
            return 1;
        }
        else {
            return 0;
        }
    }

    @Override
    public Boolean fromStringValue(String stringValue) {
        if (!is(stringValue).emptyOrOnlyWhiteSpace()
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