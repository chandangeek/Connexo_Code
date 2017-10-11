package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.KeyImportFailedException;
import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Optional;

/**
 * Base class to process an SecurityValue in the secure shipment file. This class understands how to obtain the
 * unencrypted value and store it in the DB using the data vault.
 * Assumed a single keypair (either certificate with private key or naked keypair) has been used to encrypt wrap keys, identifiable with a Felix property.
 */
abstract public class AbstractDataVaultImporter implements DeviceSecretImporter {

    public static final String IMPORT_KEY = "com.elster.jupiter.shipment.importer.keypair.alias";

    private final Thesaurus thesaurus;
    private final PkiService pkiService;
    private final Optional<String> keypairAlias;

    public AbstractDataVaultImporter(Thesaurus thesaurus, PkiService pkiService, Optional<String> keypairAlias) {
        this.thesaurus = thesaurus;
        this.pkiService = pkiService;
        this.keypairAlias = keypairAlias;
    }

    @Override
    public SecurityValueWrapper importSecret(byte[] encryptedDeviceSecret, byte[] initializationVector, byte[] encryptedSymmetricWrapKey,
                                             String symmetricAlgorithm, String asymmetricAlgorithm)
            throws KeyImportFailedException {
        if (!keypairAlias.isPresent()) {
            throw new KeyImportFailedException(thesaurus, MessageSeeds.NO_IMPORT_KEY_DEFINED, IMPORT_KEY);
        }
        Optional<KeypairWrapper> keypairWrapper = pkiService.findKeypairWrapper(keypairAlias.get());
        if (!keypairWrapper.isPresent()) {
            throw new KeyImportFailedException(thesaurus, MessageSeeds.IMPORT_KEY_NOT_FOUND, keypairAlias.get());
        }
        if (keypairWrapper.get().hasPrivateKey()) {
            try {
                byte[] decryptedWrapKey = decryptWrapKey(encryptedSymmetricWrapKey, asymmetricAlgorithm, keypairWrapper.get());
                byte[] decryptedDeviceKey = decryptDeviceKey(encryptedDeviceSecret, initializationVector, symmetricAlgorithm, decryptedWrapKey);
                SecurityValueWrapper instance = createPlaintextWrapper(decryptedDeviceKey);
                return instance;
            } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
                throw new KeyImportFailedException(thesaurus, MessageSeeds.DEVICE_KEY_IMPORT_FAILED, e.getLocalizedMessage());
            }
        } else {
            throw new KeyImportFailedException(thesaurus, MessageSeeds.INCORRECT_IMPORT_KEY, keypairAlias.get());
        }
    }

    abstract public SecurityValueWrapper createPlaintextWrapper(byte[] bytes);

    private byte[] decryptDeviceKey(byte[] encryptedDeviceSecret, byte[] initializationVector, String symmetricAlgorithm, byte[] decryptedWrapKey) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {
        Cipher symmetricCypher = Cipher.getInstance(symmetricAlgorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decryptedWrapKey, "AES");
        symmetricCypher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(initializationVector));
        return symmetricCypher.doFinal(encryptedDeviceSecret);
    }

    private byte[] decryptWrapKey(byte[] encryptedSymmetricWrapKey, String asymmetricAlgorithm, KeypairWrapper keypairWrapper) throws
            InvalidKeyException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            BadPaddingException {
        PrivateKey privateKey = keypairWrapper.getPrivateKeyWrapper().get().getPrivateKey().get();
        Cipher cipher = Cipher.getInstance(asymmetricAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedSymmetricWrapKey);
    }
}
