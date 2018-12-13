/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

/**
 * defines the contract of a configurable class implementation
 */

public interface ConfigurationSupport {

    /**
     * Returns a list of required property keys
     *
     * @return a List of String objects
     */
    List<PropertySpec> getRequiredProperties();

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    List<PropertySpec> getOptionalProperties();

}