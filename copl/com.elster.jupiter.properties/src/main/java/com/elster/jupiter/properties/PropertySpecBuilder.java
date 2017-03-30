/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.nls.TranslationKey;

import java.util.List;

/**
 * Provides building services for {@link PropertySpec}s.
 * All methods that contribute to aspects of the PropertySpec
 * under construction will return the same PropertySpecBuilder
 * to support method chaning.<br>
 * Two aspects (name and description) of a PropertySpec are
 * designed to support translation to all the Connexo languages.
 * The preferred way is to use the API with {@link TranslationKey}s but
 * an alternative API that uses simple Strings is available too.
 * Note however that switching between the two alternatives
 * is not supported. In other words, you need to decide
 * if you want to build a PropertySpec whose name and description
 * are backed by the {@link com.elster.jupiter.nls.NlsService}
 * or if you want to hard code the name and description.
 * Mixing the two APIs will result in an IllegalStateException.
 * <br>
 * Finally, the client code will call the finish method when all
 * aspects of the PropertySpec have been specified.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (13:13)
 */
public interface PropertySpecBuilder<T> {

    String DEFAULT_MULTI_VALUE_SEPARATOR = "::";

    /**
     * Sets a default value for the {@link PropertySpec} under construction.
     * Setting a default value implies that the default value
     * will become one of the possible values of the PropertySpec.
     * Note that there is only one default and calling this method
     * a second time will overrule the previous default.
     * The previous value will remain in the list of possible values though.
     *
     * @param defaultValue The default value
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    PropertySpecBuilder<T> setDefaultValue (T defaultValue);

    /**
     * Marks the list of possible values of the {@link PropertySpec}
     * under construction as an exhaustive list.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    PropertySpecBuilder<T> markExhaustive ();

    /**
     * Marks the list of possible values of the {@link PropertySpec}
     * as editable.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    PropertySpecBuilder<T> markEditable();

    /**
     * Marks the list of possible values of the {@link PropertySpec}
     * under construction as an exhaustive list and uses
     * the specified {@link PropertySelectionMode} as a hint
     * for the UI how to render the list of possible values.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    PropertySpecBuilder<T> markExhaustive (PropertySelectionMode selectionMode);

    /**
     * Marks the {@link PropertySpec} that is under construction as supporting multi values,
     * using the default separator
     * The resulting PropertySpec will therefore return <code>true</code>
     * for the {@link PropertySpec#supportsMultiValues()} method.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    default PropertySpecBuilder<T> markMultiValued() {
        return markMultiValued(DEFAULT_MULTI_VALUE_SEPARATOR);
    }

    /**
     * Marks the {@link PropertySpec} that is under construction as supporting multi values
     * and uses the specified separator to separate the results of calling
     * {@link ValueFactory#toStringValue(Object)}.
     * The resulting PropertySpec will therefore return <code>true</code>
     * for the {@link PropertySpec#supportsMultiValues()} method.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    PropertySpecBuilder<T> markMultiValued(String separator);

    /**
     * Marks the {@link PropertySpec} that is under construction as required.
     * The resulting PropertySpec will therefore return <code>true</code>
     * for the {@link PropertySpec#isRequired()} method.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    PropertySpecBuilder<T> markRequired ();

    /**
     * Adds the specified values to the PropertySpec under construction.
     *
     * @param values The possible values
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    PropertySpecBuilder<T> addValues (T... values);

    /**
     * Adds the specified values to the PropertySpec under construction.
     *
     * @param values The possible values
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    PropertySpecBuilder<T> addValues (List<T> values);

    /**
     * Finishes the building process and returns the
     * {@link PropertySpec} as it was constructed so far.
     * Note that this stops the building process and
     * attempts to reuse this builder will fail
     * with an {@link IllegalStateException} being thrown.
     *
     * @return The PropertySpec
     */
    PropertySpec finish();

}