/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol;

import java.util.List;

/**
 * Models the category of a device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:03)
 */
public interface DeviceMessageCategory {

    /**
     * Gets the name of this DeviceMessageCategory.
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the description of this DeviceMessageCategory.
     *
     * @return The description
     */
    String getDescription();

    /**
     * Gets the unique identifier this DeviceMessageCategory.
     *
     * @return The identifier
     */
    int getId();

    /**
     * Gets the {@link DeviceMessageSpec}s that are part of this DeviceMessageCategory.
     *
     * @return The DeviceMessageSpecs that are part of this DeviceMessageCategory
     */
    List<DeviceMessageSpec> getMessageSpecifications();

}