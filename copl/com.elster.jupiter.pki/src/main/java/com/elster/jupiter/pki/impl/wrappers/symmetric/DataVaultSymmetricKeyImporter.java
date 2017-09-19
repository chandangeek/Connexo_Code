package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.KeyImportFailedException;
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
 * Created by bvn on 8/24/17.
 */
public class DataVaultSymmetricKeyImporter implements DeviceSecretImporter {

    public static final String IMPORT_KEY = "com.elster.jupiter.shipment.importer.certificate.alias";

    private final KeyAccessorType keyAccessorType;
    private final Thesaurus thesaurus;
    private final PkiService pkiService;
    private final Optional<String> certificateAlias;
    private final DataModel dataModel;

    public DataVaultSymmetricKeyImporter(KeyAccessorType keyAccessorType, Thesaurus thesaurus, PkiService pkiService, Optional<String> certificateAlias, DataModel dataModel) {
        this.keyAccessorType = keyAccessorType;
        this.thesaurus = thesaurus;
        this.pkiService = pkiService;
        this.certificateAlias = certificateAlias;
        this.dataModel = dataModel;
    }

    @Override
    public SecurityValueWrapper importSecret(byte[] encryptedDeviceSecret, byte[] initializationVector, byte[] encryptedSymmetricWrapKey,
                                             String symmetricAlgorithm, String asymmetricAlgorithm)
            throws KeyImportFailedException {
        if (!certificateAlias.isPresent()) {
            throw new KeyImportFailedException(thesaurus, MessageSeeds.NO_IMPORT_KEY_DEFINED, IMPORT_KEY);
        }
        Optional<CertificateWrapper> certificateWrapper = pkiService.findCertificateWrapper(certificateAlias.get());
        if (!certificateWrapper.isPresent()) {
            throw new KeyImportFailedException(thesaurus, MessageSeeds.IMPORT_KEY_NOT_FOUND, certificateAlias.get());
        }
        if (certificateWrapper.get() instanceof ClientCertificateWrapper) {
            try {
                byte[] decryptedWrapKey = decryptWrapKey(encryptedSymmetricWrapKey, asymmetricAlgorithm, (ClientCertificateWrapper) certificateWrapper.get());
                byte[] decryptedDeviceKey = decryptDeviceKey(encryptedDeviceSecret, initializationVector, symmetricAlgorithm, decryptedWrapKey);
                PlaintextSymmetricKey instance = createPlaintextSymmetricKeyWrapper(decryptedDeviceKey);
                return instance;
            } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
                throw new KeyImportFailedException(thesaurus, MessageSeeds.DEVICE_KEY_IMPORT_FAILED, e.getLocalizedMessage());
            }
        } else {
            throw new KeyImportFailedException(thesaurus, MessageSeeds.INCORRECT_IMPORT_KEY, certificateAlias.get());
        }
    }

    private PlaintextSymmetricKey createPlaintextSymmetricKeyWrapper(byte[] bytes) {
        PlaintextSymmetricKeyImpl instance = dataModel.getInstance(PlaintextSymmetricKeyImpl.class);
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, keyAccessorType.getKeyType().getKeyAlgorithm());
        instance.init(keyAccessorType.getKeyType(), keyAccessorType.getDuration().get());
        instance.setKey(secretKeySpec);
        return instance;
    }

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

    private byte[] decryptWrapKey(byte[] encryptedSymmetricWrapKey, String asymmetricAlgorithm, ClientCertificateWrapper certificateWrapper) throws
            InvalidKeyException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            BadPaddingException {
        PrivateKey privateKey = certificateWrapper.getPrivateKeyWrapper().getPrivateKey();
//                    Cipher cipher = Cipher.getInstance(clientCertificateWrapper.getKeyType().getKeyAlgorithm());
        Cipher cipher = Cipher.getInstance(asymmetricAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedSymmetricWrapKey);
    }
}
