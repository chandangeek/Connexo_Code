/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.properties.PropertySpecService;

import org.bouncycastle.asn1.x500.X500Name;

import javax.inject.Inject;

public class ClientCertificateWrapperImpl extends RequestableCertificateWrapperImpl implements ClientCertificateWrapper {
    private final DataModel dataModel;

    private RefAny privateKeyReference;
    private Reference<KeyType> keyTypeReference = ValueReference.absent();

    @Inject
    public ClientCertificateWrapperImpl(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, EventService eventService) {
        super(dataModel, thesaurus, propertySpecService, eventService);
        this.dataModel = dataModel;
    }

    /**
     * Initializer for Client certificate wrapper
     *
     * @param privateKeyWrapper The Wrapper that will contain the private key for this client certificate
     * @param keyType The type for this certificate.
     * @return The updated ClientCertificateWrapper
     */
    public ClientCertificateWrapperImpl init(PrivateKeyWrapper privateKeyWrapper, KeyType keyType) {
        this.privateKeyReference = dataModel.asRefAny(privateKeyWrapper);
        this.keyTypeReference.set(keyType);
        return this;
    }

    @Override
    public PrivateKeyWrapper getPrivateKeyWrapper() {
        return (PrivateKeyWrapper) privateKeyReference.get();
    }

    public void setPrivateKeyWrapper(PrivateKeyWrapper privateKeyWrapper) {
        this.privateKeyReference = dataModel.asRefAny(privateKeyWrapper);
        this.save();
    }

    @Override
    public KeyType getKeyType() {
        return keyTypeReference.get();
    }

    @Override
    public void generateCSR(X500Name subjectDN) {
        setCSR(getPrivateKeyWrapper().generateCSR(subjectDN, getKeyType().getSignatureAlgorithm()));
        save();
    }

    @Override
    public void delete() {
        if (privateKeyReference.isPresent()) {
            ((PrivateKeyWrapper)privateKeyReference.get()).delete();
        }
        super.delete();
    }

    @Override
    public boolean hasPrivateKey() {
        return true; // TODO getPrivateKey() should also be optional in case of empty holder
    }
}
