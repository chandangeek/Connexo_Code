package com.energyict.mdc.dynamic;

import java.sql.SQLException;

import static com.elster.jupiter.util.Checks.is;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:26)
 */
public class BooleanFactory extends AbstractValueFactory<Boolean> {

    public Class<Boolean> getValueType() {
        return Boolean.class;
    }

    @Override
    public String getDatabaseTypeName() {
        return "number(1)";
    }

    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    @Override
    public Boolean valueFromDatabase(Object object) throws SQLException {
        if (object == null) {
            return Boolean.FALSE;
        }
        else {
            return ((Number) object).intValue() != 0;
        }
    }

    @Override
    public Object valueToDatabase(Boolean object) {
        if (object == null) {
            return 0;
        }
        else if (object) {
            return 1;
        }
        else {
            return 0;
        }
    }

    @Override
    public Boolean fromStringValue(String stringValue) {
        if (!is(stringValue).emptyOrOnlyWhiteSpace()
                && "1".equals(stringValue.trim())) {
            return Boolean.TRUE;
        }
        else {
            return Boolean.FALSE;
        }
    }

    @Override
    public String toStringValue(Boolean object) {
        if (Boolean.TRUE.equals(object)) {
            return "1";
        }
        else {
            return "0";
        }
    }

}