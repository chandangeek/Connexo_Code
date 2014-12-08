package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 12/03/14
 * Time: 11:16
 */
public interface PhysicalGatewayReference extends Effectivity {

    /**
     * Gets the origin of the reference, i.e. the {@link Device} that effectively references the gateway.
     *
     * @return The Device that references the gateway
     */
    public Device getOrigin();

    /**
     * Gets the {@link Device gateway} that is referenced through this PhysicalGatewayReference.
     *
     * @return The gateway device
     */
    public Device getGateway();

    /**
     * @param existenceDate The instant in time
     * @return true if for this moment, a communication gateway exists
     */
    public boolean existsFor(Instant existenceDate);

    /**
     * Closes the current interval.
     */
    public void terminate(Instant closingDate);

}