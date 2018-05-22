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
            return new EncryptResponse(getEncrypt(eRequest));
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new EncryptBaseException(e);
        }
    }

    private byte[] getEncrypt(EncryptRequest eReq) throws FunctionFailedException {
        if (EncryptionType.SYMMETRIC.equals(eReq.getType())) {
            return Symmetric.encrypt(new KeyLabel(eReq.getKeyLabel()), KeyDerivation.FIXED_KEY_ARRAY, eReq.getBytes(), null, Mapper.map(eReq.getPaddingAlgorithm()), Mapper.map(eReq.getChainingMode())).getData();
        }
        return Asymmetric.encrypt(new KeyLabel(eReq.getKeyLabel()), eReq.getBytes(), Mapper.map(eReq.getPaddingAlgorithm()));
    }

    @Override
    public DecryptResponse decrypt(DecryptRequest dRequest) throws EncryptBaseException {
        this.hsmConfigService.checkInit();
        try {
            byte[] decrypt = getDecrypt(dRequest);
            return new DecryptResponse(decrypt);
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new EncryptBaseException(e);
        }

    }

    private byte[] getDecrypt(DecryptRequest dReq) throws FunctionFailedException {
        if (EncryptionType.SYMMETRIC.equals(dReq.getType())) {
            return Symmetric.decrypt(new KeyLabel(dReq.getKeyLabel()), KeyDerivation.FIXED_KEY_ARRAY, dReq.getBytes(), null, Mapper.map(dReq.getPaddingAlgorithm()), Mapper.map(dReq.getChainingMode()));
        }
      return   Asymmetric.decrypt(new KeyLabel(dReq.getKeyLabel()), dReq.getBytes(),  Mapper.map(dReq.getPaddingAlgorithm()));
    }


    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigService){
        this.hsmConfigService = hsmConfigService;
    }

}
