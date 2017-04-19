package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.properties.rest.PropertyValueConverter;

/**
 * Copyrights EnergyICT
 * Date: 19/04/2017
 * Time: 15:19
 */
public interface MdcPropertyValueConverterFactory {

    PropertyValueConverter getConverterFor(Class clazz);
}
