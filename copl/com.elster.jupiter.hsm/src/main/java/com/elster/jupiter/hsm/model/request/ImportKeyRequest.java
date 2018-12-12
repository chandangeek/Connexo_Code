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
     *
     * @param wrapperKeyLabel as present in import file. This is extremely important while this class will try to map it to the real HSM label.
     * @param wrapperKeyAlgorithm algorithm used to encrypt transport key.
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


    public String getImportLabel() {
        return hsmKeyType.getLabel();
    }

    public TransportKey getTransportKey(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        try {
            return new TransportKey(new KeyLabel(mapToHsmLabel(hsmConfiguration)), deviceKeyAlgorhitm.getKeySize(), encryptedTransportKey);
        } catch (UnsupportedKEKEncryptionMethodException e) {
            throw new HsmBaseException(e);
        }
    }

    public DeviceKey getDeviceKey() throws HsmBaseException {
        switch (hsmKeyType.getHsmJssKeyType()) {
            case AES:
                return new AESDeviceKey(deviceKeyInitialVector, deviceKeyAlgorhitm.getHsmSpecs().getKekEncryptionMethod(), hsmKeyType.getKeySize(), deviceKeyValue);
            case AUTHENTICATION:
                return new AuthenticationKey(deviceKeyInitialVector, deviceKeyAlgorhitm.getHsmSpecs().getKekEncryptionMethod(), hsmKeyType.getKeySize(), deviceKeyValue);
            case HLSECRET:
                return new HLSecret(deviceKeyInitialVector, deviceKeyAlgorhitm.getHsmSpecs().getKekEncryptionMethod(), hsmKeyType.getKeySize(), deviceKeyValue);
            default:
                throw new HsmBaseException("Unknown JSS key type");
        }

    }

    public SessionKeyCapability getImportSessionCapability() {
        return hsmKeyType.getImportCapability();
    }

    private String mapToHsmLabel(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        return hsmConfiguration.map(wrapperKeyLabel);
    }
}
