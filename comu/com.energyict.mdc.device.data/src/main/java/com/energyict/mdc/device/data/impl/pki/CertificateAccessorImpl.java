/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.PkiService;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import java.security.cert.CertificateEncodingException;
import java.util.Optional;

public class CertificateAccessorImpl extends AbstractKeyAccessorImpl<CertificateWrapper> implements CertificateAccessor {
    private final DataModel dataModel;
    private final PkiService pkiService;
    private final Thesaurus thesaurus;

    private Reference<CertificateWrapper> actualCertificate = Reference.empty();
    private Reference<CertificateWrapper> tempCertificate = Reference.empty();

    @Inject
    public CertificateAccessorImpl(DataModel dataModel, PkiService pkiService, Thesaurus thesaurus) {
        super(pkiService);
        this.dataModel = dataModel;
        this.pkiService = pkiService;
        this.thesaurus = thesaurus;
    }

    @Override
    public CertificateWrapper getActualValue() {
        return actualCertificate.get();
    }

    @Override
    public void setActualValue(CertificateWrapper newWrapperValue) {
        this.actualCertificate.set(newWrapperValue);
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
            throw new PkiLocalizedException(thesaurus, MessageSeeds.TEMP_VALUE_NOT_SET);
        }
        if (!actualCertificate.isPresent()) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ACTUAL_VALUE_NOT_SET);
        }
        CertificateWrapper actualCertificateWrapper = actualCertificate.get();
        actualCertificate.set(tempCertificate.get());
        tempCertificate.set(actualCertificateWrapper);
        this.save();
    }

    @Override
    public void renew() {
        try {
            if (!actualCertificate.isPresent() || !actualCertificate.get().getCertificate().isPresent()) {
                throw new PkiLocalizedException(thesaurus, MessageSeeds.NO_ACTUAL_CERTIFICATE);
            }
            if (tempCertificate.isPresent()) {
                clearTempValue();
            }
            doRenewCertificate();
        } catch (CertificateEncodingException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_ENCODING_EXCEPTION);
        }
    }

    @Override
    public void clearTempValue() {
        if (tempCertificate.isPresent()) {
            tempCertificate.setNull();
            this.save();
        }
    }

    private void doRenewCertificate() throws CertificateEncodingException { // TODO can NOT renew non-ClientCertificate types
        ClientCertificateWrapper clientCertificateWrapper = pkiService.newClientCertificateWrapper(getKeyAccessorType().getKeyType(), getKeyAccessorType().getKeyEncryptionMethod())
                .alias(actualCertificate.get().getAlias()+"-new)")
                .add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();
        X500Name x500Name = getDNFromCertificate(getActualValue());
        PKCS10CertificationRequest pkcs10CertificationRequest = clientCertificateWrapper.getPrivateKeyWrapper()
                .generateCSR(x500Name, getKeyAccessorType().getKeyType().getSignatureAlgorithm());
        clientCertificateWrapper.setCSR(pkcs10CertificationRequest);
        clientCertificateWrapper.save();
        tempCertificate.set(clientCertificateWrapper);
        this.save();
    }

    private X500Name getDNFromCertificate(CertificateWrapper original) throws CertificateEncodingException {
        JcaX509CertificateHolder certificateHolder = new JcaX509CertificateHolder(original.getCertificate().get());
        return certificateHolder.getSubject();
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public void save() {
        Save.UPDATE.save(dataModel, this);
    }
}
