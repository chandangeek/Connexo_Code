/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.pki.impl.UniqueAlias;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@UniqueAlias(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.ALIAS_UNIQUE + "}")
public abstract class AbstractCertificateWrapperImpl implements CertificateWrapper {
    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public static final Map<String, Class<? extends CertificateWrapper>> IMPLEMENTERS =
            ImmutableMap.of(
                    "C", ClientCertificateWrapperImpl.class,
                    "T", TrustedCertificateImpl.class,
                    "R", RequestableCertificateWrapperImpl.class);

    public enum Fields {
        CERTIFICATE("certificate"),
        CSR("csr"),
        CRL("latestCrl"),
        PRIVATE_KEY("privateKeyReference"),
        KEY_TYPE("keyTypeReference"),
        EXPIRATION("expirationTime"),
        TRUST_STORE("trustStoreReference"),
        ALIAS("alias")
        ;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    private long id;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String alias;
    private byte[] certificate;
    private Instant expirationTime;

    public AbstractCertificateWrapperImpl(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Optional<Instant> getExpirationTime() {
        return Optional.ofNullable(expirationTime);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public Optional<X509Certificate> getCertificate() {
        if (this.certificate == null || this.certificate.length == 0) {
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
            this.save();
        } catch (CertificateEncodingException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_ENCODING_EXCEPTION, e);
        }
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        EnumSet.allOf(Properties.class).forEach(p -> p.copyFromMap(properties, this));
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        EnumSet.allOf(Properties.class).forEach(p -> p.copyToMap(properties, this));
        return properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return EnumSet.allOf(Properties.class)
                .stream().map(properties -> properties.asPropertySpec(propertySpecService, thesaurus)).collect(toList());
    }

    public enum Properties {
        CERTIFICATE_ALIAS("alias") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), TranslationKeys.ALIAS).fromThesaurus(thesaurus)
                        .finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, AbstractCertificateWrapperImpl certificateWrapper) {
                // No properties to set
            }

            @Override
            void copyToMap(Map<String, Object> properties, AbstractCertificateWrapperImpl certificateWrapper) {
                properties.put(getPropertyName(), certificateWrapper.getAlias());
            }
        },
        ;

        private final String propertyName;

        Properties(String propertyName) {
            this.propertyName = propertyName;
        }

        abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus);
        abstract void copyFromMap(Map<String, Object> properties, AbstractCertificateWrapperImpl certificateWrapper);
        abstract void copyToMap(Map<String, Object> properties, AbstractCertificateWrapperImpl certificateWrapper);

        String getPropertyName() {
            return propertyName;
        }
    }


    public void save() {
        Save.action(id).save(dataModel, this);
    }

}
