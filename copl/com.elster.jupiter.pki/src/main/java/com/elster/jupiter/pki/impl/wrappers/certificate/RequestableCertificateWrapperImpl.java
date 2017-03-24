/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.TranslationKeys;
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
            return doGetCSR();
        } catch (IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CSR_EXCEPTION, e);
        }
    }

    private Optional<PKCS10CertificationRequest> doGetCSR() throws IOException {
        return Optional.of(new PKCS10CertificationRequest(this.csr));
    }

    @Override
    public void setCSR(PKCS10CertificationRequest csr) {
        try {
            doSetCSR(csr);
        } catch (IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CSR_EXCEPTION, e);
        }
    }

    private void doSetCSR(PKCS10CertificationRequest csr) throws IOException {
        this.csr = csr.getEncoded();
    }

    @Override
    protected Optional<TranslationKeys> getInternalStatus() {
        Optional<TranslationKeys> internalStatus = super.getInternalStatus();
        if (internalStatus.isPresent()) {
            return internalStatus;
        } else {
            return this.getCSR().isPresent() ? Optional.of(TranslationKeys.REQUESTED) : Optional.empty();
        }
    }
}
