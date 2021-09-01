package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.HsmNotConfiguredException;
import com.elster.jupiter.hsm.model.config.HsmLabelConfiguration;
import com.atos.worldline.jss.api.FunctionTimedOutException;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.ChainingValue;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;


@Component(name = "com.elster.jupiter.impl.HsmEncryptionServiceImpl", service = {HsmEncryptionService.class}, immediate = true, property = "name=" + HsmEncryptionServiceImpl.COMPONENTNAME)
public class HsmEncryptionServiceImpl implements HsmEncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(HsmEncryptionServiceImpl.class);

    private volatile HsmConfigurationService hsmConfigurationService;

    public static final String COMPONENTNAME = "HsmEncryptionServiceImpl";


    @Override
    public byte[] symmetricEncrypt(byte[] bytes, String label) throws HsmBaseException, HsmNotConfiguredException {
        HsmLabelConfiguration hsmLabelConfiguration = hsmConfigurationService.getHsmConfiguration().get(label);
        return symmetricEncrypt(bytes, label, null, hsmLabelConfiguration.getChainingMode(), hsmLabelConfiguration.getPaddingAlgorithm());
    }

    @Override
    public byte[] symmetricEncrypt(byte[] bytes, String label, byte[] icv, ChainingMode chainingMode, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException {
        logger.debug("Symmetric encrypt:\n bytes:{}\nlabel:{}\nicv:{}\nchaining mode:{}\npadding algorithm:{}\n ", Base64.getEncoder()
                .encodeToString(bytes), label, icv, chainingMode, paddingAlgorithm);
        int retry = getTimeoutRetryCount();
        while (retry>0) {
            try {
                return Symmetric.encrypt(new KeyLabel(label), KeyDerivation.FIXED_KEY_ARRAY, bytes, getIcv(icv), paddingAlgorithm, chainingMode).getData();
            } catch (FunctionTimedOutException e){
                retry--;
                logger.warn(e.getLocalizedMessage() + "; will retry " + retry + " more times");
            } catch (FunctionFailedException e) {
                throw new HsmBaseException(e);
            }
        }
        throw new HsmBaseException("Symmetric.encrypt failed after all retries");
    }

    @Override
    public byte[] symmetricDecrypt(byte[] cipher, String label) throws HsmBaseException, HsmNotConfiguredException {
        HsmLabelConfiguration hsmLabelConfiguration = hsmConfigurationService.getHsmConfiguration().get(label);
        return symmetricDecrypt(cipher, label, null, hsmLabelConfiguration.getChainingMode(), hsmLabelConfiguration.getPaddingAlgorithm());
    }

    @Override
    public byte[] symmetricDecrypt(byte[] cipher, String label, byte[] icv, ChainingMode chainingMode, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException {
        logger.debug("Symmetric decrypt:\n bytes:{}\nlabel:{}\nicv:{}\nchaining mode:{}\npadding algorithm:{}\n ", Base64.getEncoder()
                .encodeToString(cipher), label, icv, chainingMode, paddingAlgorithm);
        int retry = getTimeoutRetryCount();
        while (retry>0) {
            try {
                return Symmetric.decrypt(new KeyLabel(label), KeyDerivation.FIXED_KEY_ARRAY, cipher, getIcv(icv), paddingAlgorithm, chainingMode);
            } catch (FunctionTimedOutException e) {
                retry--;
                logger.warn(e.getLocalizedMessage() + "; will retry " + retry + " more times");
            } catch (FunctionFailedException e) {
                throw new HsmBaseException(e);
            }
        }
        throw new HsmBaseException("Symmetric.decrypt failed after all retries");
    }

    @Override
    public byte[] asymmetricDecrypt(byte[] cipher, String label, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException {
        logger.debug("Asymmetric decrypt:\n bytes:{}\nlabel:{}\npadding algorithm:{}\n ", Base64.getEncoder().encodeToString(cipher), label, paddingAlgorithm);
        int retry = getTimeoutRetryCount();
        while (retry>0) {
            try {
                return Asymmetric.decrypt(new KeyLabel(label), cipher, paddingAlgorithm);
            } catch (FunctionTimedOutException e) {
                retry--;
                logger.warn(e.getLocalizedMessage() + "; will retry " + retry + " more times");
            } catch (FunctionFailedException e) {
                throw new HsmBaseException(e);
            }
        }
        throw new HsmBaseException("Asymmetric.decrypt failed after all retries");
    }

    @Override
    public byte[] asymmetricEncrypt(byte[] bytes, String label, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException {
        logger.debug("Asymmetric encrypt:\n bytes:{}\nlabel:{}\npadding algorithm:{}\n ", Base64.getEncoder().encodeToString(bytes), label, paddingAlgorithm);
        int retry = getTimeoutRetryCount();
        while (retry>0) {
            try {
                return Asymmetric.encrypt(new KeyLabel(label), bytes, paddingAlgorithm);
            } catch (FunctionTimedOutException e) {
                retry--;
                logger.warn(e.getLocalizedMessage() + "; will retry " + retry + " more times");
            } catch (FunctionFailedException e) {
                throw new HsmBaseException(e);
            }
        }
        throw new HsmBaseException("Asymmetric.encrypt failed after all retries");
    }

    private ChainingValue getIcv(byte[] icv) {
        if (icv == null) {
            return null;
        }
        return new ChainingValue(icv);
    }

    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigurationService) throws HsmNotConfiguredException {
        this.hsmConfigurationService = hsmConfigurationService;
    }

    private int getTimeoutRetryCount()  {
        try {
            return this.hsmConfigurationService.getHsmConfiguration().getTimeoutRetryCount();
        } catch (HsmNotConfiguredException e) {
            // do not trow exception here, other methods will do, this is just a parameter
            return HsmConfiguration.HSM_CONFIG_TIMEOUT_RETRY_COUNT_DEFAULT;
        }
    }
}
