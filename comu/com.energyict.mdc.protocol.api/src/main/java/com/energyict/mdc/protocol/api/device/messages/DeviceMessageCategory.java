package com.energyict.mdc.protocol.api.device.messages;

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
    public String getName();

    /**
     * Gets the description of this DeviceMessageCategory.
     *
     * @return The description
     */
    public String getDescription();

    /**
     * Gets the unique identifier this DeviceMessageCategory.
     *
     * @return The identifier
     */
    public int getId();

    /**
     * Gets the {@link DeviceMessageSpec}s that are part of this DeviceMessageCategory.
     *
     * @return The DeviceMessageSpecs that are part of this DeviceMessageCategory
     */
    public List<DeviceMessageSpec> getMessageSpecifications();

    /**
     * Gets the PrimaryKey for this {@link DeviceMessageCategory}
     *
     * @return the primary key
     */
    public DeviceMessageCategoryPrimaryKey getPrimaryKey();

}