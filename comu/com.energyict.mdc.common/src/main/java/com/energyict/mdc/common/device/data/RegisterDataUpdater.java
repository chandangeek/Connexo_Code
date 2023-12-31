/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.metering.readings.BaseReading;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;

/**
 * Supports updating data in a {@link Register} in a single transaction.
 * The transaction ends when the complete method is called.
 * All method calls return this RegisterDataUpdater to support method chaining.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-21 (09:56)
 */
@ConsumerType
public interface RegisterDataUpdater {

    RegisterDataUpdater editReading(BaseReading modified, Instant editTimeStamp);

    RegisterDataUpdater confirmReading(BaseReading modified, Instant editTimeStamp);

    RegisterDataUpdater removeReading(Instant timestamp);

    /**
     * Completes the transaction, effectively applying all the changes
     * from previous method calls.
     */
    void complete();

}