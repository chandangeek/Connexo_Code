package com.energyict.mdc.common;

import java.io.File;

/**
 * Copyrights EnergyICT
 *
 * @since 9/6/12 3:39 PM
 */
public interface DataVault {
    String encrypt(byte[] decrypted);
    byte[] decrypt(String encrypted);
    void createVault(File file);
}
