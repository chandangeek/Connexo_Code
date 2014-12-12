package com.energyict.protocolimplv2.elster.ctr.MTU155.info;

/**
 * Copyrights EnergyICT
 * Date: 24/02/11
 * Time: 18:32
 */
public enum VolumeCalculationMethod {

    NONE(0, "None"),
    ISO_9951(1, "ISO 9951"),
    MOD_1(2, "Mod 1"),
    MOD_2(4, "Mod 2"),
    MOD_3(8, "Mod 3"),
    UNI_ISO_5167(0x10, "UNI EN ISO 5167"),
    EN12405(0x20, "EN12405"),
    RESERVED(0x21, "Reserved"),
    INVALID(0xFF, "Invalid");

    private final int methodNr;
    private final String description;

    private VolumeCalculationMethod(int methodNr, String description) {
        this.description = description;
        this.methodNr = methodNr;
    }

    public String getDescription() {
        return description;
    }

    public int getMethodNr() {
        return methodNr;
    }

    public static VolumeCalculationMethod fromMethodNr(int methodNr) {
        for (VolumeCalculationMethod method : VolumeCalculationMethod.values()) {
            if (method.getMethodNr() == methodNr) {
                return method;
            }
        }
        if ((methodNr >= 9) && (methodNr <= 0x7F)) {
            return RESERVED;
        } else {
            return INVALID;
        }
    }

}
