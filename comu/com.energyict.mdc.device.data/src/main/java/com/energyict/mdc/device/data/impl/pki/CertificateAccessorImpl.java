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
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import java.security.cert.CertificateEncodingException;
import java.util.Optional;

// almost copy-pasted as com.elster.jupiter.pki.impl.accessors.CertificateAccessorImpl.
// A refactoring towards usage of that class can be attempted
public class CertificateAccessorImpl extends AbstractDeviceSecurityAccessorImpl<CertificateWrapper> implements CertificateAccessor {

    private DataModel dataModel;
    private SecurityManagementService securityManagementService;
    private Thesaurus thesaurus;

    private Reference<CertificateWrapper> actualCertificate = Reference.empty();
    private Reference<CertificateWrapper> tempCertificate = Reference.empty();

    public CertificateAccessorImpl() {
        super();
    }

    @Inject
    public CertificateAccessorImpl(DataModel dataModel, SecurityManagementService securityManagementService, Thesaurus thesaurus) {
        super(securityManagementService);
        this.dataModel = dataModel;
        this.securityManagementService = securityManagementService;
        this.thesaurus = thesaurus;
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
            throw new PkiLocalizedException(thesaurus, MessageSeeds.TEMP_VALUE_NOT_SET);
        }
        if (!actualCertificate.isPresent()) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ACTUAL_VALUE_NOT_SET);
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
        ClientCertificateWrapper clientCertificateWrapper = securityManagementService.newClientCertificateWrapper(getSecurityAccessorType().getKeyType(), getSecurityAccessorType().getKeyEncryptionMethod())
                .alias(actualCertificate.get().getAlias()+"-new)")
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

    @Override
    public void delete() {
        getDevice().removeSecurityAccessor(this);
    }

    @Override
    public void save() {
        Save.UPDATE.save(dataModel, this);
    }
}
