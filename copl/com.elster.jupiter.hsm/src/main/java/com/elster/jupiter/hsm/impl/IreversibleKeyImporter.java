package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmIrreversibleKey;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
import com.atos.worldline.jss.api.FunctionTimedOutException;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.custom.energy.Energy;
import com.atos.worldline.jss.api.custom.energy.KeyImportResponse;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKey;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IreversibleKeyImporter {

    private static final Logger logger = LoggerFactory.getLogger(IreversibleKeyImporter.class);

    public HsmIrreversibleKey importKey(ImportKeyRequest importKeyRequest, HsmConfiguration hsmConfiguration) throws HsmBaseException {
        int retry = hsmConfiguration.getTimeoutRetryCount();
        while (retry>0) {
            try {
                KeyImportResponse keyImportResponse = Energy.keyImport(importKeyRequest.getTransportKey(hsmConfiguration),
                        importKeyRequest.getWrapperKeyAlgorithm().getHsmSpecs().getPaddingAlgorithm(),
                        importKeyRequest.getDeviceKey(),
                        new KeyLabel(importKeyRequest.getStorageLabel()),
                        importKeyRequest.getHsmKeyType().getImportCapability().toProtectedSessionKeyCapability());
                ProtectedSessionKey psk = keyImportResponse.getProtectedSessionKey();
                String kekLabel = ((KeyLabel) psk.getKek()).getValue();
                return new HsmIrreversibleKey(psk.getValue(), kekLabel);
            } catch (FunctionTimedOutException e) {
                retry--;
                logger.warn(e.getLocalizedMessage() + "; will retry " + retry + " more times");
            } catch (FunctionFailedException e) {
                logger.error("Failed to import key:" + importKeyRequest);
                throw new HsmBaseException(e);
            }
        }
        throw new HsmBaseException("Energy.keyImport failed after all retries");
    }

}
