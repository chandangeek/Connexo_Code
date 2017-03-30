/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.ApplicationException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:34)
 */
public class DateAndTimeFactory extends AbstractValueFactory<Date> {

    private static final Logger LOGGER = Logger.getLogger(DateAndTimeFactory.class.getName());

    @Override
    public Class<Date> getValueType() {
        return Date.class;
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    @Override
    public Date valueFromDatabase (Object object) {
        return this.valueFromDatabase((Number) object);
    }

    private Date valueFromDatabase (Number number) {
        if (number != null) {
            return new Date(number.longValue());
        }
        else {
            return null;
        }
    }

    @Override
    public Object valueToDatabase (Date object) {
        if (object != null) {
            return object.getTime();
        }
        else {
            return null;
        }
    }

    @Override
    public Date fromStringValue(String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        }
        else {
            try {
                return new Date(new Long(stringValue));
            }
            catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new ApplicationException(e);
            }
        }
    }

    @Override
    public String toStringValue(Date object) {
        if (object != null) {
            return valueToDatabase(object).toString();
        }
        else {
            return "";
        }
    }

    @Override
    public void bind(SqlBuilder builder, Date value) {
        if (value != null) {
            builder.addDate(value);
        }
        else {
            builder.addNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, Date value) throws SQLException {
        if (value != null) {
            statement.setDate(offset, new java.sql.Date(value.getTime()));
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

}