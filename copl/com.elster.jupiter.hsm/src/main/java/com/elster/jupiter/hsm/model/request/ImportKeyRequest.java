package com.elster.jupiter.hsm.model.request;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;

import com.atos.worldline.jss.api.custom.energy.AESDeviceKey;
import com.atos.worldline.jss.api.custom.energy.DeviceKey;
import com.atos.worldline.jss.api.custom.energy.TransportKey;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.UnsupportedKEKEncryptionMethodException;

import javax.annotation.Nonnull;

public class ImportKeyRequest {

    private final String wrapperKeyLabel;
    private final AsymmetricAlgorithm wrapperKeyAlgorithm;
    private final byte[] encryptedTransportKey;

    private final SymmetricAlgorithm deviceKeyAlgorhitm;
    private final byte[] deviceKeyValue;
    private final byte[] deviceKeyInitialVector;

    /**
     *
     * @param wrapperKeyLabel as present in import file. This is extremely important while this class will try to map it to the real HSM label.
     * @param wrapperKeyAlgorithm algorithm used to encrypt transport key.
     * @param encryptedTransportKey encrypted transport key (symmetric one).
     * @param transportKeyAlgorithm algorithm used to encode device key.
     * @param deviceKeyValue device key encrypted using transportKeyAlgorithm and encryptedTransportKey
     * @param deviceKeyInitialVector initial vector (first bytes or by convention).In file from the deviceKeyValue + deviceKeyInitialVector will form the cipher value.
     */
    public ImportKeyRequest(@Nonnull String wrapperKeyLabel, @Nonnull AsymmetricAlgorithm wrapperKeyAlgorithm, @Nonnull byte[] encryptedTransportKey, @Nonnull SymmetricAlgorithm transportKeyAlgorithm, @Nonnull byte[] deviceKeyValue, @Nonnull byte[] deviceKeyInitialVector) {
        this.wrapperKeyLabel = wrapperKeyLabel;
        this.wrapperKeyAlgorithm = wrapperKeyAlgorithm;
        this.encryptedTransportKey = encryptedTransportKey;
        this.deviceKeyAlgorhitm = transportKeyAlgorithm;
        this.deviceKeyValue = deviceKeyValue;
        this.deviceKeyInitialVector = deviceKeyInitialVector;
    }


    public AsymmetricAlgorithm getWrapperKeyAlgorithm() {
        return wrapperKeyAlgorithm;
    }


    public String getImportLabel(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        return hsmConfiguration.get(mapToHsmLabel(hsmConfiguration)).getImportLabel();
    }

    public TransportKey getTransportKey(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        try {
            return new TransportKey(new KeyLabel(mapToHsmLabel(hsmConfiguration)), deviceKeyAlgorhitm.getKeySize(), encryptedTransportKey);
        } catch (UnsupportedKEKEncryptionMethodException e) {
            throw new HsmBaseException(e);
        }
    }

    public DeviceKey getDeviceKey(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        if (SymmetricAlgorithm.AES_256_CBC.equals(deviceKeyAlgorhitm)){
            String hsmLabel = mapToHsmLabel(hsmConfiguration);
            return new AESDeviceKey(deviceKeyInitialVector, deviceKeyAlgorhitm.getHsmSpecs().getKekEncryptionMethod(), hsmConfiguration.get(hsmLabel).getDeviceKeyLength(), deviceKeyValue);
        }
        throw new HsmBaseException("Could not construct device key based on symmetric algorithm:" + deviceKeyAlgorhitm);
    }

    public SessionKeyCapability getImportSessionCapability(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        return hsmConfiguration.get(mapToHsmLabel(hsmConfiguration)).getImportSessionCapability();
    }

    private String mapToHsmLabel(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        return hsmConfiguration.map(wrapperKeyLabel);
    }
}
