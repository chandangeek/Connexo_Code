package com.elster.jupiter.util.time;

import java.util.Date;
import java.util.TimeZone;

/**
 */
public interface Clock {

    TimeZone getTimeZone();

    Date now();

}
