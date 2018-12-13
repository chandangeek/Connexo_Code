/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.HexString;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:01)
 */
public class HexStringFactory extends AbstractValueFactory<com.energyict.mdc.upl.properties.HexString> {

    public static final int DEFAULT_MAX_SIZE = Table.MAX_STRING_LENGTH;

    private final LengthConstraint lengthConstraint;

    public static HexStringFactory forDefaultMaximumLength() {
        return new HexStringFactory(new MaximumLength(DEFAULT_MAX_SIZE));
    }

    public static HexStringFactory forExactLength(int length) {
        return new HexStringFactory(new ExactLength(length));
    }

    private HexStringFactory(LengthConstraint lengthConstraint) {
        super();
        this.lengthConstraint = lengthConstraint;
    }

    @Override
    public Class<com.energyict.mdc.upl.properties.HexString> getValueType () {
        return com.energyict.mdc.upl.properties.HexString.class;
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public boolean isNull(com.energyict.mdc.upl.properties.HexString value) {
        return super.isNull(value) || value.getContent() == null || value.getContent().isEmpty();
    }

    @Override
    public HexString valueFromDatabase (Object object) {
        if (object == null) {
            return null;
        }
        else {
            return new HexString((String) object);
        }
    }

    @Override
    public Object valueToDatabase (com.energyict.mdc.upl.properties.HexString object) {
        if (object == null) {
            return null;
        }
        else {
            return this.toStringValue(object);
        }
    }

    @Override
    public HexString fromStringValue (String stringValue) {
        if (stringValue == null) {
            return null;
        }
        else {
            return new HexString(stringValue);
        }
    }

    @Override
    public String toStringValue (com.energyict.mdc.upl.properties.HexString object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }

    @Override
    public void bind(SqlBuilder builder, com.energyict.mdc.upl.properties.HexString value) {
        if (value != null) {
            builder.addObject(this.toStringValue(value));
        }
        else {
            builder.addNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, com.energyict.mdc.upl.properties.HexString value) throws SQLException {
        if (value != null) {
            statement.setString(offset, this.toStringValue(value));
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

    @Override
    public boolean isValid(com.energyict.mdc.upl.properties.HexString value) {
        return this.lengthConstraint.isValid(value);
    }

    private interface LengthConstraint {
        boolean isValid(com.energyict.mdc.upl.properties.HexString value);
    }

    private static class MaximumLength implements LengthConstraint {
        private final int maxSize;

        private MaximumLength(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public boolean isValid(com.energyict.mdc.upl.properties.HexString value) {
            return value.length() <= this.maxSize;
        }
    }

    private static class ExactLength implements LengthConstraint {
        private final int length;

        private ExactLength(int length) {
            this.length = length;
        }

        @Override
        public boolean isValid(com.energyict.mdc.upl.properties.HexString value) {
            return value.length() == this.length;
        }
    }

}