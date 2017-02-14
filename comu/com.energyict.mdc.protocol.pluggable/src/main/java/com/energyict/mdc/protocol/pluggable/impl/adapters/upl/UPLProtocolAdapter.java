package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 13/02/2017 - 14:42
 */
public interface UPLProtocolAdapter {

    /**
     * Return the class of the protocol that is adapted
     */
    Class getActualClass();

}