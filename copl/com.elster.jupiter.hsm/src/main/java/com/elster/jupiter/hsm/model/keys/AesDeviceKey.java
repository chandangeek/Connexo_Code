package com.elster.jupiter.hsm.model.keys;

import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;

import com.atos.worldline.jss.api.custom.energy.AESDeviceKey;

public class AesDeviceKey extends DeviceKey {
    public AesDeviceKey(SymmetricAlgorithm algorithm, int keyLength, byte[] encryptedKey, byte[] initVector, KeyType keyType) throws EncryptBaseException {
        super(algorithm, keyLength, encryptedKey, initVector, keyType);
        if (SymmetricAlgorithm.AES_256_CBC.equals(super.getEncryptionAlgorithm())) {
            throw new EncryptBaseException("Trying to build a non AES encryption key using AES impl");
        }
    }

    @Override
    public AESDeviceKey toHsmFormat() throws EncryptBaseException {
        return new AESDeviceKey(super.getInitVector(), super.getEncryptionAlgorithm().getHsmSpecs().getKekEncryptionMethod(), super.getKeyLength(), super.getEncryptedKey());
    }


}
