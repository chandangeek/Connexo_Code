/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.assymetric;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PrivateKeyFactory;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name="PlaintextPrivateKeyFactory", service = PrivateKeyFactory.class, immediate = true)
public class PlaintextPrivateKeyFactory implements PrivateKeyFactory {

    public static final String KEY_ENCRYPTION_METHOD = "SSM";

    private volatile DataModel dataModel;

    @Inject
    public PlaintextPrivateKeyFactory(SoftwareSecurityDataModel ssmModel) {
        this.setSsmModel(ssmModel);
    }

    @Reference
    public void setSsmModel(SoftwareSecurityDataModel ssmModel) {
        this.dataModel = ssmModel.getDataModel();
    }

    @Override
    public String getKeyEncryptionMethod() {
        return KEY_ENCRYPTION_METHOD;
    }

    @Override
    public PrivateKeyWrapper newPrivateKey(KeyAccessorType keyAccessorType) {
        switch (PkiService.AsymmetricKeyAlgorithms.valueOf(keyAccessorType.getKeyType().getAlgorithm())) {
            case ECDSA: return newEcdsaPrivateKey(keyAccessorType);
            case RSA: return newRsaPrivateKey(keyAccessorType);
            case DSA: return newDsaPrivateKey(keyAccessorType);
            default: return null;
        }
    }

    private PrivateKeyWrapper newRsaPrivateKey(KeyAccessorType keyAccessorType) {
        PlaintextRsaPrivateKey plaintextPrivateKey = dataModel.getInstance(PlaintextRsaPrivateKey.class);
        plaintextPrivateKey.init(keyAccessorType);
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    private PrivateKeyWrapper newDsaPrivateKey(KeyAccessorType keyAccessorType) {
        PlaintextDsaPrivateKey plaintextPrivateKey = dataModel.getInstance(PlaintextDsaPrivateKey.class);
        plaintextPrivateKey.init(keyAccessorType);
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    private PrivateKeyWrapper newEcdsaPrivateKey(KeyAccessorType keyAccessorType) {
        PlaintextEcdsaPrivateKey plaintextPrivateKey = dataModel.getInstance(PlaintextEcdsaPrivateKey.class);
        plaintextPrivateKey.init(keyAccessorType);
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

}
