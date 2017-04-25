/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.SymmetricKeyFactory;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;
import com.elster.jupiter.properties.PropertySpec;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name="PlaintextSymmetricKeyFactory", service = SymmetricKeyFactory.class, immediate = true)
public class DataVaultSymmetricKeyFactory implements SymmetricKeyFactory {

    public static final String KEY_ENCRYPTION_METHOD = "DataVault";

    private volatile DataModel dataModel;

    // OSGi
    public DataVaultSymmetricKeyFactory() {
    }

    @Inject // Testing only
    public DataVaultSymmetricKeyFactory(SoftwareSecurityDataModel ssmModel) {
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
    public SymmetricKeyWrapper newSymmetricKey(KeyAccessorType keyAccessorType) {
        PlaintextSymmetricKeyImpl symmetricKeyWrapper = dataModel.getInstance(PlaintextSymmetricKeyImpl.class)
                .init(keyAccessorType.getKeyType(), keyAccessorType.getDuration().get());
        symmetricKeyWrapper.save();
        return symmetricKeyWrapper;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return dataModel.getInstance(PlaintextSymmetricKeyImpl.class).getPropertySpecs();
    }

}
