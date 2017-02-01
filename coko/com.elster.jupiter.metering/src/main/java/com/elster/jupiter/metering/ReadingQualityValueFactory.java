/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.properties.ReadingQualityPropertyValue;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.NoSuchElementException;

public class ReadingQualityValueFactory implements ValueFactory<ReadingQualityPropertyValue> {

    @Override
    public ReadingQualityPropertyValue fromStringValue(String stringValue) {
        return new ReadingQualityPropertyValue(stringValue);
    }

    @Override
    public String toStringValue(ReadingQualityPropertyValue object) {
        return object.getCimCode();
    }

    /**
     * Validate that the CIM code of the given reading quality is valid.
     * It be formatted as x.y.z, where every field is numerical.
     * The system (x) and the category (y) should be known CIM codes, or the wildcard '*'.
     * The index (z) can be any number.
     */
    @Override
    public boolean isValid(ReadingQualityPropertyValue value) {
        String cimCode = value.getCimCode();
        String[] parts = cimCode.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        ReadingQualityType readingQualityType = new ReadingQualityType(cimCode);

        try {
            //System code should be known in CIM, or a wildcard
            if (!isWildCard(parts[0]) && !readingQualityType.system().isPresent()) {
                return false;
            }

            //Category code should be known in CIM
            if (!readingQualityType.category().isPresent()) {
                return false;
            }

            //Index code should be numerical, or a wildcard
            if (!isWildCard(parts[2])) {
                try {
                    int index = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        } catch (NoSuchElementException e) {
            return false;
        }

        return true;
    }

    private boolean isWildCard(String part) {
        return part.equals(ReadingQualityPropertyValue.WILDCARD);
    }

    @Override
    public Class<ReadingQualityPropertyValue> getValueType() {
        return ReadingQualityPropertyValue.class;
    }

    @Override
    public ReadingQualityPropertyValue valueFromDatabase(Object object) {
        return this.fromStringValue(String.valueOf(object));
    }

    @Override
    public Object valueToDatabase(ReadingQualityPropertyValue object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, ReadingQualityPropertyValue value) throws SQLException {
        if (value != null) {
            statement.setObject(offset, valueToDatabase(value));
        } else {
            statement.setNull(offset, Types.VARCHAR);
        }
    }

    @Override
    public void bind(SqlBuilder builder, ReadingQualityPropertyValue value) {
        if (value != null) {
            builder.addObject(valueToDatabase(value));
        } else {
            builder.addNull(Types.VARCHAR);
        }
    }
}