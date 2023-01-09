/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public enum FirmwareType implements TranslationKey {
    // The order defines how firmware types are displayed in UI
    METER("meter", "Device firmware"),
    COMMUNICATION("communication", "Communication firmware"),
    AUXILIARY("auxiliary", "Auxiliary firmware"),
    CA_CONFIG_IMAGE("caConfigImage", "Image");

    private final String type;
    private final String description;

    FirmwareType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getKey() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getDefaultFormat() {
        return description;
    }

    public String getTranslation(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }
}
