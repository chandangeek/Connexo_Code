package com.energyict.mdc.protocol.api.legacy.dynamic;

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
    List<com.elster.jupiter.properties.PropertySpec> getRequiredProperties();

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    List<com.elster.jupiter.properties.PropertySpec> getOptionalProperties();

}
