package com.energyict.mdc.channels.serial.modemproperties;


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
    public abstract PEMPModemConfiguration getPEMPModemConfiguration();

}
