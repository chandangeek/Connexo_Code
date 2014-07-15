package com.energyict.mdc.device.data;

import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

/**
 * Models a {@link Reading} that relates to an event that occurred in the related {@link Device}.
 * Some events apply to an Interval when e.g. the event that reports the maximum
 * demand value measured during the day. The interval of that event will be the
 * day (starting but not including midnight of the day until and including midnight of the next day)
 * for wich that maximum demand was measured. The timestamp of the event is the timestamp at
 * which the maximum demand occurred.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
public interface EventReading extends NumericalReading {

    /**
     * Returns the Interval to which the event applies.
     *
     * @return The Interval
     */
    public Optional<Interval> getInterval();

}