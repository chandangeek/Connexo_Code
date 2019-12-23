/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;

public class TimeUtils {

    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    private TimeUtils() {
    }

    public static Instant convertFromTimeZone(Instant date, String timeZone) {
        ZoneId zoneId = UTC_ZONE_ID;
        try {
            if (timeZone != null) {
                zoneId = ZoneId.of(timeZone);
            }
        } catch (DateTimeException e) {
            // No action, just use UTC zone
        }
        return date.atZone(zoneId).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
    }

    public static Instant convertToUTC(Instant date) {
        return date.atZone(ZoneId.systemDefault()).withZoneSameLocal(UTC_ZONE_ID).toInstant();
    }
}
