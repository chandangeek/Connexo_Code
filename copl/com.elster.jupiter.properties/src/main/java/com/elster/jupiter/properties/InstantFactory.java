/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.Checks;

import java.time.Instant;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * that supports {@link Instant}s and persists them as the number
 * of millis since Jan 1st, 1970.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-24 (15:44)
 */
public class InstantFactory extends AbstractValueFactory<Instant> {

    @Override
    public Class<Instant> getValueType() {
        return Instant.class;
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    @Override
    public Instant valueFromDatabase (Object object) {
        return this.valueFromDatabase((Number) object);
    }

    private Instant valueFromDatabase (Number number) {
        if (number != null) {
            return Instant.ofEpochMilli(number.longValue());
        }
        else {
            return null;
        }
    }

    @Override
    public Object valueToDatabase(Instant instant) {
        return instant.toEpochMilli();
    }

    @Override
    public Instant fromStringValue(String stringValue) {
        if (Checks.is(stringValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        else {
            return this.valueFromDatabase(new Long(stringValue));
        }
    }

    @Override
    public String toStringValue(Instant object) {
        if (object != null) {
            return this.valueToDatabase(object).toString();
        }
        else {
            return "";
        }
    }
}