/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.CertificateRequestData;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RequestableCertificateWrapperImpl extends AbstractCertificateWrapperImpl implements RequestableCertificateWrapper {
    private final Thesaurus thesaurus;
    private byte[] csr;

    @Inject
    public RequestableCertificateWrapperImpl(DataModel dataModel,
                                             Thesaurus thesaurus,
                                             PropertySpecService propertySpecService,
                                             EventService eventService,
                                             SecurityManagementService securityManagementService) {
        super(dataModel, thesaurus, propertySpecService, eventService, securityManagementService);
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<PKCS10CertificationRequest> getCSR() {
        if (this.csr == null || this.csr.length == 0) {
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
    public void setCertificate(X509Certificate certificate, Optional<CertificateRequestData> certificateRequestUserData) {
        validateCertificateMatchesCsr(certificate);
        super.setCertificate(certificate, certificateRequestUserData);
    }

    private void validateCertificateMatchesCsr(X509Certificate certificate) {
        getCSR().ifPresent(csr -> {
            try {
                if (!Arrays.equals(certificate.getPublicKey().getEncoded(),
                        csr.getSubjectPublicKeyInfo().getEncoded())) {
                    throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_PUBLIC_KEY_MISMATCH);
                }
                if (!new org.bouncycastle.asn1.x500.X500Name(certificate.getSubjectDN().getName()).equals(csr.getSubject())) {
                    throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_SUBJECT_DN_MISMATCH);
                }

                //key usage mismatch should warn the user, but not fail the whole validation
                try {
                    validateCertificateKeyUsagesMatchCsr(certificate, csr);
                }catch (PkiLocalizedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_PUBLIC_KEY_MISMATCH);
            }
        });
    }

    private void validateCertificateKeyUsagesMatchCsr(X509Certificate certificate, PKCS10CertificationRequest csr) {
        if (!getCsrKeyUsages(csr).containsAll(getCertificateKeyUsages(certificate))) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_KEY_USAGE_MISMATCH);
        }
        if (!this.getCsrExtendedKeyUsages(csr).containsAll(getCertificateExtendedKeyUsages(certificate))) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_EXTENDED_KEY_USAGES_MISMATCH);
        }
    }

    @Override
        public void setCSR(PKCS10CertificationRequest csr, EnumSet<KeyUsage> keyUsages, EnumSet<ExtendedKeyUsage> extendedKeyUsages) {
        try {
            doSetCSR(csr, keyUsages, extendedKeyUsages);
        } catch (IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CSR_EXCEPTION, e);
        }
    }

    @Override
    public void setCSR(byte[] encodedCsr, EnumSet<KeyUsage> keyUsages, EnumSet<ExtendedKeyUsage> extendedKeyUsages) {
        try {
            PKCS10CertificationRequest csr = new PKCS10CertificationRequest(encodedCsr);
            setCSR(csr, keyUsages, extendedKeyUsages);
        } catch (IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CSR_EXCEPTION, e);
        }
    }

    private void doSetCSR(PKCS10CertificationRequest csr, EnumSet<KeyUsage> keyUsages, EnumSet<ExtendedKeyUsage> extendedKeyUsages) throws IOException {
        this.csr = csr.getEncoded();

        setSubject(csr.getSubject().toString());
        String keyUsagesCsv = stringifyKeyUsages(keyUsages, extendedKeyUsages);
        if (keyUsagesCsv != null && !keyUsagesCsv.isEmpty()) {
            setKeyUsagesCsv(keyUsagesCsv);
        }
    }

    @Override
    protected Optional<TranslationKeys> getInternalStatus() {
        Optional<TranslationKeys> internalStatus = super.getInternalStatus();
        if (internalStatus.isPresent()) {
            return internalStatus;
        } else {
            return getCSR().map(csr -> TranslationKeys.REQUESTED);
        }
    }

    @Override
    public boolean hasCSR() {
        return getCSR().isPresent();
    }

    @Override
    public Optional<String> getAllKeyUsages() {
        if (!this.getCertificate().isPresent() && getCSR().isPresent()) {
            List<String> usages = new ArrayList<>();
            for (Attribute attribute : getCSR().get().getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                for (ASN1Encodable asn1Encodable : attribute.getAttributeValues()) {
                    Extensions extensions = Extensions.getInstance(asn1Encodable);
                    Extension keyUsageExtension = extensions.getExtension(Extension.keyUsage);
                    if (keyUsageExtension != null) {
                        KeyUsage.fromExtension(keyUsageExtension).stream().map(Enum::toString).forEach(usages::add);
                    }
                    Extension extendedKeyUsage = extensions.getExtension(Extension.extendedKeyUsage);
                    if (extendedKeyUsage != null) {
                        ExtendedKeyUsage.fromExtension(extendedKeyUsage).stream().map(Enum::toString).forEach(usages::add);
                    }
                }
            }
            return Optional.of(Joiner.on(", ").join(usages));
        } else {
            return super.getAllKeyUsages();
        }
    }

    @Override
    public Set<ExtendedKeyUsage> getExtendedKeyUsages() {
        if (!getCertificate().isPresent() && getCSR().isPresent()) {
            EnumSet<ExtendedKeyUsage> extendedKeyUsages = EnumSet.noneOf(ExtendedKeyUsage.class);
            extendedKeyUsages.addAll(getCsrExtendedKeyUsages(getCSR().get()));
            return extendedKeyUsages;
        }
        return super.getExtendedKeyUsages();
    }

    private Set<ExtendedKeyUsage> getCsrExtendedKeyUsages(PKCS10CertificationRequest certificationRequest) {
        Set<ExtendedKeyUsage> extendedKeyUsages = EnumSet.noneOf(ExtendedKeyUsage.class);
        for (Attribute attribute : certificationRequest.getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
            for (ASN1Encodable asn1Encodable : attribute.getAttributeValues()) {
                Extensions extensions = Extensions.getInstance(asn1Encodable);
                Extension extendedKeyUsage = extensions.getExtension(Extension.extendedKeyUsage);
                if (extendedKeyUsage != null) {
                    extendedKeyUsages.addAll(ExtendedKeyUsage.fromExtension(extendedKeyUsage));
                }
            }
        }
        return extendedKeyUsages;
    }

    @Override
    public Set<KeyUsage> getKeyUsages() {
        if (!getCertificate().isPresent() && getCSR().isPresent()) {
            return getCsrKeyUsages(getCSR().get());
        }
        return super.getKeyUsages();
    }

    private static Set<KeyUsage> getCsrKeyUsages(PKCS10CertificationRequest csr) {
        EnumSet<KeyUsage> keyUsages = EnumSet.noneOf(KeyUsage.class);
        for (Attribute attribute : csr.getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
            for (ASN1Encodable asn1Encodable : attribute.getAttributeValues()) {
                Extensions extensions = Extensions.getInstance(asn1Encodable);
                Extension keyUsageExtension = extensions.getExtension(Extension.keyUsage);
                if (keyUsageExtension != null) {
                    keyUsages.addAll(KeyUsage.fromExtension(keyUsageExtension));
                }
            }
        }
        return keyUsages;
    }
}
