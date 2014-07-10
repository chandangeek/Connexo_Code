package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.FactoryIds;

/**
 * Exception indicating that the FactoryFinder was not yet registered
 *
 * Copyrights EnergyICT
 * Date: 7/8/14
 * Time: 4:33 PM
 */
public class NoFinderComponentFoundException extends RuntimeException {

    public NoFinderComponentFoundException(FactoryIds factoryId) {
        super("No finder component registered for factory " + factoryId.name());
    }
}
