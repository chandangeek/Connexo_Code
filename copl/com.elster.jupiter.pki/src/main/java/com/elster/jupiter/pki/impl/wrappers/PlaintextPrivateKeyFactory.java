/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PrivateKeyFactory;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.properties.PropertySpecService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by bvn on 2/14/17.
 */

@Component(name="PlaintextPrivateKeyFactory", service = PrivateKeyFactory.class, immediate = true)
public class PlaintextPrivateKeyFactory implements PrivateKeyFactory {

    static final String KEY_ENCRYPTION_METHOD = "Plaintext";

    private volatile DataVaultService dataVaultService;
    private volatile PropertySpecService propertySpecService;
    private volatile OrmService ormService;
    private volatile DataModel dataModel;

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return KEY_ENCRYPTION_METHOD;
    }

    @Override
    public PrivateKeyWrapper newPrivateKey(KeyAccessorType keyAccessorType) {
        switch (PkiService.AsymmetricKeyAlgorithms.valueOf(keyAccessorType.getKeyType().getAlgorithm())) {
            case ECDSA: return newDsaPrivateKey(keyAccessorType);
            case RSA: return newRsaPrivateKey(keyAccessorType);
            case DSA: return newEcdsaPrivateKey(keyAccessorType);
            default: return null;
        }
    }

    private PrivateKeyWrapper newDsaPrivateKey(KeyAccessorType keyAccessorType) {
        PlaintextRsaPrivateKey plaintextPrivateKey = new PlaintextRsaPrivateKey(dataVaultService, propertySpecService, dataModel);
        plaintextPrivateKey.setKeySize(keyAccessorType.getKeyType().getKeySize());
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    private PrivateKeyWrapper newRsaPrivateKey(KeyAccessorType keyAccessorType) {
        PlaintextDsaPrivateKey plaintextPrivateKey = new PlaintextDsaPrivateKey(dataVaultService, propertySpecService, dataModel);
        plaintextPrivateKey.setKeySize(keyAccessorType.getKeyType().getKeySize());
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    private PrivateKeyWrapper newEcdsaPrivateKey(KeyAccessorType keyAccessorType) {
        PlaintextEcdsaPrivateKey plaintextPrivateKey = new PlaintextEcdsaPrivateKey(dataVaultService, propertySpecService, dataModel);
        plaintextPrivateKey.setKeySize(keyAccessorType.getKeyType().getKeySize());
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    @Activate
    public void activate() {
        registerOrm();
    }

    private void registerOrm() {
        DataModel dataModel = ormService.newDataModel("PPK", "Plaintext private keys");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }
}
