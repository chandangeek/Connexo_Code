package com.energyict.protocolimplv2.common;


import com.elster.jupiter.properties.PropertySpec;

/**
 * Copyrights EnergyICT
 * Date: 12/9/14
 * Time: 12:30 PM
 */
public interface DynamicProperty<T> {

    String getName();

    T getDefaultValue();

    PropertySpec<T> getPropertySpec();
}
