package com.energyict.mdc.protocol.api.device.messages;

import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.List;
import java.util.Optional;

/**
 * Provides functionality for DeviceMessage
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-11 (13:30)
 */
public interface DeviceMessageSpecificationService {

    public static final String COMPONENT_NAME = "DMC";

    /**
     * Returns the List of available {@link DeviceMessageCategory}.
     *
     * @return The List
     */
    public List<DeviceMessageCategory> allCategories();

    /**
     * Finds the {@link DeviceMessageCategory} with the specified id.
     *
     * @param categoryId The id
     * @return The DeviceMessageCategory
     * @see DeviceMessageCategory#getId()
     */
    public Optional<DeviceMessageCategory> findCategoryById(int categoryId);

    /**
     * Finds The {@link DeviceMessageSpec} with the specified id.
     *
     * @param messageSpecIdDbValue The dbValue of the {@link DeviceMessageId}
     * @return The DeviceMessageSpec
     * @see DeviceMessageSpec#getId()
     * @see DeviceMessageId#dbValue()
     */
    public Optional<DeviceMessageSpec> findMessageSpecById(long messageSpecIdDbValue);

}