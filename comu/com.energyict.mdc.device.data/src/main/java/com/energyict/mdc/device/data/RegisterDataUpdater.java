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

    public RegisterDataUpdater editReading(BaseReading modified);

    public RegisterDataUpdater confirmReading(BaseReading modified);

    public RegisterDataUpdater removeReading(Instant timestamp);

    /**
     * Completes the transaction, effectively applying all the changes
     * from previous method calls.
     */
    public void complete();

}