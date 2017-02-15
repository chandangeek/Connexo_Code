package com.energyict.mdc.channel.serial.modemproperties;

/**
 * @author sva
 * @since 30/04/13 - 13:22
 */
public abstract class AbstractCaseModemProperties extends AbstractModemProperties{

    /**
     * Getter for the address selector to use
     *
     * @return the address selector to use after a physical connect
     */
    public abstract String getAddressSelector();

}