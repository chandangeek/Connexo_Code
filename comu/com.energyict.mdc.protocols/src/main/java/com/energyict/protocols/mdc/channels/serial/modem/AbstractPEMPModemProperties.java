package com.energyict.protocols.mdc.channels.serial.modem;


import com.energyict.dialer.core.impl.PEMPModemConfiguration;

/**
 * @author sva
 * @since 29/04/13 - 14:19
 */
public abstract class AbstractPEMPModemProperties extends AbstractPaknetModemProperties {

    /**
     * Getter for the the PEMP modem configuration to use
     *
     * @return the PEMP modem configuration
     */
    protected abstract PEMPModemConfiguration getPEMPModemConfiguration();

}
