/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.sql.Types;

public class NoneOrTimeDurationValueFactory extends AbstractValueFactory<NoneOrTimeDurationValue> {

    private static final String NONE = "none";

    private final TimeDurationValueFactory timeDurationValueFactory = new TimeDurationValueFactory();

    @Override
    public NoneOrTimeDurationValue fromStringValue(String stringValue) {
        if (NONE.equals(stringValue)) {
            return NoneOrTimeDurationValue.none();
        }
        return NoneOrTimeDurationValue.of(this.timeDurationValueFactory.fromStringValue(stringValue));
    }

    @Override
    public String toStringValue(NoneOrTimeDurationValue object) {
        if (object.isNone()) {
            return NONE;
        }
        return this.timeDurationValueFactory.toStringValue(object.getValue());
    }

    @Override
    public Class<NoneOrTimeDurationValue> getValueType() {
        return NoneOrTimeDurationValue.class;
    }

    @Override
    public NoneOrTimeDurationValue valueFromDatabase(Object object) {
        return fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(NoneOrTimeDurationValue object) {
        return toStringValue(object);
    }

    @Override
    public int getJdbcType() {
        return Types.VARCHAR;
    }
}
