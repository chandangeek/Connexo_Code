/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to edit/remove/estimate/confirm
 * a {@link com.elster.jupiter.metering.readings.BaseReading}
 * on a {@link UsagePoint} for a {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}
 * with active time of use when the UsagePoint's
 * {@link com.elster.jupiter.calendar.Calendar}
 * produces a different code at the timestamp of the reading.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-24 (11:53)
 */
public class IncompatibleTimeOfUseException extends LocalizedException {
    public IncompatibleTimeOfUseException(Thesaurus thesaurus, Event expectedEvent, Event actualEvent) {
        super(thesaurus, MessageSeeds.INCOMPATIBLE_TIME_OF_USE_FOR_EDITING, expectedEvent.getName(), actualEvent.getName());
    }
}