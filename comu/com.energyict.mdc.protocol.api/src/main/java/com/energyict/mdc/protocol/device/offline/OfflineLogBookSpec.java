package com.energyict.mdc.protocol.device.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Offline;

/**
 * Represents an Offline version of a LogBookSpec
 *
 * @author sva
 * @since 10/12/12 - 13:50
 */
public interface OfflineLogBookSpec extends Offline {

    /**
     * Returns the database ID of the online LogBookSpec
     *
     * @return the ID of the LogBookSpec
     */
    public int getLogBookSpecId();

    /**
     * Returns the database ID of the DeviceConfiguration for the LogBookSpec
     *
     * @return the ID of the DeviceConfiguration
     */
    public int getDeviceConfigId();

    /**
     * Returns the database ID of the LogBookType for the LogBookSpec
     *
     * @return the database ID of the LogBookType
     */
    public int getLogBookTypeId();

    /**
     * Returns the DeviceObisCode, this is the ObisCode as used in the device
     *
     * @return the DeviceObisCode
     */
    public ObisCode getDeviceObisCode();

    /**
     * Returns the ObisCode, this is the ObisCode configured for this LogBookSpec
     *
     * @return the ObisCode
     */
    public ObisCode getObisCode();

}