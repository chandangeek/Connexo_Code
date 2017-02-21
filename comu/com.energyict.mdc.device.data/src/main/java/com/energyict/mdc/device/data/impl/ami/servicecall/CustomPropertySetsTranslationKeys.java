/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall;

import com.elster.jupiter.nls.TranslationKey;

/**
 * @author sva
 * @since 7/07/2016 - 10:20
 */
public enum CustomPropertySetsTranslationKeys implements TranslationKey {

    DOMAIN_NAME("Service call"),
    RELEASE_DATE("Release date"),
    DEVICE_MSG("ID of device commands"),
    NR_OF_UNCONFIRMED_DEVICE_COMMANDS("Number of unconfirmed device commands"),
    STATUS("Status"),
    DESTINATION_SPEC("Destination spec"),
    DESTINATION_IDENTIFICATION("Destination identification");

    private String defaultFormat;

    CustomPropertySetsTranslationKeys(String defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}