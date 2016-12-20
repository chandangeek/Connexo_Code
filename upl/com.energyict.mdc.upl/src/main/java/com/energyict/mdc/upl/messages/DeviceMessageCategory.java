package com.energyict.mdc.upl.messages;

import java.util.List;

/**
 * Models the category of a {@link DeviceMessage}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:03)
 */
public interface DeviceMessageCategory {

    /**
     * Gets the translated name of this DeviceMessageCategory.
     *
     * @return The translated name
     */
    String getName();

    /**
     * Get the key for the translation of the name of this message
     */
    String getNameResourceKey();

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
     * Gets the PrimaryKey for this {@link DeviceMessageCategory}
     *
     * @return the primary key
     */
    DeviceMessageCategoryPrimaryKey getPrimaryKey();

    /**
     * Gets the {@link DeviceMessageSpec}s that are part of this DeviceMessageCategory.
     *
     * @return The DeviceMessageSpecs that are part of this DeviceMessageCategory
     */
    List<DeviceMessageSpec> getMessageSpecifications();

}