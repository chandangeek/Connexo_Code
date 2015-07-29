package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.google.common.collect.Range;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.device.BaseLogBook;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Models a Logbook on a Device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/03/14
 * Time: 15:26
 */
@ProviderType
public interface LogBook extends BaseLogBook, HasId {

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
     * Defines an <i>update</i> component to update a {@link com.energyict.mdc.device.data.LogBook} implementation
     */
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
    }

}