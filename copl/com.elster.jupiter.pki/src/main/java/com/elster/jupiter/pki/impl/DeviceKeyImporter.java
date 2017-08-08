package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.SecurityValueWrapper;

/**
 * Capable of importing a specific key into connexo, into a form understood by either Connexo (DataVault key) or an
 * external party (HSM)
 */
public interface DeviceKeyImporter {
    /**
     * Import the encrypted data into Connexo. As connexo can not know how a key should be stored (which columns are required)
     * the task is delegated to the appropriate DeviceKeyImporter. DeviceKeyImporters are provided by a
     * KeyFactory ({@link com.elster.jupiter.pki.PrivateKeyFactory}, {@link com.elster.jupiter.pki.SymmetricKeyFactory},
     * {@link com.elster.jupiter.pki.PassphraseFactory}). Which factory is asked for a DeviceKeyImporter depends on the KeyEncryptionMethod
     * as defined on the SecurityAccessor.
     *
     * @param encryptedDeviceKey This is the encrypted version of the (symmetric) device key. The device key is (symmetrically)
     * encrypted with a wrap key (KEK), also contained in the shipment file
     * @param initializationVector The IV to use when decrypting the device key
     * @param encryptedSymmetricWrapKey The (asymmetrically) encrypted wrap key(KEK) used to encrypt the device key.
     * @param symmetricAlgorithm The algorithm used to encrypt the device key with the wrap key
     * @param asymmetricAlgorithm The algorithm used to encrypt the wrap key(KEK) with a public key. The private key
     * counterpart will be stored in Connexo (using DataVault) or in HSM or a hybrid solution
     * @return A SecurityValueWrapper.
     * @throws KeyImportFailedException
     */
    SecurityValueWrapper importKey(byte[] encryptedDeviceKey, byte[] initializationVector, byte[] encryptedSymmetricWrapKey,
                                   String symmetricAlgorithm, String asymmetricAlgorithm)
            throws KeyImportFailedException;

}
