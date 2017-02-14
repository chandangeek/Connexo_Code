package com.energyict.mdc.protocol.api.services;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cps.CustomPropertySet;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 13/02/2017 - 11:17
 */
@ProviderType
public interface CustomPropertySetInstantiatorService {

    /**
     * Instantiate a given java class name that represents a CPS support class in the mdc.protocols bundle
     */
    CustomPropertySet createCustomPropertySet(String javaClassName) throws ClassNotFoundException;

    /**
     * Instantiate any given java class name that represents a class in the mdc.protocols bundle
     */
    Class forName(String javaClassName) throws ClassNotFoundException;

}