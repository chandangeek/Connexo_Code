/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import com.elster.jupiter.orm.associations.Effectivity;
import com.energyict.mdc.common.device.data.Device;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Represent Links between (slave) device/gateway
 *                 between data logger slave/ data logger
 *                 between multi-element submeter/ multi-element meter
 * Each link is valid for a given period (=Effectivity)
 */
@ProviderType
public interface PhysicalGatewayReference extends Effectivity {

    enum PhysicalGatewayReferenceDiscriminator {
        /**
        /* default slave to master (gateway) reference
         */
        DEFAULT,
        /**
         * data logger slave to data logger reference
         */
        DATA_LOGGER_REFERENCE,
        /**
         * Multi-element submeter to multi-element meter reference
         */
        MULTI_ELEMENT_REFERENCE
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

    /**
     * Synonym for 'is not effective at this moment'
     * @return true if the 'effectivity' is ended, false if not
     */
    boolean isTerminated();

    long getId();
}
