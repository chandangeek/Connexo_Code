/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class TrustedCertificateImpl extends AbstractCertificateWrapperImpl implements TrustedCertificate {

    private final Thesaurus thesaurus;
    private byte[] latestCrl;
    private Reference<TrustStore> trustStoreReference = Reference.empty();

    @Inject
    public TrustedCertificateImpl(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, EventService eventService) {
        super(dataModel, thesaurus, propertySpecService, eventService);
        this.thesaurus = thesaurus;
    }

    public TrustedCertificateImpl init(TrustStore trustStore, String alias, X509Certificate x509Certificate) {
        this.trustStoreReference.set(trustStore);
        this.setAlias(alias);
        this.setCertificate(x509Certificate);
        return this;
    }

    @Override
    public Optional<CRL> getCRL() {
        if (this.latestCrl==null || this.latestCrl.length==0) {
            return Optional.empty();
        }
        try (InputStream bytes = new ByteArrayInputStream(this.latestCrl)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return Optional.of(certificateFactory.generateCRL(bytes));
        } catch (CRLException | IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CRL_EXCEPTION, e);
        } catch (CertificateException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        }
    }

    @Override
    public void setCRL(CRL crl) {
        // TODO PERFORM CHECKS
        try {
            this.latestCrl = ((X509CRL)crl).getEncoded();
            this.save();
        } catch (CRLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TrustStore getTrustStore() {
        return trustStoreReference.get();
    }

}
