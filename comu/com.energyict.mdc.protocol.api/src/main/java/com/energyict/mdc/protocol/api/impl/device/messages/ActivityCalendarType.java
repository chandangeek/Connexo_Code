package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Copyrights EnergyICT
 * Date: 25/10/13
 * Time: 17:27
 * Author: khe
 */
enum ActivityCalendarType implements TranslationKey {

    PublicNetwork("PublicNetwork", "Public network"),
    Provider("Provider", "Provider");

    private final String key;
    private final String defaultFormat;

    ActivityCalendarType(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }
    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static String[] getAllDescriptions() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getKey();
        }
        return result;
    }
}