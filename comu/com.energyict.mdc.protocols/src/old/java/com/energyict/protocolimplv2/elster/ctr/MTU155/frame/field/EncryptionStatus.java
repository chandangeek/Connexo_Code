/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

public enum EncryptionStatus {

    NO_ENCRYPTION(0, false, "No encryption used"),
    KEYC_ENCRYPTION(1, true, "Encrypted and authenticated using KEYC"),
    KEYT_ENCRYPTION(2, true, "Encrypted and authenticated using Temporary Key [KEYT]"),
    KEYF_ENCRYPTION(3, true, "Encrypted and authenticated using Factory Key [KEYF]"),
    INVALID_ENCRYPTION(-1, false, "Invalid encryption key");

    private final int encryptionStateBits;
    private final String description;
    private final boolean isEncrypted;

    private EncryptionStatus(int encryptionBits, boolean encrypted, String description) {
        this.encryptionStateBits = encryptionBits;
        this.isEncrypted = encrypted;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getEncryptionStateBits() {
        return encryptionStateBits;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public static EncryptionStatus fromEncryptionBits(int bits) {
        for (EncryptionStatus status : EncryptionStatus.values()) {
            if (status.getEncryptionStateBits() == (bits & 0x03)) {
                return status;
            }
        }
        return INVALID_ENCRYPTION;
    }

}
