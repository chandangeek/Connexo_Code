package com.elster.jupiter.datavault;

import aQute.bnd.annotation.ProviderType;

import java.io.Serializable;

/**
 * Copyrights EnergyICT
 *
 * @since 9/6/12 3:39 PM
 */
@ProviderType
public interface DataVault extends Serializable {
    String encrypt(byte[] decrypted);
    byte[] decrypt(String encrypted);
}