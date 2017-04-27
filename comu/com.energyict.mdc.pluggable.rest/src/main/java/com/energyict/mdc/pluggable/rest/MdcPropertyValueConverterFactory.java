package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.properties.rest.PropertyValueConverter;

import aQute.bnd.annotation.ProviderType;

/**
 * Copyrights EnergyICT
 * Date: 19/04/2017
 * Time: 15:19
 */
@ProviderType
public interface MdcPropertyValueConverterFactory {
    PropertyValueConverter getConverterFor(Class clazz);
}