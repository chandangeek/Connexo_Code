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
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TrustedCertificateImpl extends AbstractCertificateWrapperImpl implements TrustedCertificate {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    private byte[] latestCrl;
    private Reference<TrustStore> trustStoreReference = Reference.empty();

    @Inject
    public TrustedCertificateImpl(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, EventService eventService) {
        super(dataModel, thesaurus, propertySpecService, eventService);
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
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

    @Override
    public void setProperties(Map<String, Object> properties) {
        super.setProperties(properties);
        EnumSet.allOf(Properties.class).forEach(p -> p.copyFromMap(properties, this));
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        EnumSet.allOf(Properties.class).forEach(p -> p.copyToMap(properties, this));
        return properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getPropertySpecs();
        EnumSet.allOf(Properties.class)
                .stream().map(properties -> properties.asPropertySpec(propertySpecService, thesaurus)).forEach(propertySpecs::add);
        return propertySpecs;
    }

    public enum Properties {
        TRUSTSTORE("trustStore") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), TranslationKeys.TRUSTSTORE).fromThesaurus(thesaurus)
                        .finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, TrustedCertificate certificateWrapper) {
                // not allowed
            }

            @Override
            void copyToMap(Map<String, Object> properties, TrustedCertificate certificateWrapper) {
                properties.put(getPropertyName(), certificateWrapper.getTrustStore().getName());
            }
        },
        ;

        private final String propertyName;

        Properties(String propertyName) {
            this.propertyName = propertyName;
        }

        abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus);
        abstract void copyFromMap(Map<String, Object> properties, TrustedCertificate certificateWrapper);
        abstract void copyToMap(Map<String, Object> properties, TrustedCertificate certificateWrapper);

        String getPropertyName() {
            return propertyName;
        }
    }

}
