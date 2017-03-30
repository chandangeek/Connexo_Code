/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.energyict.mdc.common.TimeOfDay;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:32)
 */
public class TimeOfDayFactory extends AbstractValueFactory<TimeOfDay> {

    @Override
    public Class<TimeOfDay> getValueType() {
        return TimeOfDay.class;
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    @Override
    public TimeOfDay valueFromDatabase (Object object) {
        if (object != null) {
            return new TimeOfDay(((Number) object).intValue());
        }
        else {
            return null;
        }
    }

    @Override
    public Object valueToDatabase (TimeOfDay object) {
        if (object != null) {
            return object.getSeconds();
        }
        else {
            return null;
        }
    }

    @Override
    public TimeOfDay fromStringValue(String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return new TimeOfDay(0);
        }
        else {
            return new TimeOfDay(Integer.parseInt(stringValue));
        }
    }

    @Override
    public String toStringValue(TimeOfDay object) {
        if (object == null) {
            return "";
        }
        else {
            return Integer.toString(object.getSeconds());
        }
    }

}