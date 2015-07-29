package com.elster.jupiter.properties;

import com.elster.jupiter.util.Checks;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by dvy on 11/05/2015.
 */
public class LongFactory extends AbstractValueFactory<Long> {
    @Override
    public Class<Long> getValueType () {
        return Long.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "number";
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.NUMERIC;
    }

    @Override
    public Long valueFromDatabase (Object object) {
        return (Long) object;
    }

    @Override
    public Object valueToDatabase (Long object) {
        return object;
    }

    @Override
    public Long fromStringValue (String stringValue) {
        if (Checks.is(stringValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        else {
            return new Long(stringValue);
        }
    }

    @Override
    public String toStringValue (Long object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, Long value) throws SQLException {
        if (value != null) {
            statement.setLong(offset, value);
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }
}
