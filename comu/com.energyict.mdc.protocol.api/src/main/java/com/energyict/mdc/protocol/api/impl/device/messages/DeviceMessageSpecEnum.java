/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.List;
import java.util.Optional;

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
    String getKey();

    String getDefaultFormat();

    DeviceMessageId getId();

    /**
     * Gets the List of {@link PropertySpec}s that
     * specify in detail which attributes are required and which are optional.
     *
     * @param propertySpecService The PropertySpecService
     * @param thesaurus The Thesaurus
     * @return The List of PropertySpec
     */
    List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus);

    /**
     * Gets the {@link PropertySpec} with the specified name.
     *
     * @param name The name
     * @param propertySpecService The PropertySpecService
     * @param thesaurus The Thesaurus
     * @return The PropertySpec or <code>null</code> if no such PropertySpec exists
     */
    default Optional<PropertySpec> getPropertySpec(String name, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        return getPropertySpecs(propertySpecService, thesaurus)
                .stream()
                .filter(propertySpec -> propertySpec.getName().equals(name))
                .findAny();
    }

}