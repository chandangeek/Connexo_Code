package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.EncryptionType;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.model.DecryptRequest;
import com.elster.jupiter.hsm.model.DecryptResponse;
import com.elster.jupiter.hsm.model.EncryptRequest;
import com.elster.jupiter.hsm.model.EncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.console.HsmEncryptionServiceImpl", service = {HsmEncryptionServiceImpl.class}, immediate = true)
public class HsmEncryptionServiceImpl implements HsmEncryptionService {

    private HsmConfigurationService hsmConfigService;

    @Override
    public EncryptResponse encrypt(EncryptRequest eRequest) throws EncryptBaseException {
        this.hsmConfigService.checkInit();
        try {
            return new EncryptResponse(getEncrypt(eRequest.getBytes(), new KeyLabel(eRequest.getKeyLabel()), eRequest.getType()));
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new EncryptBaseException(e);
        }
    }

    private byte[] getEncrypt(byte[] bytes, KeyLabel keyLabel, EncryptionType etype) throws FunctionFailedException {
        if (EncryptionType.SYMMETRIC.equals(etype)) {
            return Symmetric.encrypt(keyLabel, KeyDerivation.FIXED_KEY_ARRAY, bytes, null, PaddingAlgorithm.ANSI_X9_23, ChainingMode.CBC).getData();
        }
        return Asymmetric.encrypt(keyLabel, bytes, PaddingAlgorithm.ANSI_X9_23);
    }

    @Override
    public DecryptResponse decrypt(DecryptRequest dRequest) throws EncryptBaseException {
        this.hsmConfigService.checkInit();
        try {
            byte[] decrypt = getDecrypt(dRequest.getKeyLabel(), dRequest.getBytes(), dRequest.getType(), Mapper.map(dRequest.getPaddingAlgorithm()), Mapper.map(dRequest.getChainingMode()));
            return new DecryptResponse(decrypt);
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new EncryptBaseException(e);
        }

    }

    private byte[] getDecrypt(String label, byte[] bytes, EncryptionType etype, PaddingAlgorithm pAlg, ChainingMode cMode) throws FunctionFailedException {
        if (EncryptionType.SYMMETRIC.equals(etype)) {
            return Symmetric.decrypt(new KeyLabel(label), KeyDerivation.FIXED_KEY_ARRAY, bytes, null, pAlg, cMode);
        }
      return   Asymmetric.decrypt(new KeyLabel(label), bytes,  PaddingAlgorithm.ANSI_X9_23);
    }


    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigService){
        this.hsmConfigService = hsmConfigService;
    }

}
