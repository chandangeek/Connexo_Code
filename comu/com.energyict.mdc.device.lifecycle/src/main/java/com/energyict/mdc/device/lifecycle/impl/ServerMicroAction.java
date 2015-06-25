package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Models the implementation behavior of the {@link MicroAction}
 * interface and is therefore reserverd for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-23 (09:58)
 */
public interface ServerMicroAction {

    /**
     * Gets the {@link PropertySpec}s for this ServerMicroAction.
     *
     * @param propertySpecService The PropertySpecService
     * @return The List of PropertySpec
     */
    public default List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Collections.emptyList();
    }

    /**
     * Executes this Action on the specified {@link Device}
     * with the specified List of {@link ExecutableActionProperty properties}.
     *
     * @param device The Device
     * @param effectiveTimestamp The point in time when this transition will become effective, i.e. when the resulting state change will become effective
     * @param properties The List of
     */
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties);

}