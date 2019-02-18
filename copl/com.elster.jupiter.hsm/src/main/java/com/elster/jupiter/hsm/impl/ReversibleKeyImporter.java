package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmReversibleKey;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;


public class ReversibleKeyImporter {

    public HsmReversibleKey importKey(ImportKeyRequest importKeyRequest, HsmConfiguration hsmConfiguration, HsmEncryptionService encryptService) throws HsmBaseException {
        byte[] wrapperKey = encryptService.asymmetricDecrypt(importKeyRequest.getWrapLabel(hsmConfiguration), importKeyRequest.getTransportKey(hsmConfiguration)
                .getValue(), importKeyRequest.getWrapperKeyAlgorithm().getHsmSpecs().getPaddingAlgorithm());

        byte[] plainKey = decrypt(importKeyRequest, wrapperKey);
        return new HsmReversibleKey(plainKey, importKeyRequest.getStorageLabel());
    }


    private byte[] decrypt(ImportKeyRequest importKeyRequest, byte[] wrapperKey) throws HsmBaseException {
        try {
            Cipher cipher = importKeyRequest.getDeviceKeyAlgorhitm().newCipher();
            SecretKey secret = new SecretKeySpec(wrapperKey, importKeyRequest.getDeviceKeyAlgorhitm().getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(importKeyRequest.getDeviceKeyInitVector()));
            return cipher.doFinal(importKeyRequest.getEncryptedDeviceKey());
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new HsmBaseException(e);
        }
    }

}
