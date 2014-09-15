package com.energyict.mdc.protocol.api.device.messages;

import java.util.List;

/**
 * Provides services for {@link DeviceMessageCategory DeviceMessageCategories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-11 (13:30)
 */
public interface DeviceMessageService {

    public static final String COMPONENT_NAME = "DMC";

    /**
     * Returns the List of available {@link DeviceMessageCategory}.
     *
     * @return The List
     */
    public List<DeviceMessageCategory> allCategories();

}