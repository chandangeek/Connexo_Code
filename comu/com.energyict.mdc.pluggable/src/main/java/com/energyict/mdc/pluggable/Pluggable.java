/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.energyict.mdc.common.TypedProperties;

/**
 * Models a component that can be plugged into the meter data collection engine.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-27 (15:00)
 */
public interface Pluggable extends HasDynamicProperties {

    /**
     * Returns the implementation version.
     *
     * @return a version string
     */
    public String getVersion();

    /**
     * Copies the properties that are known and supported.
     *
     * @param properties The TypedProperties
     */
    public void copyProperties(TypedProperties properties);

}