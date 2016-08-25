package com.elster.jupiter.properties;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Marker interface for value factories using seperate for validating (isValid(T)) the value(s)
 *  @param <T> The type of values that are validated
 * Copyrights EnergyICT
 * Date: 16/08/2016
 * Time: 15:12
 */
public interface HasPropertyValidator<T> {
    MessageSeed invalidMessage();
    Object getReferenceValue();
}

