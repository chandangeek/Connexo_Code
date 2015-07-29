package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.orm.associations.Effectivity;

/**
 * Models neighbor information that is maintained by a {@link Device}
 * that uses power line carrier technology to communicate.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (13:53)
 */
@ProviderType
public interface PLCNeighbor extends Effectivity {

    public ModulationScheme getModulationScheme();

    public Modulation getModulation();

    /**
     * Gets the {@link Device} for which this
     * PLCNeighbor was created.
     *
     * @return The Device
     */
    public Device getDevice();

    /**
     * Gets the neighboring Device.
     *
     * @return The neighboring Device
     */
    public Device getNeighbor();

}