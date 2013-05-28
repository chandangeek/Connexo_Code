package com.elster.jupiter.util.time;

import java.util.Date;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 27/05/13
 * Time: 15:40
 */
public interface Clock {

    TimeZone getTimeZone();

    Date now();

}
