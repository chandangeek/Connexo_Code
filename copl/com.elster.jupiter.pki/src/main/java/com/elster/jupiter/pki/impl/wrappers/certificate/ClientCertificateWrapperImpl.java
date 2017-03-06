/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;

public class ClientCertificateWrapperImpl extends RequestableCertificateWrapperImpl implements ClientCertificateWrapper {
    private Reference<PrivateKeyWrapper> privateKeyReference = ValueReference.absent();
    private Reference<KeyType> keyTypeReference = ValueReference.absent();

    @Inject
    public ClientCertificateWrapperImpl(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(dataModel, thesaurus, propertySpecService);
    }

    /**
     * Initializer for Client certificate wrapper
     *
     * @param alias
     * @param privateKeyWrapper The Wrapper that will contain the private key for this client certificate
     * @param keyType The type for this certificate.
     * @return The updated ClientCertificateWrapper
     */
    public ClientCertificateWrapperImpl init(String alias, PrivateKeyWrapper privateKeyWrapper, KeyType keyType) {
        this.setAlias(alias);
        this.privateKeyReference.set(privateKeyWrapper);
        this.keyTypeReference.set(keyType);
        return this;
    }

    @Override
    public PrivateKeyWrapper getPrivateKeyWrapper() {
        return privateKeyReference.get();
    }

    public void setPrivateKeyWrapper(PrivateKeyWrapper privateKeyWrapper) {
        this.privateKeyReference.set(privateKeyWrapper);
        this.save();
    }

    @Override
    public void renewValue() {
        
    }
}
