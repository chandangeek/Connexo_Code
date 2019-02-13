package com.elster.jupiter.hsm.model.request;

import com.atos.worldline.jss.api.custom.energy.*;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.UnsupportedKEKEncryptionMethodException;

import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;


public class ImportKeyRequest {

    private final String wrapperKeyLabel;
    private final AsymmetricAlgorithm wrapperKeyAlgorithm;
    private final byte[] encryptedTransportKey;

    private final SymmetricAlgorithm deviceKeyAlgorhitm;
    private final byte[] deviceKeyValue;
    private final byte[] deviceKeyInitialVector;
    private final HsmKeyType hsmKeyType;

    /**
     * @param wrapperKeyLabel as present in import file. This is extremely important while this class will try to map it to the real HSM label.
     * @param wrapperKeyAlgorithm algorithm used to encrypt transport/wrapper key.
     * @param encryptedTransportKey encrypted transport key (symmetric one).
     * @param transportKeyAlgorithm algorithm used to encode device key.
     * @param deviceKeyValue device key encrypted using transportKeyAlgorithm and encryptedTransportKey
     * @param deviceKeyInitialVector initial vector (first bytes or by convention).In file from the deviceKeyValue + deviceKeyInitialVector will form the cipher value.
     */
    public ImportKeyRequest(String wrapperKeyLabel, AsymmetricAlgorithm wrapperKeyAlgorithm, byte[] encryptedTransportKey, SymmetricAlgorithm transportKeyAlgorithm, byte[] deviceKeyValue, byte[] deviceKeyInitialVector, HsmKeyType hsmKeyType) {
        this.wrapperKeyLabel = wrapperKeyLabel;
        this.wrapperKeyAlgorithm = wrapperKeyAlgorithm;
        this.encryptedTransportKey = encryptedTransportKey;
        this.deviceKeyAlgorhitm = transportKeyAlgorithm;
        this.deviceKeyValue = deviceKeyValue;
        this.deviceKeyInitialVector = deviceKeyInitialVector;
        this.hsmKeyType = hsmKeyType;
    }

    public AsymmetricAlgorithm getWrapperKeyAlgorithm() {
        return wrapperKeyAlgorithm;
    }

    public SymmetricAlgorithm getDeviceKeyAlgorhitm() {
        return deviceKeyAlgorhitm;
    }

    /**
     * @return label to be used for encryption of device key before storage.
     */
    public String getStorageLabel() {
        return hsmKeyType.getLabel();
    }

    /**
     * @param hsmConfiguration
     * @return label used to encrypt/decrypt transport/wrapper key. This is returned based on label inside file but mapped to real one in HSM if mapped,
     * otherwise same value as the one in file is returned.
     * @throws HsmBaseException if underlying configuration for HSM is not available
     */
    private String getWrapperLabel(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        return hsmConfiguration.map(wrapperKeyLabel);
    }

    /**
     * @param hsmConfiguration
     * @return label that was used to encrypt wrapper key (linked to AsymmetricAlgorithm). Label returned is based on label present in file
     * but mapped according to HSM configuration file.
     * @throws HsmBaseException if underlying configuration for HSM is not available
     */
    public KeyLabel getWrapLabel(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        return new KeyLabel(getWrapperLabel(hsmConfiguration));
    }


    public HsmKeyType getHsmKeyType() {
        return this.hsmKeyType;
    }

    /**
     * @return device key init vector (see constructor)
     */
    public byte[] getDeviceKeyInitVector() {
        return deviceKeyInitialVector;
    }

    public byte[] getEncryptedDeviceKey() {
        return deviceKeyValue;
    }

    public DeviceKey getDeviceKey() throws HsmBaseException {
        switch (hsmKeyType.getHsmJssKeyType()) {
            case AES:
                return new AESDeviceKey(deviceKeyInitialVector, deviceKeyAlgorhitm.getHsmSpecs().getKekEncryptionMethod(), hsmKeyType.getKeySize(), deviceKeyValue);
            case AUTHENTICATION:
                return new AuthenticationKey(deviceKeyInitialVector, deviceKeyAlgorhitm.getHsmSpecs().getKekEncryptionMethod(),  deviceKeyValue);
            case HLSECRET:
                return new HLSecret(deviceKeyInitialVector, deviceKeyAlgorhitm.getHsmSpecs().getKekEncryptionMethod(), hsmKeyType.getKeySize(), deviceKeyValue);
            default:
                throw new HsmBaseException("Unknown JSS key type");
        }
    }

    /**
     *
     * @param hsmConfiguration
     * @return wrapped transport key based on this request
     * @throws HsmBaseException if HSM configuration is not available
     */
    public TransportKey getTransportKey(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        try {
            return new TransportKey(new KeyLabel(getWrapperLabel(hsmConfiguration)), deviceKeyAlgorhitm.getKeySize(), encryptedTransportKey);
        } catch (UnsupportedKEKEncryptionMethodException e) {
            throw new HsmBaseException(e);
        }
    }
}
