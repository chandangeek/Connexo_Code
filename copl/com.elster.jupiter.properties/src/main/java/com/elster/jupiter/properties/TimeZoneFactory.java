/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
        } else {
            List<String> availableTimeZoneIDs = Arrays.asList(TimeZone.getAvailableIDs());
            if (availableTimeZoneIDs.contains(stringValue)) {
                return TimeZone.getTimeZone(stringValue);
            } else {
                return new InvalidTimeZone(stringValue);
            }
        }
    }

    @Override
    public String toStringValue (TimeZone timeZone) {
        return timeZone.getID();
    }

    /**
     * InvalidTimeZone extends from TimeZone<br/>
     * It should be used in cases where the received stringValue
     * could not be converted to a proper TimeZone; Validation should
     * check for this type of TimeZone and throw an error when encountering
     * such instances.
     */
    public class InvalidTimeZone extends TimeZone {

        private String invalidID;

        public InvalidTimeZone(String invalidID) {
            this.invalidID = invalidID;
        }

        @Override
        public String getID() {
            return invalidID;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
            return 0;
        }

        @Override
        public void setRawOffset(int offsetMillis) {
        }

        @Override
        public int getRawOffset() {
            return 0;
        }

        @Override
        public boolean useDaylightTime() {
            return false;
        }

        @Override
        public boolean inDaylightTime(Date date) {
            return false;
        }
    }
}