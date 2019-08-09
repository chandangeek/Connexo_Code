/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import com.energyict.mdc.upl.TypedProperties;

import java.util.List;

/**
 * Provides {@link ConnectionTaskProperty properties} of a {@link ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-09 (17:01)
 */
public interface ConnectionTaskPropertyProvider {

    List<ConnectionTaskProperty> getProperties();

    /**
     * Provides the current properties ({@link #getProperties()} in the TypedProperties format.
     *
     * @return the TypedProperties
     */
    TypedProperties getTypedProperties();

    void saveAllProperties();

    void removeAllProperties();

}