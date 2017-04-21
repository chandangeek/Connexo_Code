/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PassphraseFactory;
import com.elster.jupiter.pki.PassphraseWrapper;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;
import com.elster.jupiter.properties.PropertySpec;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name="PlaintextPassphraseFactory", service = PassphraseFactory.class, immediate = true)
public class DataVaultPassphraseFactory implements PassphraseFactory {

    public static final String KEY_ENCRYPTION_METHOD = "DataVault";

    private volatile DataModel dataModel;

    // OSGi
    public DataVaultPassphraseFactory() {
    }

    @Inject // Testing only
    public DataVaultPassphraseFactory(SoftwareSecurityDataModel ssmModel) {
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
    public PassphraseWrapper newPassphraseWrapper(KeyAccessorType keyAccessorType) {
        PlaintextPassphraseImpl plaintextPassphrase = dataModel.getInstance(PlaintextPassphraseImpl.class)
                .init(keyAccessorType.getKeyType());
        plaintextPassphrase.save();
        return plaintextPassphrase;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return dataModel.getInstance(PlaintextPassphraseImpl.class).getPropertySpecs();
    }

}
