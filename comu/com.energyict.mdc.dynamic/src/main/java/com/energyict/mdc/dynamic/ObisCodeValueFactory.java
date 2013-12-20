package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.ObisCode;

import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:43)
 */
public class ObisCodeValueFactory extends AbstractValueFactory<ObisCode> {

    @Override
    public String getDatabaseTypeName () {
        return "varchar2(23)";
    }

    @Override
    public Class<ObisCode> getValueType () {
        return ObisCode.class;
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public ObisCode valueFromDatabase (Object object) throws SQLException {
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
            return object.toString();
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

}