package com.energyict.protocols.impl;

import com.energyict.mdc.protocol.api.services.UnableToCreateConnectionType;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/01/2017 - 10:48
 */
public interface InstanceFactory {

    Object newInstance() throws UnableToCreateConnectionType;

}