package com.energyict.mdc.protocol.api.device;

import com.energyict.obis.ObisCode;

import java.util.List;

/**
 * LoadProfile represents a loadprofile on a data logger or energy meter.
 * Each LoadProfile has a number of channels to store load profile data.
 */
public interface BaseLoadProfile<C extends BaseChannel> {

    /**
     * Get the unique ID of the LoadProfile
     *
     * @return the unique ID of the LoadProfile
     */
    public long getId();

    /**
     * Returns the Overruled ObisCode of the LoadProfileSpec.</br>
     * (this is the ObisCode of the LoadProfile that is know to the Device)
     *
     * @return the obisCode of the LoadProfile which is know to the Device
     */
    public ObisCode getDeviceObisCode();

    /**
     * Returns the Device for the LoadProfile object.
     *
     * @return the Device.
     */
    public BaseDevice getDevice();

    /**
     * Gets the ID of the LoadProfileType
     *
     * @return the ID of the LoadProfileType
     */
    public long getLoadProfileTypeId();

    /**
     * Gets the ObisCode which is configured for the LoadProfileType of this LoadProfile
     *
     * @return the ObisCode which is configured on the LoadProfileType
     */
    public ObisCode getLoadProfileTypeObisCode();

}