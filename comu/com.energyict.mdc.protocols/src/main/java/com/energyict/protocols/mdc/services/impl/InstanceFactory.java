package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;

/**
 *
 *
 * @author khe
 * @since 23/01/2017 - 10:48
 */
public interface InstanceFactory {

    Object newInstance() throws UnableToCreateProtocolInstance;

}