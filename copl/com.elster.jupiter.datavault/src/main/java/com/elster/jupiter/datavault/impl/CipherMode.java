package com.elster.jupiter.datavault.impl;

import javax.crypto.Cipher;

/**
 * Copyrights EnergyICT
 *
 * @since 9/5/12 1:16 PM
 */
enum CipherMode {
    encrypt(Cipher.ENCRYPT_MODE),
    decrypt(Cipher.DECRYPT_MODE);

    private final int mode;

    CipherMode(int mode) {
        this.mode=mode;
    }

    public int asInt() {
        return mode;
    }
}