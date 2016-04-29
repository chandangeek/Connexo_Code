package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;

/**
 * models the link between a slave device (origin) is linked to a master device (gateway)
 * Copyrights EnergyICT
 * Date: 12/03/14
 * Time: 11:16
 */
public interface PhysicalGatewayReference extends Effectivity {

    enum PhysicalGatewayReferenceDiscriminator {
        /**
        /* default slave to master (gateway) reference
         */
        DEFAULT,
        /**
         * data logger slave to data logger reference
         */
        DATA_LOGGER_REFERENCE
    }

    /**
     * Gets the origin of the reference, i.e. the {@link Device} that effectively references the gateway.
     *
     * @return The Device that references the gateway
     */
    Device getOrigin();

    /**
     * Gets the {@link Device gateway} that is referenced through this PhysicalGatewayReference.
     *
     * @return The gateway device
     */
    Device getGateway();

    /**
     * @param existenceDate The instant in time
     * @return true if for this moment, a communication gateway exists
     */
    boolean existsFor(Instant existenceDate);

    /**
     * Closes the current interval.
     */
    void terminate(Instant closingDate);

}