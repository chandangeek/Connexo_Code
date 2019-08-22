/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.metering.readings.BaseReading;

import aQute.bnd.annotation.ConsumerType;

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
@ConsumerType
public interface ChannelDataUpdater {

    ChannelDataUpdater removeChannelData(List<Instant> intervals);

    ChannelDataUpdater editChannelData(List<BaseReading> modifiedChannelData);

    ChannelDataUpdater editBulkChannelData(List<BaseReading> modifiedChannelData);

    ChannelDataUpdater confirmChannelData(List<BaseReading> modifiedChannelData);

    ChannelDataUpdater estimateChannelData(List<BaseReading> modifiedChannelData);

    ChannelDataUpdater estimateBulkChannelData(List<BaseReading> modifiedChannelData);

    /**
     * Completes the transaction, effectively applying all the changes
     * from previous method calls.
     */
    void complete();

}