package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

/**
 * Copyrights EnergyICT
 * Date: 30.04.15
 * Time: 15:35
 */
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