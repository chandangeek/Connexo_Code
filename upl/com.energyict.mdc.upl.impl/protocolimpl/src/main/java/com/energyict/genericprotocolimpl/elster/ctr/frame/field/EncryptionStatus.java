package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 17:21:38
 */
public enum EncryptionStatus {

    NO_ENCRYPTION(0, "No encryption used"),
    KEYC_ENCRYPTION(1, "Encrypted and authenticated using KEYC"),
    KEYT_ENCRYPTION(2, "Encrypted and authenticated using Temporary Key [KEYT]"),
    KEYF_ENCRYPTION(3, "Encrypted and authenticated using Factory Key [KEYF]"),
    UNKNOWN_ENCRYPTION(-1, "Invalid encryption key");

    private final int encryptionStateBits;
    private final String description;

    private EncryptionStatus(int encryptionBits, String description) {
        this.encryptionStateBits = encryptionBits;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getEncryptionStateBits() {
        return encryptionStateBits;
    }

    public static EncryptionStatus fromEncryptionBits(int bits) {
        for (EncryptionStatus status : EncryptionStatus.values()) {
            if (status.getEncryptionStateBits() == (bits & 0x03)) {
                return status;
            }
        }
        return UNKNOWN_ENCRYPTION;
    }

}
