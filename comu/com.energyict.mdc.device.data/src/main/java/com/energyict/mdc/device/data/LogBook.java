package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import java.util.Date;
import java.util.List;

/**
 * Models a Logbook on a Device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/03/14
 * Time: 15:26
 */
public interface LogBook extends BaseLogBook, HasId {

    Device getDevice();

    Date getLastLogBook();

    LogBookSpec getLogBookSpec();

    LogBookType getLogBookType();

    /**
     * Time at which the logbook was was updated in the DB with a new event
     * @return
     */
    Date getLatestEventAdditionDate();

    public List<EndDeviceEventRecord> getEndDeviceEvents(Interval interval);

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
        LogBookUpdater setLastLogBookIfLater(Date lastReading);

        /**
         * Updates the com.energyict.mdc.device.data.LogBook, preferably via his Device
         */
        void update();

        LogBookUpdater setLastReadingIfLater(Date date);
    }
}
