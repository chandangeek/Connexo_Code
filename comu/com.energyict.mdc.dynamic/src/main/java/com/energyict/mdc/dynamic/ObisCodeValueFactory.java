/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.ObisCode;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:43)
 */
public class ObisCodeValueFactory extends AbstractValueFactory<ObisCode> {

    @Override
    public Class<ObisCode> getValueType () {
        return ObisCode.class;
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public ObisCode valueFromDatabase (Object object) {
        if (object == null) {
            return null;
        }
        else {
            String obisCodeValue = (String) object;
            return ObisCode.fromString(obisCodeValue);
        }
    }

    @Override
    public Object valueToDatabase (ObisCode object) {
        if (object == null) {
            return null;
        }
        else {
            return this.toStringValue(object);
        }
    }

    @Override
    public ObisCode fromStringValue (String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        }
        else {
            return ObisCode.fromString(stringValue);
        }
    }

    @Override
    public String toStringValue (ObisCode object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }

    @Override
    public void bind(SqlBuilder builder, ObisCode value) {
        if (value != null) {
            builder.addObject(this.toStringValue(value));
        }
        else {
            builder.addNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, ObisCode value) throws SQLException {
        if (value != null) {
            statement.setString(offset, this.toStringValue(value));
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

}