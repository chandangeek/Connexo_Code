package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmConfigurationService;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.krypto.Type;
import com.elster.jupiter.hsm.model.request.DecryptRequest;
import com.elster.jupiter.hsm.model.request.EncryptRequest;
import com.elster.jupiter.hsm.model.response.DecryptResponse;
import com.elster.jupiter.hsm.model.response.EncryptResponse;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.console.HsmEncryptionServiceImpl", service = {HsmEncryptionService.class}, immediate = true)
public class HsmEncryptionServiceImpl implements HsmEncryptionService {

    private HsmConfigurationService hsmConfigService;

    @Reference
    public void setHsmConfigService(HsmConfigurationService hsmConfigService) {
        this.hsmConfigService = hsmConfigService;
    }

    @Override
    public EncryptResponse encrypt(EncryptRequest eRequest) throws HsmBaseException {
        if (hsmConfigService instanceof HsmConfigurationServiceImpl) {
            ((HsmConfigurationServiceImpl) this.hsmConfigService).checkInit();
        } else {
            throw new HsmBaseException("...");
        }
        try {
            return new EncryptResponse(getEncrypt(eRequest));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public DecryptResponse decrypt(DecryptRequest dRequest) throws HsmBaseException {
        if (hsmConfigService instanceof HsmConfigurationServiceImpl) {
            ((HsmConfigurationServiceImpl) this.hsmConfigService).checkInit();
        } else {
            throw new HsmBaseException("...");
        }
        try {
            byte[] decrypt = getDecrypt(dRequest);
            return new DecryptResponse(decrypt);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }

    }

    private byte[] getEncrypt(EncryptRequest eReq) throws FunctionFailedException, HsmBaseException {
        if (Type.SYMMETRIC.equals(eReq.getAlgorithm().getType())) {
            return Symmetric.encrypt(new KeyLabel(eReq.getKeyLabel()), KeyDerivation.FIXED_KEY_ARRAY, eReq.getBytes(), null, eReq.getAlgorithm()
                    .getHsmSpecs()
                    .getPaddingAlgorithm(), eReq.getAlgorithm().getHsmSpecs().getChainingMode()).getData();
        }
        return Asymmetric.encrypt(new KeyLabel(eReq.getKeyLabel()), eReq.getBytes(), eReq.getAlgorithm().getHsmSpecs().getPaddingAlgorithm());
    }

    private byte[] getDecrypt(DecryptRequest dReq) throws FunctionFailedException, HsmBaseException {
        if (Type.SYMMETRIC.equals(dReq.getAlgorithm().getType())) {
            return Symmetric.decrypt(new KeyLabel(dReq.getKeyLabel()), KeyDerivation.FIXED_KEY_ARRAY, dReq.getBytes(), null, dReq.getAlgorithm()
                    .getHsmSpecs()
                    .getPaddingAlgorithm(), dReq.getAlgorithm().getHsmSpecs().getChainingMode());
        }
        return Asymmetric.decrypt(new KeyLabel(dReq.getKeyLabel()), dReq.getBytes(), dReq.getAlgorithm().getHsmSpecs().getPaddingAlgorithm());
    }


}
