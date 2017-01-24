package com.energyict.protocolimplv2.elster.ctr.MTU155.info;

/**
 * Copyrights EnergyICT
 * Date: 24/02/11
 * Time: 18:32
 */
public enum ZCalculationMethod {

    NONE(0, "None"),
    ISO_SET_A(1, "UNI EN ISO 12213-3  (set a)"),
    ISO_SET_B(2, "UNI EN ISO 12213-3  (set b)"),
    ISO_SET_C(3, "UNI EN ISO 12213-3  (set c)"),
    ISO_SET_D(4, "UNI EN ISO 12213-3  (set d)"),
    MOD(5, "Mod"),
    GROSS_METHOD_1(6, "Gross method 1"),
    GROSS_METHOD_2(7, "Gross method 2"),
    DETAILED(8, "Detailed"),
    RESERVED(9, "Reserved"),
    INVALID(0xFF, "Invalid");

    private final int methodNr;
    private final String description;

    ZCalculationMethod(int methodNr, String description) {
        this.methodNr = methodNr;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getMethodNr() {
        return methodNr;
    }

    public static ZCalculationMethod fromMethodNr(int methodNr) {
        for (ZCalculationMethod method : ZCalculationMethod.values()) {
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
