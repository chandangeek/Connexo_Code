package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.protocol.api.device.BaseLogBook;

import java.util.Date;

/**
 * Models a Logbook on a Device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/03/14
 * Time: 15:26
 */
public interface LogBook extends BaseLogBook {

    Device getDevice();

    Date getLastLogBook();

    LogBookSpec getLogBookSpec();

    LogBookType getLogBookType();

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
    }
}
