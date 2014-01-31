package com.energyict.mdc.device.config;

/**
 * Copyrights EnergyICT
 * Date: 20/06/13
 * Time: 11:17
 */
public enum ChannelSpecLinkType {
    PRIME(0, "prime"), TIME_OF_USE(1, "timeOfUse");

    private int code;
    private String translationKey;

    private ChannelSpecLinkType(int code, String translationKey) {
        this.code=code;
        this.translationKey=translationKey;
    }


    public int getCode() {
        return code;
    }

    public static ChannelSpecLinkType fromDbCode(int code) {
        switch (code) {
            case 0: return PRIME;
            case 1: return TIME_OF_USE;
            default: return null;
        }
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
