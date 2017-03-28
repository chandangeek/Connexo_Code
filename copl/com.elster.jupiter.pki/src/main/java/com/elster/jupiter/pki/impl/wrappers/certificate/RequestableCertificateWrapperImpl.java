/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.common.base.Joiner;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public void setCertificate(X509Certificate certificate) {
        validateCertificateMatchesCsr(certificate);
        super.setCertificate(certificate);
    }

    private void validateCertificateMatchesCsr(X509Certificate certificate) {
        try {
            if (getCSR().isPresent()) {
                if (!Arrays.equals(certificate.getPublicKey().getEncoded(),
                        getCSR().get().getSubjectPublicKeyInfo().getEncoded())) {
                    throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_PUBLIC_KEY_MISMATCH);
                }
            }
        } catch (IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_PUBLIC_KEY_MISMATCH);
        }
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

    @Override
    public boolean hasCSR() {
        return getCSR().isPresent();
    }

    @Override
    public Optional<String> getAllKeyUsages() {
        if (!this.getCertificate().isPresent() && getCSR().isPresent()) {
            for (Attribute attribute: getCSR().get().getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                List<String> usages = new ArrayList<>();
                for (ASN1Encodable asn1Encodable : attribute.getAttributeValues()) {
                    Extensions extensions = Extensions.getInstance(asn1Encodable);
                    Extension keyUsageExtension = extensions.getExtension(Extension.keyUsage);
                    KeyUsage.fromExtension(keyUsageExtension).stream().map(Enum::toString).forEach(usages::add);

                    Extension extendedKeyUsage = extensions.getExtension(Extension.extendedKeyUsage);
                    ExtendedKeyUsage.fromExtension(extendedKeyUsage).stream().map(Enum::toString).forEach(usages::add);
                }
                return Optional.of(Joiner.on(", ").join(usages));
            }
            return Optional.empty();
        } else {
            return super.getAllKeyUsages();
        }
    }
}
