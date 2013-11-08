package com.energyict.dlms;

/**
 * Copyrights EnergyICT
 * Date: 14/02/11
 * Time: 10:41
 */
public enum DLMSReference {

    LN(ProtocolLink.LN_REFERENCE, "Long name reference"),
    SN(ProtocolLink.SN_REFERENCE, "Short name reference"),
    INVALID(-1, "Invalid reference");

    private final int reference;
    private final String description;

    private DLMSReference(int reference, String description) {
        this.reference = reference;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getReference() {
        return reference;
    }

    public static DLMSReference fromValue(int reference) {
        for (DLMSReference ref : values()) {
            if (ref.getReference() == reference) {
                return ref;
            }
        }
        return INVALID;
    }

}
