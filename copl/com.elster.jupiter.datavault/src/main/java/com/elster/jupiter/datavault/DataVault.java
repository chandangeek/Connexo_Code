package com.elster.jupiter.datavault;

import com.elster.jupiter.orm.Encrypter;

import java.io.OutputStream;
import java.io.Serializable;

/**
 * Copyrights EnergyICT
 *
 * @since 9/6/12 3:39 PM
 */
public interface DataVault extends Serializable, Encrypter {
    String encrypt(byte[] decrypted);
    byte[] decrypt(String encrypted);
    void createVault(OutputStream stream);
}
