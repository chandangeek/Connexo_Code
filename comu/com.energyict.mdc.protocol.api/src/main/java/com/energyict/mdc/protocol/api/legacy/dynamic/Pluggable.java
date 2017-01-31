/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.energyict.mdc.common.TypedProperties;

/**
 * defines the contract between a pluggable class implementation and the
 * plugabble class business object
 */
public interface Pluggable extends ConfigurationSupport {

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    String getVersion();

    /**
     * add the properties
     *
     * @param properties properties to add
     */
    void addProperties(TypedProperties properties);
}
