package com.elster.jupiter.metering;

import com.elster.jupiter.properties.ReadingQualityPropertyValue;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.NoSuchElementException;

/**
 * - Parse a given {@link ReadingQualityPropertyValue} to a string value that can be stored to the database
 * - Parse a string value from the database into a {@link ReadingQualityPropertyValue}
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/06/2016 - 17:53
 */
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
     * The system (x) and the category (y) should be known CIM codes.
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
            //System code should be known in CIM
            if (!readingQualityType.system().isPresent()) {
                return false;
            }

            //Category code should be known in CIM
            if (!readingQualityType.category().isPresent()) {
                return false;
            }

            //Index code should be numerical
            try {
                int index = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return false;
            }
        } catch (NoSuchElementException e) {
            return false;
        }

        return true;
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