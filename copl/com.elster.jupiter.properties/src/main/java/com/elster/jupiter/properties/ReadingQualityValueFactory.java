package com.elster.jupiter.properties;

import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

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