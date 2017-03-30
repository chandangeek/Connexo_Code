/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.orm.Table;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for String values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (16:47)
 */
public class StringFactory extends AbstractValueFactory<String> {

    public static final int MAX_SIZE = Table.MAX_STRING_LENGTH;

    @Override
    public Class<String> getValueType () {
        return String.class;
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public boolean isNull(String value) {
        return super.isNull(value) || value.isEmpty();
    }

    @Override
    public boolean isValid(String value) {
        return value.length() <= StringFactory.MAX_SIZE;
    }

    @Override
    public String valueFromDatabase (Object object) {
        return (String) object;
    }

    @Override
    public Object valueToDatabase (String object) {
        return object;
    }

    @Override
    public String fromStringValue (String stringValue) {
        if (stringValue == null) {
            return "";
        }
        else {
            return stringValue;
        }
    }

    @Override
    public String toStringValue (String object) {
        return object;
    }

    @Override
    public void bind(PreparedStatement statement, int offset, String value) throws SQLException {
        if (value != null) {
            statement.setString(offset, value);
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

}