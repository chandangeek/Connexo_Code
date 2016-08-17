package com.energyict.mdc.dynamic.impl;

/**
 * For validation of values by the {@link com.elster.jupiter.properties.ValueFactory}
 *  @param <T> The type of values that are supported by this factory
 * Copyrights EnergyICT
 * Date: 10/08/2016
 * Time: 14:19
 */
public interface PropertyValidator<T> {

    boolean validate(T value);

}
