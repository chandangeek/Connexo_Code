/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.device.config.LogBookSpec;
import com.energyict.mdc.common.masterdata.LogBookType;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ConsumerType
public interface LogBook extends com.energyict.mdc.upl.meterdata.LogBook, HasId {

    Device getDevice();

    Optional<Instant> getLastLogBook();

    LogBookSpec getLogBookSpec();

    LogBookType getLogBookType();

    /**
     * Time at which the logbook was was updated in the DB with a new event
     * @return
     */
    Optional<Instant> getLatestEventAdditionDate();

    List<EndDeviceEventRecord> getEndDeviceEvents(Range<Instant> interval);

    List<EndDeviceEventRecord> getEndDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter);

    /**
     * Defines an <i>update</i> component to update a {@link LogBook} implementation
     */
    @ConsumerType
    interface LogBookUpdater {

        /**
         * Updates the last reading if the argument is later than
         * the current last reading.
         *
         * @param lastReading the new last reading.
         */
        LogBookUpdater setLastLogBookIfLater(Instant lastReading);

        /**
         * Updates the com.energyict.mdc.device.data.LogBook, preferably via his Device
         */
        void update();

        LogBookUpdater setLastReadingIfLater(Instant date);

        LogBookUpdater setLastReading(Instant date);
    }

}