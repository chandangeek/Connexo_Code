package com.energyict.mdc.channel.serial.modemproperties;

/**
 * @author sva
 * @since 23/11/12 (8:53)
 */
public abstract class AbstractAtModemProperties extends AbstractModemProperties {

    /**
     * Getter for the address selector to use
     *
     * @return the address selector to use after a physical connect
     */
    public abstract String getAddressSelector();

    /**
     * Getter for the list of post dial command(s) to use
     *
     * @return the post dial command(s) to execute after a physical connect
     */
    public abstract String getPostDialCommands();

}