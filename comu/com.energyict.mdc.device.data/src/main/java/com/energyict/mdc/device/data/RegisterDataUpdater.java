package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.readings.BaseReading;

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

    RegisterDataUpdater editReading(BaseReading modified, Instant editTimeStamp);

    RegisterDataUpdater confirmReading(BaseReading modified, Instant editTimeStamp);

    RegisterDataUpdater removeReading(Instant timestamp);

    /**
     * Completes the transaction, effectively applying all the changes
     * from previous method calls.
     */
    void complete();

}