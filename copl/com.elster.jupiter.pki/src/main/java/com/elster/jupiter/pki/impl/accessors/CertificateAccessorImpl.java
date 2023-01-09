/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.accessors;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import java.security.cert.CertificateEncodingException;
import java.util.Optional;

public class CertificateAccessorImpl extends AbstractSecurityAccessorImpl<CertificateWrapper> {
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + com.elster.jupiter.pki.impl.MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<CertificateWrapper> actualCertificate = Reference.empty();
    private Reference<CertificateWrapper> tempCertificate = Reference.empty();

    @Inject
    public CertificateAccessorImpl(DataModel dataModel,
                                   SecurityManagementService securityManagementService,
                                   Thesaurus thesaurus,
                                   FileImportService fileImportService,
                                   EventService eventService) {
        super(securityManagementService, dataModel, fileImportService, thesaurus, eventService);
    }

    @Override
    public Optional<CertificateWrapper> getActualValue() {
        return actualCertificate.getOptional();
    }

    @Override
    public void setActualValue(CertificateWrapper newValueWrapper) {
        this.actualCertificate.set(newValueWrapper);
    }

    @Override
    public Optional<CertificateWrapper> getTempValue() {
        return tempCertificate.getOptional();
    }

    @Override
    public void setTempValue(CertificateWrapper newValueWrapper) {
        tempCertificate.set(newValueWrapper);
    }

    @Override
    public void swapValues() {
        if (!tempCertificate.isPresent()) {
            throw new PkiLocalizedException(getThesaurus(), MessageSeeds.TEMP_VALUE_NOT_SET);
        }
        if (!actualCertificate.isPresent()) {
            throw new PkiLocalizedException(getThesaurus(), MessageSeeds.ACTUAL_VALUE_NOT_SET);
        }
        CertificateWrapper actualCertificateWrapper = actualCertificate.get();
        actualCertificate.set(tempCertificate.get());
        tempCertificate.set(actualCertificateWrapper);
        super.swapValues();
        this.save();
    }

    @Override
    public void renew() {
        try {
            if (!actualCertificate.isPresent() || !actualCertificate.get().getCertificate().isPresent()) {
                throw new PkiLocalizedException(getThesaurus(), MessageSeeds.NO_ACTUAL_CERTIFICATE);
            }
            if (tempCertificate.isPresent()) {
                clearTempValue();
            }
            doRenewCertificate();
        } catch (CertificateEncodingException e) {
            throw new PkiLocalizedException(getThesaurus(), MessageSeeds.CERTIFICATE_ENCODING_EXCEPTION);
        }
    }

    @Override
    public void clearTempValue() {
        if (tempCertificate.isPresent()) {
            super.clearTempValue();
            tempCertificate.setNull();
            this.save();
        }
    }

    @Override
    public void clearActualValue() {
        if (actualCertificate.isPresent()) {
            actualCertificate.setNull();
            this.save();
        }
    }

    private void doRenewCertificate() throws CertificateEncodingException { // TODO can NOT renew non-ClientCertificate types
        ClientCertificateWrapper clientCertificateWrapper = getSecurityManagementService()
                .newClientCertificateWrapper(getSecurityAccessorType().getKeyType(), getSecurityAccessorType().getKeyEncryptionMethod())
                .alias(actualCertificate.get().getAlias() + "-new)")
                .add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();
        X500Name x500Name = getDNFromCertificate(getActualValue().get());
        PKCS10CertificationRequest pkcs10CertificationRequest = clientCertificateWrapper.getPrivateKeyWrapper()
                .generateCSR(x500Name, getSecurityAccessorType().getKeyType().getSignatureAlgorithm());
        clientCertificateWrapper.setCSR(pkcs10CertificationRequest, getSecurityAccessorType().getKeyType().getKeyUsages(), getSecurityAccessorType().getKeyType().getExtendedKeyUsages());
        clientCertificateWrapper.save();
        tempCertificate.set(clientCertificateWrapper);
        this.save();
    }

    private X500Name getDNFromCertificate(CertificateWrapper original) throws CertificateEncodingException {
        JcaX509CertificateHolder certificateHolder = new JcaX509CertificateHolder(original.getCertificate().get());
        return certificateHolder.getSubject();
    }
}
