package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

/**
 * Models the minimal behavior of a specification of a device message,
 * i.e. the description of all of the attributes of the DeviceMessage
 * and which of these attributes are required or optional.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-11 (15:31)
 */
public interface DeviceMessageSpecEnum extends TranslationKey {

    /**
     * Returns the translation key for the name of this DeviceMessageSpec.
     *
     * @return the name of this DeviceMessageSpec
     */
    public String getKey();

    public String getDefaultFormat();

    public DeviceMessageId getId();

    /**
     * Gets the List of {@link PropertySpec}s that
     * specify in detail which attributes are required and which are optional.
     *
     * @param propertySpecService The PropertySpecService
     * @return The List of PropertySpec
     */
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService);

    /**
     * Gets the {@link PropertySpec} with the specified name.
     *
     * @param name The name
     * @param propertySpecService The PropertySpecService
     * @return The PropertySpec or <code>null</code> if no such PropertySpec exists
     */
    public PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService);

}