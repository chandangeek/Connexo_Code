package com.elster.jupiter.hsm.model.keys;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;

import com.atos.worldline.jss.api.custom.energy.AESDeviceKey;

public class AesDeviceKey extends DeviceKey {
    public AesDeviceKey(SymmetricAlgorithm algorithm, int keyLength, byte[] encryptedKey, byte[] initVector, SessionKeyCapability sessionKeyCapability) throws HsmBaseException {
        super(algorithm, keyLength, encryptedKey, initVector, sessionKeyCapability);
        if (!SymmetricAlgorithm.AES_256_CBC.equals(super.getEncryptionAlgorithm())) {
            throw new HsmBaseException("Trying to build a non AES encryption key using AES impl");
        }
    }

    @Override
    public AESDeviceKey toHsmFormat() throws HsmBaseException {
        return new AESDeviceKey(super.getInitVector(), super.getEncryptionAlgorithm().getHsmSpecs().getKekEncryptionMethod(), super.getKeyLength(), super.getEncryptedKey());
    }


}
