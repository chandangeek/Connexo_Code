/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.TimeZonePropertySpec;

import java.util.TimeZone;


public class TimeZonePropertySpecImpl extends BasicPropertySpec implements TimeZonePropertySpec {

    public TimeZonePropertySpecImpl() {
        super(new TimeZoneFactory());
    }

    @Override
    public boolean validateValue(Object objectValue) throws InvalidValueException {
        if (objectValue instanceof TimeZone) {
            TimeZone value = (TimeZone) objectValue;
            boolean valid = super.validateValue(value);
            if (value instanceof TimeZoneFactory.InvalidTimeZone) {
                throw new InvalidValueException("TimeZoneIsNotValid", "Invalid time zone", this.getName(), value.getID());
            }
            return valid;
        } else {
            throw new InvalidValueException("TimeZoneIsNotValid", "Invalid time zone", this.getName(), objectValue);
        }
    }
}
