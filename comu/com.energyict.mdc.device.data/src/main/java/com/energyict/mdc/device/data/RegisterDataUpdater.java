/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.readings.BaseReading;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Supports updating data in a {@link Register} in a single transaction.
 * The transaction ends when the complete method is called.
 * All method calls return this RegisterDataUpdater to support method chaining.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-21 (09:56)
 */
@ProviderType
public interface RegisterDataUpdater {

    RegisterDataUpdater editReading(BaseReading modified);

    RegisterDataUpdater confirmReading(BaseReading modified);

    RegisterDataUpdater removeReading(Instant timestamp);

    /**
     * Completes the transaction, effectively applying all the changes
     * from previous method calls.
     */
    void complete();

}