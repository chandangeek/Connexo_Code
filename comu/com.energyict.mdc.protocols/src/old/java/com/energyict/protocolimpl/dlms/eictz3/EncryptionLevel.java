/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.eictz3;

public enum EncryptionLevel {

    NO_ENCRYPTION(0), MESSAGE_AUTHENTICATION(1), MESSAGE_ENCRYPTION(2), BOTH(3);

    private final int encryptionLevel;

    /**
     * Create new instance
     *
     * @param encryptionLevel the value of the encryptionLevel
     */
    EncryptionLevel(final int encryptionLevel) {
        this.encryptionLevel = encryptionLevel;
    }

    /**
     * Returns the encryption level that corresponds to the given property value.
     *
     * @param encryptionValue The encryption value.
     * @return The matching {@link EncryptionLevel}, <code>null</code> if none matches.
     */
    public static final EncryptionLevel getByPropertyValue(final int encryptionValue) {
        for (final EncryptionLevel level : values()) {
            if (level.encryptionLevel == encryptionValue) {
                return level;
            }
        }

        return null;
    }

    /**
     * Returns the encryption value associated with the level.
     *
     * @return The encryption value associated with the level.
     */
    public final int getEncryptionValue() {
        return this.encryptionLevel;
    }
}
