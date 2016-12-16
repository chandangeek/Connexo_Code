package com.energyict.mdc.upl.messages;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;
import java.util.Optional;

/**
 * Models the specification of a {@link DeviceMessage},
 * i.e. the description of all of the attributes of the DeviceMessage
 * and which of these attributes are required or optional.
 * A DeviceMessage can be standard, meaning that it is supported
 * off the shelve by the ComServer and was not added to the ComServer
 * for the purpose of a single customer installation.
 * Any DeviceMessageSpec that is created through the ComServer
 * API will by default be a non-standard DeviceMessage.
 * Note that non standard messages can still be part
 * of standard {@link DeviceMessageCategory DeviceMessageCategories}.
 * <p/>
 * When adding new messages, keep in mind to also add the translation key (category.message) in the NLS database.
 * Also add a translation key for every attribute of the new message.
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
     * Gets the globally unique identifier of this DeviceMessageSpec.
     */
    long getId();

    /**
     * Returns the translatable name of this DeviceMessageSpec.
     *
     * @return the name of this DeviceMessageSpec
     */
    String getName();

    /**
     * Gets the {@link TranslationKey} to translate the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    TranslationKey getNameTranslationKey();

    /**
     * Gets the List of {@link PropertySpec}s that
     * specify in detail which attributes are required and which are optional.
     *
     * @return The List of PropertySpec
     */
    List<PropertySpec> getPropertySpecs();

    /**
     * Gets the {@link PropertySpec} with the specified name.
     *
     * @param name The name
     * @return The PropertySpec or <code>Optional.empty()</code> if no such PropertySpec exists
     */
    default Optional<PropertySpec> getPropertySpec(String name) {
        return getPropertySpecs()
                    .stream()
                    .filter(each -> each.getName().equals(name))
                    .findAny();
    }

}