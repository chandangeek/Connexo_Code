/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Models the possible values of a {@link PropertySpec}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-19 (15:41)
 */
@ProviderType
public interface PropertySpecPossibleValues {

    PropertySelectionMode getSelectionMode();

    /**
     * Gets the possible values.
     *
     * @return The possible values
     */
    List getAllValues();

    /**
     * Returns <code>true</code> if the possible values are an
     * exhaustive list, i.e. if no other values will be accepted.
     * Therefore, if this returns <code>false</code> then
     * the list of values returned by {@link #getAllValues()}
     * can be regarded as a example list of values.
     *
     * @return A flag that indicates if only values returned by the getAllValues method are accepted
     */
    boolean isExhaustive();

    /**
     * Returns <code>true</code> if the possible values should be editable,
     * i.e. the user should be allowed to type in text directly into the field,
     * after which matching possibilities are shown. This could be used in situations
     * where i.e. a ComboBox contains a lot of possible values. In this case,
     * it is not convenient for the user to select the value from the picker
     * (as he has to search his desired value in a big list of possible values).
     *
     * @return A flag that indicates if values should be editable or not
     */
    boolean isEditable();

    /**
     * Gets the default value that will be used for the PropertySpec.
     *
     * @return The default value
     */
    Object getDefault();

}