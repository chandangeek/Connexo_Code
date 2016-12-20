package com.energyict.mdc.protocol.api.device.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.List;
import java.util.Optional;

/**
 * Models the specification of a device message,
 * i.e. the description of all of the attributes of the DeviceMessage
 * and which of these attributes are required or optional.
 * A DeviceMessage can be standard, meaning that it is supported
 * off the shelve by the ComServer and was not added to the ComServer
 * for the purpose of a single customer installation.
 * Any DeviceMessageSpec that is created through the ComServer
 * API will by default be a non-standard DeviceMessage.
 * Note that non standard messages can still be part
 * of standard {@link DeviceMessageCategory DeviceMessageCategories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (17:02)
 */
public interface DeviceMessageSpec {

    /**
     * Gets the {@link DeviceMessageCategory} of this DeviceMessage.
     *
     * @return The DeviceMessageCategory
     */
    DeviceMessageCategory getCategory();

    /**
     * Returns the translatable name of this DeviceMessageSpec
     *
     * @return the name of this DeviceMessageSpec
     */
    String getName();

    /**
     * Gets the PrimaryKey for this {@link DeviceMessageSpec}
     *
     * @return the primary key
     */
    DeviceMessageId getId();

    /**
     * Gets the List of {@link PropertySpec propertySpecs} that
     * specify in detail which attributes are required and which are optional.
     *
     * @return The List of PropertySpec
     */
    List<PropertySpec> getPropertySpecs();

    /**
     * Gets the {@link PropertySpec} with the specified name.
     *
     * @param name The name
     * @return The PropertySpec or an empty Optional if no such PropertySpec exists
     */
    default Optional<PropertySpec> getPropertySpec(String name) {
        return getPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getName().equals(name))
                .findAny();
    }
}