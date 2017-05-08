/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.time.TimeDuration;

public class NoneOrTimeDurationValue {

    private static final NoneOrTimeDurationValue NONE = new NoneOrTimeDurationValue(true, null);

    private boolean isNone;
    private TimeDuration value;

    private NoneOrTimeDurationValue(boolean isNone, TimeDuration value) {
        this.isNone = isNone;
        this.value = value;
    }

    public static NoneOrTimeDurationValue none() {
        return NONE;
    }

    public static NoneOrTimeDurationValue of(TimeDuration timeDuration) {
        return new NoneOrTimeDurationValue(false, timeDuration);
    }

    public boolean isNone() {
        return isNone;
    }

    public TimeDuration getValue() {
        return value;
    }
}
