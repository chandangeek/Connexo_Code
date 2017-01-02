package com.energyict.mdc.protocol.api.device.messages;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.util.List;
import java.util.Optional;

/**
 * Provides functionality for DeviceMessage
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-11 (13:30)
 */
@ProviderType
public interface DeviceMessageSpecificationService {

    String COMPONENT_NAME = "DMC";

    /**
     * Provides a list of DeviceMessageCategories which can be selected by the User.
     * To get <i>all</i> the categories, please use {@link #allCategories()}
     *
     * @return a filtered list of categories
     * @since version 1.1
     */
    List<DeviceMessageCategory> filteredCategoriesForUserSelection();

    /**
     * Provides a list of DeviceMessageCategories that can be added
     * to the definition of a communication task.
     * To get <i>all</i> the categories, please use {@link #allCategories()}
     *
     * @return a filtered list of categories
     * @since version 3.0
     */
    List<DeviceMessageCategory> filteredCategoriesForComTaskDefinition();

    /**
     * Returns the List of <i>all</i> available {@link DeviceMessageCategory}.
     *
     * @return The List
     */
    List<DeviceMessageCategory> allCategories();

    /**
     * Finds the {@link DeviceMessageCategory} with the specified id.
     *
     * @param categoryId The id
     * @return The DeviceMessageCategory
     * @see DeviceMessageCategory#getId()
     */
    default Optional<DeviceMessageCategory> findCategoryById(int categoryId) {
        return allCategories()
                    .stream()
                    .filter(category -> category.getId() == categoryId)
                    .findFirst();
    }

    /**
     * Finds The {@link DeviceMessageSpec} with the specified id.
     *
     * @param messageSpecIdDbValue The dbValue of the {@link DeviceMessageId}
     * @return The DeviceMessageSpec
     * @see DeviceMessageSpec#getId()
     * @see DeviceMessageId#dbValue()
     */
    Optional<DeviceMessageSpec> findMessageSpecById(long messageSpecIdDbValue);

    Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOptionFor(DeviceMessageId deviceMessageId);

    Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOptionsFor(DeviceMessageId deviceMessageId);

    DeviceMessageCategory getFirmwareCategory();
}