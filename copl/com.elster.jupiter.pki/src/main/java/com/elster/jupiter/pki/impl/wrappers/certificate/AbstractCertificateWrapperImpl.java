/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.pki.impl.UniqueAlias;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
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
    public static final String CLIENT_CERTIFICATE_DISCRIMINATOR = "C";
    public static final String TRUSTED_CERTIFICATE_DISCRIMINATOR = "T";
    public static final String CERTIFICATE_DISCRIMINATOR = "R";

    private final DataModel dataModel;

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    public static final Map<String, Class<? extends CertificateWrapper>> IMPLEMENTERS =
            ImmutableMap.of(
                    CLIENT_CERTIFICATE_DISCRIMINATOR, ClientCertificateWrapperImpl.class,
                    TRUSTED_CERTIFICATE_DISCRIMINATOR, TrustedCertificateImpl.class,
                    CERTIFICATE_DISCRIMINATOR, RequestableCertificateWrapperImpl.class);

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
    public String getStatus() {
        return getInternalStatus().map(tk -> thesaurus.getFormat(tk).format()).orElse("");
    }

    /**
     * Method can be implemented by sub-classes if they wish to override/extend status
     * @return
     */
    protected Optional<TranslationKeys> getInternalStatus() {
        if (this.getCertificate().isPresent()) {
            try {
                getCertificate().get().checkValidity();
                return Optional.of(TranslationKeys.PRESENT);
            } catch (CertificateExpiredException e) {
                return Optional.of(TranslationKeys.EXPIRED);
            } catch (CertificateNotYetValidException e) {
                return Optional.of(TranslationKeys.NOT_YET_VALID);
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getAllKeyUsages() {
        try {
            if (!this.getCertificate().isPresent()) {
                return Optional.empty();
            }
            return Optional.of(doAllGetKeyUsages(this.getCertificate().get()));
        } catch (CertificateParsingException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.COULD_NOT_READ_KEY_USAGES);
        }
    }

    private String doAllGetKeyUsages(X509Certificate x509Certificate) throws CertificateParsingException {
        String joinedKeyUsages = doGetKeyUsages(x509Certificate);
        String joinedExtendedKeyUsages = doGetExtendedKeyUsages(x509Certificate);
        if (joinedExtendedKeyUsages.isEmpty()) {
            return joinedKeyUsages;
        } else {
            return joinedKeyUsages+joinedExtendedKeyUsages;
        }
    }

    private String doGetExtendedKeyUsages(X509Certificate x509Certificate) throws CertificateParsingException {
        if (x509Certificate.getExtendedKeyUsage()!=null) {
            return x509Certificate.getExtendedKeyUsage()
                    .stream()
                    .map(ExtendedKeyUsage::byOid)
                    .map(Optional::get)
                    .map(Enum::name)
                    .reduce("", (a, b) -> a + ", " + b);
        }
        return "";
    }

    private String doGetKeyUsages(X509Certificate x509Certificate) {
        EnumSet<KeyUsage> keyUsages = EnumSet.noneOf(KeyUsage.class);
        if (x509Certificate.getKeyUsage()!=null) {
            for (int index = 0; index < x509Certificate.getKeyUsage().length; index++) {
                if (x509Certificate.getKeyUsage()[index]) {
                    KeyUsage.byBitPosition(index).ifPresent(keyUsages::add);
                }
            }
        }
        return Joiner.on(", ").join(keyUsages);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
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
                if (properties.containsKey(getPropertyName())) {
                    certificateWrapper.setAlias((String) properties.get(getPropertyName()));
                }
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
