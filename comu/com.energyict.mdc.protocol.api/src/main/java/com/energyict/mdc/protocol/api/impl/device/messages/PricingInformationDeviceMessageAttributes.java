/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum PricingInformationDeviceMessageAttributes implements TranslationKey {

    PricingInformationUserFileAttributeName(DeviceMessageConstants.PricingInformationUserFileAttributeName, "UserFile"),
    PricingInformationActivationDateAttributeName(DeviceMessageConstants.PricingInformationActivationDateAttributeName, "Activation date"),
    StandingChargeAttributeName(DeviceMessageConstants.StandingChargeAttributeName, "Standing charge");

    private final String key;
    private final String defaultFormat;

    PricingInformationDeviceMessageAttributes(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}