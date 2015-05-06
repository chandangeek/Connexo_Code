package com.energyict.mdc.protocol.api.device.messages;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

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

    public static final String COMPONENT_NAME = "DMC";

    /**
     * Provides a list of DeviceMessageCategories which can be selected by the User.
     * Some categories are filtered because they are used by other processes (like firmware).
     * To get <i>all</i> the categories, please use {@link #allCategories()}
     *
     * @return a filtered list of categories
     * @since version 1.1
     */
    public List<DeviceMessageCategory> filteredCategoriesForUserSelection();

    /**
     * Returns the List of <i>all</i> available {@link DeviceMessageCategory}.
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

    public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOptionFor(DeviceMessageId deviceMessageId);

    public DeviceMessageCategory getFirmwareCategory();
}