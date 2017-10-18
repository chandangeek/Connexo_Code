/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pki.CertificateFormatter;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.impl.EventType;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.pki.impl.UniqueAlias;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UniqueAlias(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.ALIAS_UNIQUE + "}")
public abstract class AbstractCertificateWrapperImpl implements CertificateWrapper, CertificateFormatter {
    public static final String CLIENT_CERTIFICATE_DISCRIMINATOR = "C";
    public static final String TRUSTED_CERTIFICATE_DISCRIMINATOR = "T";
    public static final String CERTIFICATE_DISCRIMINATOR = "R";

    private final DataModel dataModel;
    private final EventService eventService;

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
        LAST_READ_DATE("lastReadDate"),
        ALIAS("alias"),
        ID("id"),
        SUBJECT("subject"),
        ISSUER("issuer"),
        KEY_USAGES("keyUsagesCsv"),
        EXT_KEY_USAGES("extendedKeyUsagesCsv");

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    private final Map<String, Integer> rdsOrder = new HashMap<>();
    private final ExceptionFactory exceptionFactory;

    private long id;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String alias;
    private byte[] certificate;
    private Instant expirationTime;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String subject;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String issuer;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String keyUsagesCsv;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String extendedKeyUsagesCsv;
    private Instant lastReadDate;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    public AbstractCertificateWrapperImpl(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, EventService eventService, ExceptionFactory exceptionFactory) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.eventService = eventService;
        this.exceptionFactory = exceptionFactory;
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
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
            return Optional.of((X509Certificate) certificateFactory.generateCertificate(bytes));
        } catch (CertificateException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        } catch (IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_EXCEPTION, e);
        } catch (NoSuchProviderException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.UNKNOWN_PROVIDER, e);
        }
    }

    public void setCertificate(X509Certificate certificate) {
        try {
            this.certificate = certificate.getEncoded();
            this.expirationTime = certificate.getNotAfter().toInstant();
            this.subject = x500FormattedName(certificate.getSubjectDN().getName()).replaceAll("\\s+","");
            this.issuer = x500FormattedName(certificate.getIssuerDN().getName()).replaceAll("\\s+","");
            if (getCertificateKeyUsages(certificate).size() > 0) {
                this.keyUsagesCsv = Joiner.on(", ").join(getCertificateKeyUsages(certificate).stream().map(Enum::name).collect(toList()));
            }
            if (getCertificateExtendedKeyUsages(certificate).size() > 0) {
                this.extendedKeyUsagesCsv = Joiner.on(", ").join(getCertificateExtendedKeyUsages(certificate).stream().map(Enum::name).collect(toList()));
            }
            this.save();
        } catch (CertificateEncodingException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_ENCODING_EXCEPTION, e);
        } catch (InvalidNameException e) {
            throw exceptionFactory.newException(MessageSeeds.INVALID_DN);
        }
    }

