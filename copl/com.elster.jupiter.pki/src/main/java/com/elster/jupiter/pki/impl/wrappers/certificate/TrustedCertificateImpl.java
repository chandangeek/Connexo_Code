/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Optional;

public class TrustedCertificateImpl extends AbstractCertificateWrapperImpl implements TrustedCertificate {

    private final Thesaurus thesaurus;
    private byte[] latestCrl;
    private Reference<TrustStore> trustStoreReference = Reference.empty();
    // TODO row protection

    @Inject
    public TrustedCertificateImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
        this.thesaurus = thesaurus;
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
    public TrustStore getTrustStore() {
        return trustStoreReference.get();
    }

    public TrustedCertificateImpl init(TrustStore trustStore) {
        trustStoreReference.set(trustStore);
        this.save();
        return this;
    }
}
