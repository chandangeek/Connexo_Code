package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.util.Checks;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link com.elster.jupiter.properties.ValueFactory} interface for integer values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (11:31)
 */
public class IntegerFactory extends AbstractValueFactory<Integer> {
    @Override
    public Class<Integer> getValueType () {
        return Integer.class;
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.NUMERIC;
    }

    @Override
    public Integer valueFromDatabase (Object object) {
        return (Integer) object;
    }

    @Override
    public Object valueToDatabase (Integer object) {
        return object;
    }

    @Override
    public Integer fromStringValue (String stringValue) {
        if (Checks.is(stringValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        else {
            return new Integer(stringValue);
        }
    }

    @Override
    public String toStringValue (Integer object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, Integer value) throws SQLException {
        if (value != null) {
            statement.setInt(offset, value);
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

}