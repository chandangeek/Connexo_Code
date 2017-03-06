/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpecService;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

public class RequestableCertificateWrapperImpl extends AbstractCertificateWrapperImpl implements RequestableCertificateWrapper {

    private final Thesaurus thesaurus;

    private byte[] csr;


    @Inject
    public RequestableCertificateWrapperImpl(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(dataModel, thesaurus, propertySpecService);
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<PKCS10CertificationRequest> getCSR() {
        if (this.csr==null || this.csr.length==0) {
            return Optional.empty();
        }
        try {
            return Optional.of(new PKCS10CertificationRequest(this.csr));
        } catch (IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CSR_EXCEPTION, e);
        }
    }


}
