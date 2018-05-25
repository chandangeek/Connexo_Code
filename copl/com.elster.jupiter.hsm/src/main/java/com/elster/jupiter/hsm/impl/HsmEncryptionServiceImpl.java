package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.EncryptionType;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.model.request.DecryptRequest;
import com.elster.jupiter.hsm.model.response.DecryptResponse;
import com.elster.jupiter.hsm.model.request.EncryptRequest;
import com.elster.jupiter.hsm.model.response.EncryptResponse;
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

    private byte[] getEncrypt(EncryptRequest eReq) throws FunctionFailedException, EncryptBaseException {
        if (EncryptionType.SYMMETRIC.equals(eReq.getType())) {
            return Symmetric.encrypt(new KeyLabel(eReq.getKeyLabel()), KeyDerivation.FIXED_KEY_ARRAY, eReq.getBytes(), null, eReq.getPaddingAlgorithm().toJssFormat(), eReq.getChainingMode().toJssFormat()).getData();
        }
        return Asymmetric.encrypt(new KeyLabel(eReq.getKeyLabel()), eReq.getBytes(), eReq.getPaddingAlgorithm().toJssFormat());
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

    private byte[] getDecrypt(DecryptRequest dReq) throws FunctionFailedException, EncryptBaseException {
        if (EncryptionType.SYMMETRIC.equals(dReq.getType())) {
            return Symmetric.decrypt(new KeyLabel(dReq.getKeyLabel()), KeyDerivation.FIXED_KEY_ARRAY, dReq.getBytes(), null, dReq.getPaddingAlgorithm().toJssFormat(), dReq.getChainingMode().toJssFormat());
        }
      return   Asymmetric.decrypt(new KeyLabel(dReq.getKeyLabel()), dReq.getBytes(),  dReq.getPaddingAlgorithm().toJssFormat());
    }


    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigService){
        this.hsmConfigService = hsmConfigService;
    }

}
