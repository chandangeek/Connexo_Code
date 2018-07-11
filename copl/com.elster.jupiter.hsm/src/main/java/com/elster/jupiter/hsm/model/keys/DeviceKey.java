package com.elster.jupiter.hsm.model.keys;


import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;


/**
 * This class should is just a model based on info that can be extracted from XML import file (or not only XML). Basically requires all info needed to perform a secure import.
 */
public abstract class DeviceKey {

    private final KeyType keyType;

    private final SymmetricAlgorithm alg;
    private final int keyLength;
    private final byte[] encryptedKey;
    private final byte[] initVector;


    public DeviceKey(SymmetricAlgorithm algorithm, int keyLength, byte[] encryptedKey, byte[] initVector, KeyType keyType) throws HsmBaseException {
        this.alg = algorithm;
        this.keyLength = keyLength;
        this.encryptedKey = encryptedKey;
        this.initVector = initVector;
        this.keyType = keyType;
    }


    public int getKeyLength() {
        return keyLength;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public byte[] getInitVector() {
        return initVector;
    }

    public SymmetricAlgorithm getEncryptionAlgorithm() {
        return alg;
    }

    public abstract com.atos.worldline.jss.api.custom.energy.DeviceKey toHsmFormat() throws HsmBaseException;
}
