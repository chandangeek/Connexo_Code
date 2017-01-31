/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

/**
 * Models a {@link NumericalReading} that relates billing and is relevant in the context of the billing period.
 * Example: some energy contracts impose constraints on the maximum demand requested by the contractee.
 * The energy supplier will therefore want to know the maximum demand that was requested during the billing period.
 * The interval of the reading will be the billing period.
 * The timestamp of the reading is the timestamp at which the maximum demand occurred.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface BillingReading extends NumericalReading {

    /**
     * Returns the Interval to which the event applies.
     *
     * @return The Interval
     */
    Optional<Range<Instant>> getRange();

}