/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

import java.util.Optional;

/**
 * Adds behavior to the {@link ReadingTypeDeliverable} interface
 * that is reserved for server side components only.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-13 (15:50)
 */
public interface ServerReadingTypeDeliverable extends ReadingTypeDeliverable {
    /**
     * Returns the {@link Event#getCode() event code}
     * that must be provided by a Meter to support this ReadingTypeDeliverable.
     *
     * @return The Set of event codes
     */
    Optional<Long> getRequiredTimeOfUse();

    void prepareDelete();
}