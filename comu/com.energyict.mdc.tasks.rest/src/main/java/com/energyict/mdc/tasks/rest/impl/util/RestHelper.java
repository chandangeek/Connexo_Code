/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl.util;

import com.elster.jupiter.time.TimeDuration;

public class RestHelper {

    public TimeDuration getTimeDuration(String units, int count) {
        return TimeDuration.TimeUnit.forDescription(units).during(count);
    }
}