//    String x500FormattedName(String x500Name) {
//        try {
//            return new LdapName(x500Name)
//                    .getRdns()
//                    .stream()
//                    .sorted(Comparator.comparing(rdn -> rdsOrder.getOrDefault(rdn.getType(), 7)))
//                    .map(Rdn::toString)
//                    .reduce((a, b) -> a + ", " + b)
//                    .map(X500Principal::new)
//                    .map(p -> p.getName(X500Principal.RFC1779))
//                    .get();
//        } catch (InvalidNameException e) {
//            throw exceptionFactory.newException(MessageSeeds.INVALID_DN);
//        }
//    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public String getStatus() {
        return getInternalStatus().map(tk -> thesaurus.getFormat(tk).format()).orElse("");
    }

    /**
     * Method can be implemented by sub-classes if they wish to override/extend status
     *
     * @return
     */
    protected Optional<TranslationKeys> getInternalStatus() {
        if (this.getCertificate().isPresent()) {
            try {
                getCertificate().get().checkValidity();
                return Optional.of(TranslationKeys.AVAILABLE);
            } catch (CertificateExpiredException e) {
                return Optional.of(TranslationKeys.EXPIRED);
            } catch (CertificateNotYetValidException e) {
                return Optional.of(TranslationKeys.AVAILABLE);
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
            return Optional.of(doAllGetKeyUsages());
        } catch (CertificateParsingException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.COULD_NOT_READ_KEY_USAGES);
        }
    }

    private String doAllGetKeyUsages() throws CertificateParsingException {
        List<String> collect = getKeyUsages().stream().map(Enum::name).collect(toList());
        getExtendedKeyUsages().stream().map(Enum::name).forEach(collect::add);
        return Joiner.on(", ").join(collect);
    }

    @Override
    public Set<ExtendedKeyUsage> getExtendedKeyUsages() {
        EnumSet<ExtendedKeyUsage> extendedKeyUsages = EnumSet.noneOf(ExtendedKeyUsage.class);
        if (this.getCertificate().isPresent()) {
            extendedKeyUsages.addAll(getCertificateExtendedKeyUsages(this.getCertificate().get()));
        }
        return extendedKeyUsages;
    }

    protected final Set<ExtendedKeyUsage> getCertificateExtendedKeyUsages(X509Certificate x509Certificate) {
        try {
            EnumSet<ExtendedKeyUsage> extendedKeyUsages = EnumSet.noneOf(ExtendedKeyUsage.class);
            if (x509Certificate.getExtendedKeyUsage() != null) {
                extendedKeyUsages.addAll(x509Certificate.getExtendedKeyUsage()
                        .stream()
                        .map(ExtendedKeyUsage::byOid)
                        .filter(Optional::isPresent)
                        .map(Optional::get) // TODO Find a solution for ExtendedKeyUsages we don't know (ExtendedKeyUsage as Interface)
                        .collect(toSet()));
            }
            return extendedKeyUsages;
        } catch (CertificateParsingException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CERTIFICATE_ENCODING_EXCEPTION, e);
        }
    }

    @Override
    public Set<KeyUsage> getKeyUsages() {
        EnumSet<KeyUsage> keyUsages = EnumSet.noneOf(KeyUsage.class);
        if (this.getCertificate().isPresent()) {
            keyUsages.addAll(getCertificateKeyUsages(this.getCertificate().get()));
        }
        return keyUsages;
    }

    protected final EnumSet<KeyUsage> getCertificateKeyUsages(X509Certificate certificate) {
        EnumSet<KeyUsage> keyUsages = EnumSet.noneOf(KeyUsage.class);
        if (certificate.getKeyUsage() != null) {
            for (int index = 0; index < certificate.getKeyUsage().length; index++) {
                if (certificate.getKeyUsage()[index]) {
                    KeyUsage.byBitPosition(index).ifPresent(keyUsages::add);
                }
            }
        }
        return keyUsages;
    }

    @Override
    public void delete() {
        this.eventService.postEvent(EventType.CERTIFICATE_VALIDATE_DELETE.topic(), this);
        dataModel.remove(this);
        this.eventService.postEvent(EventType.CERTIFICATE_DELETED.topic(), this);
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
        },;

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

    @Override
    public boolean hasCSR() {
        return false;
    }

    @Override
    public boolean hasPrivateKey() {
        return false;
    }

    public Optional<Instant> getLastReadDate() {
        return Optional.ofNullable(this.lastReadDate);
    }

    @Override
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    @Override
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String stringifyKeyUsages(Set<KeyUsage> keyUsages) {
        List<String> collect = keyUsages.stream().map(Enum::name).collect(toList());
        return Joiner.on(", ").join(collect);
    }

    public String getStringifiedKeyUsages() {
        return keyUsagesCsv;
    }

    public String stringifyExtendedKeyUsages(Set<ExtendedKeyUsage> extendedKeyUsages) {
        List<String> collect = extendedKeyUsages.stream().map(Enum::name).collect(toList());
        return Joiner.on(", ").join(collect);
    }

    public void setKeyUsagesCsv(String keyUsages) {
        this.keyUsagesCsv = keyUsages;
    }

    public String getKeyUsagesCsv() {
        return this.keyUsagesCsv;
    }

    public void setExtendedKeyUsagesCsv(String extendedKeyUsages) {
        this.extendedKeyUsagesCsv = extendedKeyUsages;
    }

    public String getExtendedKeyUsagesCsv() {
        return this.extendedKeyUsagesCsv;
    }

    public String getStringifiedExtendedKeyUsages() {
        return extendedKeyUsagesCsv;
    }


}
