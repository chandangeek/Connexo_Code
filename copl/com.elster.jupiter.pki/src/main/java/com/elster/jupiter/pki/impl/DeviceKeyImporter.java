package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.SecurityValueWrapper;

/**
 * Capable of importing a specific key into connexo, into a form understood by either Connexo (DataVault key) or an
 * external party (HSM)
 */
public interface DeviceKeyImporter {
    SecurityValueWrapper importKey(byte[] encryptedDeviceKey, byte[] initializationVector, byte[] encryptedSymmetricKey,
                                   String symmetricAlgorithm, String asymmetricAlgorithm)
            throws KeyImportFailedException;

}
