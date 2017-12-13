package com.elster.jupiter.pki;

import java.security.PublicKey;

/**
 * Capable of importing a specific key into connexo, into a form understood by either Connexo (DataVault key) or an
 * external party (HSM)
 */
public interface DeviceSecretImporter {
    /**
     * Import the encrypted secrets (keys, passphrases) into Connexo. As Connexo can not know how a secret should be stored (which columns are required)
     * the task is delegated to the appropriate {@link DeviceSecretImporter}. {@link DeviceSecretImporter}s are provided by a
     * KeyFactory ({@link com.elster.jupiter.pki.PrivateKeyFactory}, {@link com.elster.jupiter.pki.SymmetricKeyFactory},
     * {@link com.elster.jupiter.pki.PassphraseFactory}). Which factory is asked for a DeviceKeyImporter depends on the KeyEncryptionMethod
     * as defined on the SecurityAccessor.
     *
     * @param encryptedDeviceSecret This is the encrypted version of the (symmetric) device secret (could be a key, passphrase, ssl key).
     * The device secret is (symmetrically) encrypted with a wrap key (KEK), also contained in the shipment file
     * @param initializationVector The IV to use when decrypting the device key
     * @param encryptedSymmetricWrapKey The (asymmetrically) encrypted wrap key(KEK) used to encrypt the device key.
     * @param symmetricAlgorithm The algorithm used to encrypt the device key with the wrap key
     * @param asymmetricAlgorithm The algorithm used to encrypt the wrap key(KEK) with a public key. The private key
     * counterpart will be stored in Connexo (using DataVault) or in HSM or a hybrid solution
     * @return A SecurityValueWrapper.
     * @throws KeyImportFailedException
     */
    SecurityValueWrapper importSecret(byte[] encryptedDeviceSecret, byte[] initializationVector, byte[] encryptedSymmetricWrapKey,
                                      String symmetricAlgorithm, String asymmetricAlgorithm)
            throws KeyImportFailedException;

    /**
     * Verify that the public key passed as argument is the public key that had to be used to encrypt WrapKeys for the secret
     */
    void verifyPublicKey(PublicKey publicKey);

}
