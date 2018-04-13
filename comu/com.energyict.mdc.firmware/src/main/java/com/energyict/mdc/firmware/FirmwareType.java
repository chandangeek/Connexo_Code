/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.nls.TranslationKey;

public enum FirmwareType implements TranslationKey {
    COMMUNICATION("communication", "Communication firmware"),
    METER("meter", "Meter firmware"),
    CA_CONFIG_IMAGE("caConfigImage", "Image");

    private String type;
    private String description;

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
}
