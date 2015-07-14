package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Supports updating data in a {@link Channel} in a single transaction.
 * The transaction ends when the complete method is called.
 * All method calls return this ChannelDataUpdater to support method chaining.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-21 (09:07)
 */
@ProviderType
public interface ChannelDataUpdater {

    public ChannelDataUpdater removeChannelData(Range<Instant> interval);

    public ChannelDataUpdater removeChannelData(List<Range<Instant>> intervals);

    public ChannelDataUpdater editChannelData(List<BaseReading> modifiedChannelData);

    public ChannelDataUpdater editBulkChannelData(List<BaseReading> modifiedChannelData);

    public ChannelDataUpdater confirmChannelData(List<BaseReading> modifiedChannelData);

    /**
     * Completes the transaction, effectively applying all the changes
     * from previous method calls.
     */
    public void complete();

}