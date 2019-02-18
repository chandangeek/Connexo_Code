package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.basecrypto.ChainingValue;
import org.osgi.service.component.annotations.Component;
import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;


@Component(name = "com.elster.jupiter.impl.HsmEncryptionServiceImpl", service = {HsmEncryptionService.class}, immediate = true, property = "name=" + HsmEncryptionServiceImpl.COMPONENTNAME)
public class HsmEncryptionServiceImpl implements HsmEncryptionService {

    public static final String COMPONENTNAME = "HsmEncryptionServiceImpl";

    public static final PaddingAlgorithm DEFAULT_PADDING = PaddingAlgorithm.EME_PKCS1_V1_5;
    public static final ChainingMode DEFAULT_CHAINING = ChainingMode.CBC;


    @Override
    public byte[] symmetricEncrypt(byte[] bytes, String label) throws HsmBaseException {
            return encrypt(bytes, label, null, DEFAULT_CHAINING, DEFAULT_PADDING);
    }

    @Override
    public byte[] encrypt(byte[] bytes, String label, byte[] icv, ChainingMode chainingMode, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException {
        try {
            return Symmetric.encrypt(new KeyLabel(label), KeyDerivation.FIXED_KEY_ARRAY, bytes, getIcv(icv), paddingAlgorithm, chainingMode).getData();
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public byte[] symmetricDecrypt(byte[] cipher, String label) throws HsmBaseException {
        return symmetricDecrypt(cipher, label, null, DEFAULT_CHAINING, DEFAULT_PADDING);
    }

    @Override
    public byte[] symmetricDecrypt(byte[] cipher, String label,byte[] icv, ChainingMode chainingMode, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException {
        try {
            return Symmetric.decrypt(new KeyLabel(label), KeyDerivation.FIXED_KEY_ARRAY, cipher, getIcv(icv), DEFAULT_PADDING, DEFAULT_CHAINING);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public byte[] asymmetricDecrypt(KeyLabel label, byte[] cipher, PaddingAlgorithm paddingAlgorithm) throws  HsmBaseException {
        try {
            return Asymmetric.decrypt(label, cipher, paddingAlgorithm);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    private ChainingValue getIcv(byte[] icv) {
        if (icv == null) {
            return null;
        }
        return new ChainingValue(icv);
    }

}
