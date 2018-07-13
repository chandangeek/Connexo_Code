package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.krypto.Type;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.model.request.DecryptRequest;
import com.elster.jupiter.hsm.model.response.DecryptResponse;
import com.elster.jupiter.hsm.model.request.EncryptRequest;
import com.elster.jupiter.hsm.model.response.EncryptResponse;
import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.console.HsmEncryptionServiceImpl", service = {HsmEncryptionServiceImpl.class}, immediate = true)
public class HsmEncryptionServiceImpl implements HsmEncryptionService {

    private HsmConfigurationServiceImpl hsmConfigService;

    @Override
    public EncryptResponse encrypt(EncryptRequest eRequest) throws HsmBaseException {
        this.hsmConfigService.checkInit();
        try {
            return new EncryptResponse(getEncrypt(eRequest));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    private byte[] getEncrypt(EncryptRequest eReq) throws FunctionFailedException, HsmBaseException {
        if (Type.SYMMETRIC.equals(eReq.getAlgorithm().getType())) {
            return Symmetric.encrypt(new KeyLabel(eReq.getKeyLabel()), KeyDerivation.FIXED_KEY_ARRAY, eReq.getBytes(), null, eReq.getAlgorithm().getHsmSpecs().getPaddingAlgorithm(), eReq.getAlgorithm().getHsmSpecs().getChainingMode()).getData();
        }
        return Asymmetric.encrypt(new KeyLabel(eReq.getKeyLabel()), eReq.getBytes(), eReq.getAlgorithm().getHsmSpecs().getPaddingAlgorithm());
    }

    @Override
    public DecryptResponse decrypt(DecryptRequest dRequest) throws HsmBaseException {
        this.hsmConfigService.checkInit();
        try {
            byte[] decrypt = getDecrypt(dRequest);
            return new DecryptResponse(decrypt);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }

    }

    private byte[] getDecrypt(DecryptRequest dReq) throws FunctionFailedException, HsmBaseException {
        if (Type.SYMMETRIC.equals(dReq.getAlgorithm().getType())) {
            return Symmetric.decrypt(new KeyLabel(dReq.getKeyLabel()), KeyDerivation.FIXED_KEY_ARRAY, dReq.getBytes(), null, dReq.getAlgorithm().getHsmSpecs().getPaddingAlgorithm(), dReq.getAlgorithm().getHsmSpecs().getChainingMode());
        }
      return   Asymmetric.decrypt(new KeyLabel(dReq.getKeyLabel()), dReq.getBytes(),  dReq.getAlgorithm().getHsmSpecs().getPaddingAlgorithm());
    }


    @Reference
    public void setHsmConfigurationService(HsmConfigurationServiceImpl hsmConfigService){
        this.hsmConfigService = hsmConfigService;
    }

}
