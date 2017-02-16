/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PrivateKeyFactory;
import com.elster.jupiter.pki.SymmetricKeyFactory;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;

@Component(name="PlaintextSymmetricKeyFactory", service = PrivateKeyFactory.class, immediate = true)
public class PlaintextSymmetricKeyFactory implements SymmetricKeyFactory {

    public static final String KEY_ENCRYPTION_METHOD = "SSM";

    private volatile DataModel dataModel;

    @Inject
    public PlaintextSymmetricKeyFactory(SoftwareSecurityDataModel ssmModel) {
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
    public SymmetricKeyWrapper newKey(KeyAccessorType keyAccessorType) throws NoSuchAlgorithmException {
        SymmetricKeyWrapperImpl symmetricKeyWrapper = new SymmetricKeyWrapperImpl();
        symmetricKeyWrapper.save();
        return symmetricKeyWrapper;
    }

}
