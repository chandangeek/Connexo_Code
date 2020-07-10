/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.domain.util.AllowedChars;
import com.elster.jupiter.domain.util.HasOnlyWhiteListedCharacters;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.accessors.SecurityAccessorTypeImpl;
import com.elster.jupiter.pki.impl.wrappers.certificate.TrustedCertificateImpl;
import com.elster.jupiter.util.ShouldHaveUniqueName;
import com.elster.jupiter.util.UniqueName;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CRL;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_UNIQUE + "}")
public class TrustStoreImpl implements TrustStore, ShouldHaveUniqueName {
    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final EventService eventService;
    private final SecurityManagementService securityManagementService;
    private final FileImportService fileImportService;

    public enum Fields {
        NAME("name"),
        DESCRIPTION("description"),
        CERTIFICATES("trustedCertificates"),
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
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.TEXT_FEILD_CHARS)private String name;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.TEXTAREA_FEILD_CHARS)
    private String description;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;


    private List<TrustedCertificate> trustedCertificates = new ArrayList<>();

    @Inject
    public TrustStoreImpl(DataModel dataModel,
                          Thesaurus thesaurus,
                          EventService eventService,
                          SecurityManagementService securityManagementService,
                          FileImportService fileImportService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.eventService = eventService;
        this.securityManagementService = securityManagementService;
        this.fileImportService = fileImportService;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasUniqueName() {
        Optional<TrustStore> namesake = dataModel.mapper(TrustStore.class).getUnique("name", getName());
        if (namesake.isPresent()) {
            if (namesake.get().getId() != getId()) {
                return false;
            }

        }
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<TrustedCertificate> getCertificates() {
        return this.trustedCertificates.stream().sorted(Comparator.comparing(ts-> ts.getAlias().toLowerCase())).collect(toList());
    }

    @Override
    public TrustedCertificate addCertificate(String alias, X509Certificate x509Certificate) {
        TrustedCertificateImpl trustedCertificate = dataModel.getInstance(TrustedCertificateImpl.class);
        trustedCertificate.init(this, alias, x509Certificate);
        trustedCertificate.save();
        this.trustedCertificates.add(trustedCertificate);
        this.save();
        return trustedCertificate;
    }

    @Override
    public void removeCertificate(String alias) {
        List<TrustedCertificate> toBeRemoved = this.trustedCertificates.stream()
                .filter(trustedCertificate -> trustedCertificate.getAlias().equals(alias))
                .collect(toList());
        this.trustedCertificates.removeAll(toBeRemoved);
        toBeRemoved.stream().forEach(dataModel::remove);
    }

    @Override
    public Optional<TrustedCertificate> findCertificate(String alias) {
        return this.trustedCertificates.stream()
                .filter(trustedCertificate -> trustedCertificate.getAlias().equals(alias))
                .findAny();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void loadKeyStore(KeyStore keyStore) {
        boolean keyStoreTrustedCertificatePresent = false;
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.entryInstanceOf(alias, KeyStore.TrustedCertificateEntry.class)) {
                    Certificate entry = keyStore.getCertificate(alias);

                    Optional<TrustedCertificate> certificate = this.findCertificate(alias);
                    if (certificate.isPresent()) {
                        certificate.get().setCertificate((X509Certificate) entry, Optional.empty());
                    } else {
                        this.addCertificate(alias, (X509Certificate) entry);
                    }
                    keyStoreTrustedCertificatePresent = true;
                }
            }
            if (!keyStoreTrustedCertificatePresent) {
                throw new KeyStoreImportFailedException(thesaurus, MessageSeeds.NO_TRUSTED_CERTIFICATE_IN_KEYSTORE);
            }
        } catch (KeyStoreException e) {
            throw new KeyStoreImportFailedException(thesaurus, MessageSeeds.GENERAL_KEYSTORE_FAILURE, e);
        }
    }

    @Override
    public void validate(X509Certificate certificate) throws
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, CertificateException, CertPathValidatorException {
        Set<TrustAnchor> trustAnchors = trustedCertificates.stream()
                .filter(cert -> cert.getCertificate().isPresent())
                .map(cert -> cert.getCertificate().get())
                .map(cert -> new TrustAnchor(cert, null))
                .collect(Collectors.toSet());

        Set<CRL> crls = trustedCertificates.stream()
                .filter(cert -> cert.getCRL().isPresent())
                .map(cert -> cert.getCRL().get())
                .collect(Collectors.toSet());

        PKIXParameters pkixParameters = new PKIXParameters(trustAnchors);
        CertStoreParameters ccsp = new CollectionCertStoreParameters(crls);
        CertStore store = CertStore.getInstance("Collection", ccsp);
        pkixParameters.addCertStore(store);
        pkixParameters.setRevocationEnabled(!crls.isEmpty());

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        CertPath path = certFactory.generateCertPath(Collections.singletonList(certificate));

        CertPathValidator validator = CertPathValidator.getInstance("PKIX");    //PKIX algorithm validates CertPath objects of type X.509
        CertPathValidatorResult validate = validator.validate(path, pkixParameters);

    }

    public void save() {
        Save.action(id).save(dataModel, this);
    }

    public void delete() {
        Condition referencesThisTrustStore = Where.where(SecurityAccessorTypeImpl.Fields.TRUSTSTORE.fieldName())
                .isEqualTo(this);
        try(QueryStream<DirectoryCertificateUsage>directoryCertificateUsageQueryStream = securityManagementService.streamDirectoryCertificateUsages();
            QueryStream<SecurityAccessorTypeImpl> securityAccessorTypeQueryStream = dataModel.stream(SecurityAccessorTypeImpl.class)){
        if (securityAccessorTypeQueryStream.anyMatch(referencesThisTrustStore)) {
            throw new VetoDeleteTrustStoreException(thesaurus, MessageSeeds.TRUSTSTORE_USED_ON_SECURITY_ACCESSOR);
            }
        if (directoryCertificateUsageQueryStream
                    .filter(Where.where("trustStore").isEqualTo(this))
                    .findAny()
                    .isPresent()) {
                throw new VetoDeleteTrustStoreException(thesaurus, MessageSeeds.TRUSTSTORE_USED_BY_DIRECTORY);
            }
        }

        if (fileImportService.doImportersUse(this)) {
            throw new VetoDeleteTrustStoreException(thesaurus, MessageSeeds.TRUSTSTORE_USED_BY_IMPORT);
        }
        eventService.postEvent(EventType.TRUSTSTORE_VALIDATE_DELETE.topic(), this);
        getCertificates().forEach(TrustedCertificate::delete);
        dataModel.remove(this);
        eventService.postEvent(EventType.TRUSTSTORE_DELETED.topic(), this);
    }

    private class UntrustedCertificateException extends RuntimeException {
    }
}
