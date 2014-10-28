package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 12/03/14
 * Time: 11:16
 */
public interface GatewayReference extends Effectivity {

    /**
     * @return true if for this moment, a communication gateway exists
     * @param existenceDate
     */
    boolean existsFor(Instant existenceDate);

    /**
     * Closes the current interval
     */
    void terminate(Instant closingDate);

}