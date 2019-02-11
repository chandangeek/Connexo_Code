package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.model.HsmBaseException;

import org.osgi.service.component.annotations.Component;
import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.basecrypto.SymmetricResponse;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;


@Component(name = "com.elster.jupiter.impl.HsmEncryptionServiceImpl", service = {HsmEncryptionService.class}, immediate = true, property = "name=" + HsmEncryptionServiceImpl.COMPONENTNAME)
public class HsmEncryptionServiceImpl implements HsmEncryptionService {

    public static final String COMPONENTNAME = "HsmEncryptionServiceImpl";

    public static final PaddingAlgorithm DEFAULT_PADDING = PaddingAlgorithm.PKCS;
    public static final ChainingMode DEFAULT_CHAINING = ChainingMode.CBC;


    @Override
    public byte[] encrypt(byte[] bytes, String label) throws HsmBaseException {
        try {
            SymmetricResponse encrypt = Symmetric.encrypt(new KeyLabel(label), KeyDerivation.FIXED_KEY_ARRAY, bytes, null, DEFAULT_PADDING, DEFAULT_CHAINING);
            return encrypt.getData();
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] cipher, String label) throws HsmBaseException {
        try {
            return Symmetric.decrypt(new KeyLabel(label), KeyDerivation.FIXED_KEY_ARRAY, cipher, null, DEFAULT_PADDING, DEFAULT_CHAINING);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public byte[] decrypt(KeyLabel label, byte[] cipher, PaddingAlgorithm paddingAlgorithm) throws  HsmBaseException {
        try {
            return Asymmetric.decrypt(label, cipher, paddingAlgorithm);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

}
