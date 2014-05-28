package com.energyict.mdc.protocol.api.device.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Offline;

import java.util.Date;

/**
 * Represents an Offline version of a LogBook
 *
 * @author sva
 * @since 07/12/12 - 14:30
 */
public interface OfflineLogBook extends Offline {

    /**
     * Returns the database ID of this {@link OfflineDevice devices'} LogBook
     *
     * @return the ID of the LogBook
     */
    public long getLogBookId();

    /**
     * Returns the Id of the Device which owns this LogBookType.
     *
     * @return the {@link OfflineDevice}
     */
    int getDeviceId();

    /**
     * Returns the SerialNumber of the Master Device
     *
     * @return the SerialNumber of the Master Device
     */
    public String getMasterSerialNumber();

    /**
     * Returns the Date from where to start fetching data from the LogBook
     *
     * @return the {@link OfflineDevice}
     */
    Date getLastLogBook();

    /**
     * Returns the database ID of the LoadProfileType of this LoadProfile
     *
     * @return the ID of the LoadProfileType
     */
    public long getLogBookTypeId();

    /**
     * Returns the ObisCode for the LogBookSpec.
     *
     * @return the ObisCode
     */
    public ObisCode getObisCode();

}
