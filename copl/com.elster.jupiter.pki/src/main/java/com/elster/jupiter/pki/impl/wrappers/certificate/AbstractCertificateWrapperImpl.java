/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;

import com.google.common.collect.ImmutableMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractCertificateWrapperImpl implements CertificateWrapper {
    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    public static final Map<String, Class<? extends CertificateWrapper>> IMPLEMENTERS =
            ImmutableMap.of(
                    "C", ClientCertificateImpl.class,
                    "T", TrustedCertificateImpl.class,
                    "R", RenewableCertificateImpl.class);

    public enum Fields {
        CERTIFICATE("certificate"),
        CSR("csr"),
        CRL("latestCrl"),
        PRIVATE_KEY("privateKeyReference"),
        KEY_TYPE("keyTypeReference"),
        EXPIRATION("expirationTime"),
        TRUST_STORE("trustStoreReference");

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    private long id;
    private byte[] certificate;
    private Instant expirationTime;

    public AbstractCertificateWrapperImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<X509Certificate> getCertificate() {
        if (this.certificate==null || this.certificate.length==0) {
            return Optional.empty();
        }
        try (InputStream bytes = new ByteArrayInputStream(this.certificate)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return Optional.of((X509Certificate) certificateFactory.generateCertificate(bytes));
        } catch (CertificateException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        } catch (IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_EXCEPTION, e);
        }
    }

    public void setCertificate(X509Certificate certificate) {
        try {
            this.certificate = certificate.getEncoded();
            this.expirationTime = certificate.getNotAfter().toInstant();
        } catch (CertificateEncodingException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_ENCODING_EXCEPTION, e);
        }
    }

    @Override
    public Optional<Instant> getExpirationTime() {
        return Optional.ofNullable(expirationTime);
    }

    public void save() {
        Save.action(id).save(dataModel, this);
    }

}
