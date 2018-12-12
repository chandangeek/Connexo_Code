/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import javax.crypto.Cipher;

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