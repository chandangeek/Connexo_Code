/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault;

import com.elster.jupiter.orm.Encrypter;

import java.io.OutputStream;
import java.io.Serializable;

public interface DataVault extends Serializable, Encrypter {
    String encrypt(byte[] decrypted);
    byte[] decrypt(String encrypted);
    void createVault(OutputStream stream);
}
