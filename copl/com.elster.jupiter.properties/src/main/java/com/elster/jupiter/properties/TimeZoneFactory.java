package com.elster.jupiter.properties;

import java.util.TimeZone;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for TimeZone values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (14:04)
 */
public class TimeZoneFactory extends AbstractValueFactory<TimeZone> {

    @Override
    public Class<TimeZone> getValueType () {
        return TimeZone.class;
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
    public TimeZone valueFromDatabase (Object object) {
        return TimeZone.getTimeZone((String) object);
    }

    @Override
    public Object valueToDatabase (TimeZone timeZone) {
        return timeZone.getID();
    }

    @Override
    public TimeZone fromStringValue (String stringValue) {
        if (stringValue == null) {
            return null;
        }
        else {
            return TimeZone.getTimeZone(stringValue);
        }
    }

    @Override
    public String toStringValue (TimeZone timeZone) {
        return timeZone.getID();
    }

}