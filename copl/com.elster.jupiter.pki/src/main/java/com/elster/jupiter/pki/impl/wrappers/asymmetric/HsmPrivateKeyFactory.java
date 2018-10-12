/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.asymmetric;

import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PrivateKeyFactory;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by H216758 on 10/11/2018.
 */
@Component(name="HsmPrivateKeyFactory", service = { PrivateKeyFactory.class }, immediate = true)
public class HsmPrivateKeyFactory implements PrivateKeyFactory {

    public static final String KEY_ENCRYPTION_METHOD = "HSM";

    @Reference
    public void setSsmModel(SoftwareSecurityDataModel ssmModel) {
    }

    @Override
    public String getKeyEncryptionMethod() {
        return KEY_ENCRYPTION_METHOD;
    }

    @Override
    public PrivateKeyWrapper newPrivateKeyWrapper(KeyType keyType) {
        return null;
    }
}
