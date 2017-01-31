/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:39)
 */
public class TimeDurationValueFactory extends AbstractValueFactory<TimeDuration> {

    public static final String VALUE_UNIT_SEPARATOR = ":";

    @Override
    public Class<TimeDuration> getValueType() {
        return TimeDuration.class;
    }

    @Override
    public int getJdbcType() {
        return Types.VARCHAR;
    }

    @Override
    public TimeDuration valueFromDatabase (Object object) {
        return this.getValueFromObject(object);
    }

    @Override
    public Object valueToDatabase (TimeDuration duration) {
        return getObjectFromValue(duration);
    }

    private TimeDuration getValueFromObject(final Object object) {
        if (object == null) {
            return null;
        }
        else {
            String value = (String) object;
            String[] valueAndUnit = value.split(VALUE_UNIT_SEPARATOR);
            int timeUnits = Integer.parseInt(valueAndUnit[0]);
            int unit = TimeDuration.TimeUnit.SECONDS.getCode();
            if(valueAndUnit.length>1){
                unit = Integer.parseInt(valueAndUnit[1]);
            }
            return new TimeDuration(timeUnits, unit);
        }
    }

    private String getObjectFromValue(final TimeDuration duration) {
        if (duration == null) {
            return null;
        }
        else {
            return duration.getCount() + VALUE_UNIT_SEPARATOR + duration.getTimeUnitCode();
        }
    }

    @Override
    public TimeDuration fromStringValue(String stringValue) {
        return getValueFromObject(stringValue);
    }

    @Override
    public String toStringValue(TimeDuration object) {
        return getObjectFromValue(object);
    }


    @Override
    public void bind(SqlBuilder builder, TimeDuration value) {
        if (value != null) {
            builder.addObject(valueToDatabase(value));
        }
        else {
            builder.addNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, TimeDuration value) throws SQLException {

        if (value != null) {
            statement.setObject(offset, valueToDatabase(value));
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }
}