package com.elster.jupiter.hsm.model.request;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.configuration.HsmConfiguration;
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

    private final String fileLabel;
    private final AsymmetricAlgorithm transportKeyAlgorithm;
    private final byte[] transportKeyValue;
    private final int transportKeyLength;

    private final SymmetricAlgorithm deviceKeyAlgorhitm;
    private final byte[] deviceKeyValue;
    private final byte[] deviceKeyInitialVector;


    public ImportKeyRequest(@Nonnull String fileLabel, @Nonnull AsymmetricAlgorithm transportKeyAlgorithm, @Nonnull byte[] transportKeyValue, @Nonnull int transportKeyLength, @Nonnull SymmetricAlgorithm deviceKeyAlgorhitm, @Nonnull byte[] deviceKeyValue, @Nonnull byte[] deviceKeyInitialVector) {
        this.fileLabel = fileLabel;
        this.transportKeyAlgorithm = transportKeyAlgorithm;
        this.transportKeyValue = transportKeyValue;
        this.transportKeyLength = transportKeyLength;
        this.deviceKeyAlgorhitm = deviceKeyAlgorhitm;
        this.deviceKeyValue = deviceKeyValue;
        this.deviceKeyInitialVector = deviceKeyInitialVector;
    }



    public AsymmetricAlgorithm getTransportKeyAlgorithm() {
        return transportKeyAlgorithm;
    }

    public SymmetricAlgorithm getDeviceKeyAlgorhitm() {
        return deviceKeyAlgorhitm;
    }

    public String getImportLabel(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        return hsmConfiguration.get(mapToHsmLabel(hsmConfiguration)).getImportLabel();
    }

    public TransportKey getTransportKey(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        try {
            return new TransportKey(new KeyLabel(mapToHsmLabel(hsmConfiguration)), deviceKeyAlgorhitm.getKeySize(), transportKeyValue);
        } catch (UnsupportedKEKEncryptionMethodException e) {
            throw new HsmBaseException(e);
        }
    }

    public DeviceKey getDeviceKey(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        if (SymmetricAlgorithm.AES_256_CBC.equals(deviceKeyAlgorhitm)){
            String hsmLabel = mapToHsmLabel(hsmConfiguration);
            String importLabel = hsmConfiguration.get(hsmLabel).getImportLabel();
            return new AESDeviceKey(deviceKeyInitialVector, deviceKeyAlgorhitm.getHsmSpecs().getKekEncryptionMethod(), hsmConfiguration.get(importLabel).getKeyLength(), deviceKeyValue);
        }
        throw new HsmBaseException("Could not construct device key based on symmetric algorithm:" + deviceKeyAlgorhitm);
    }

    public SessionKeyCapability getImportSessionCapability(HsmConfiguration hsmConfiguration) throws HsmBaseException {
        return hsmConfiguration.get(mapToHsmLabel(hsmConfiguration)).getImportSessionKeyCapability();
    }

    private String mapToHsmLabel(HsmConfiguration HsmConfiguration) {
        return HsmConfiguration.map(fileLabel);
    }
}
