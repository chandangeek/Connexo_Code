/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.asymmetric;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PrivateKeyFactory;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name="PlaintextPrivateKeyFactory", service = { PrivateKeyFactory.class }, immediate = true)
public class DataVaultPrivateKeyFactory implements PrivateKeyFactory {

    public static final String KEY_ENCRYPTION_METHOD = "DataVault";

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;

    // OSGi
    public DataVaultPrivateKeyFactory() {
    }

    @Inject // Testing only
    public DataVaultPrivateKeyFactory(SoftwareSecurityDataModel ssmModel) {
        this.setSsmModel(ssmModel);
    }

    @Reference
    public void setSsmModel(SoftwareSecurityDataModel ssmModel) {
        this.dataModel = ssmModel.getDataModel();
    }

    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(SoftwareSecurityDataModel.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public String getKeyEncryptionMethod() {
        return KEY_ENCRYPTION_METHOD;
    }

    @Override
    public PrivateKeyWrapper newPrivateKeyWrapper(KeyType keyType) {
        try {
            switch (PkiService.AsymmetricKeyAlgorithms.valueOf(keyType.getKeyAlgorithm())) {
                case ECDSA:
                    return newEcdsaPrivateKey(keyType);
                case RSA:
                    return newRsaPrivateKey(keyType);
                case DSA:
                    return newDsaPrivateKey(keyType);
                default:
                    throw new UnsupportedAsymmetricKeyType(thesaurus, keyType.getKeyAlgorithm());
            }
        } catch (IllegalArgumentException e) {
            throw new UnsupportedAsymmetricKeyType(thesaurus, keyType.getKeyAlgorithm());
        }
    }

    private PrivateKeyWrapper newRsaPrivateKey(KeyType keyType) {
        PlaintextRsaPrivateKey plaintextPrivateKey = dataModel.getInstance(PlaintextRsaPrivateKey.class);
        plaintextPrivateKey.init(keyType);
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    private PrivateKeyWrapper newDsaPrivateKey(KeyType keyType) {
        PlaintextDsaPrivateKey plaintextPrivateKey = dataModel.getInstance(PlaintextDsaPrivateKey.class);
        plaintextPrivateKey.init(keyType);
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    private PrivateKeyWrapper newEcdsaPrivateKey(KeyType keyType) {
        PlaintextEcdsaPrivateKey plaintextPrivateKey = dataModel.getInstance(PlaintextEcdsaPrivateKey.class);
        plaintextPrivateKey.init(keyType);
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

}
