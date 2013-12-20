package com.energyict.mdc.dynamic;

import java.sql.SQLException;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for String values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (16:47)
 */
public class StringFactory extends AbstractValueFactory<String> {

    @Override
    public Class<String> getValueType () {
        return String.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "varchar2(4000)";
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public String valueFromDatabase (Object object) throws SQLException {
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

}