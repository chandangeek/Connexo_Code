/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.AliasParameterFilter;
import com.elster.jupiter.pki.CertificateUsagesFinder;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.ExpirationSupport;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.IssuerParameterFilter;
import com.elster.jupiter.pki.KeyPurpose;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.KeyUsagesParameterFilter;
import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.pki.PassphraseFactory;
import com.elster.jupiter.pki.PassphraseWrapper;
import com.elster.jupiter.pki.PrivateKeyFactory;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorTypePurposeTranslation;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.SubjectParameterFilter;
import com.elster.jupiter.pki.SymmetricAlgorithm;
import com.elster.jupiter.pki.SymmetricKeyFactory;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.accessors.AbstractSecurityAccessorImpl;
import com.elster.jupiter.pki.impl.accessors.CertificateAccessorImpl;
import com.elster.jupiter.pki.impl.accessors.SecurityAccessorTypeBuilder;
import com.elster.jupiter.pki.impl.accessors.SecurityAccessorTypeImpl;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterTranslatedProperty;
import com.elster.jupiter.pki.impl.wrappers.asymmetric.AbstractPlaintextPrivateKeyWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.certificate.AbstractCertificateWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.certificate.ClientCertificateWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.certificate.RequestableCertificateWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.certificate.TrustedCertificateImpl;
import com.elster.jupiter.pki.impl.wrappers.keypair.KeypairWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.symmetric.HsmKeyImpl;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.properties.Expiration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.upgrade.V10_4_2SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_3SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_6SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_8SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_9SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_7SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_8SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_9SimpleUpgrader;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserDirectorySecurityProvider;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "PkiService",
        service = {SecurityManagementService.class, TranslationKeyProvider.class, MessageSeedProvider.class, UserDirectorySecurityProvider.class},
        property = "name=" + SecurityManagementService.COMPONENTNAME,
        immediate = true)
public class SecurityManagementServiceImpl implements SecurityManagementService, TranslationKeyProvider, MessageSeedProvider, UserDirectorySecurityProvider {
    private static final int DEFAULT_MAX_PAGE_SIZE = 200;
    private final Map<String, PrivateKeyFactory> privateKeyFactories = new ConcurrentHashMap<>();
    private final Map<String, SymmetricKeyFactory> symmetricKeyFactories = new ConcurrentHashMap<>();
    private final Map<String, PassphraseFactory> passphraseFactories = new ConcurrentHashMap<>();
    private final Map<String, SymmetricAlgorithm> symmetricAlgorithmMap = new ConcurrentHashMap<>();
    private final List<CertificateUsagesFinder> certificateUsagesFinders = new ArrayList<>();

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Thesaurus thesaurus;
    private volatile DataVaultService dataVaultService;
    private volatile OrmService ormService;
    private volatile PropertySpecService propertySpecService;
    private volatile EventService eventService;
    private volatile UserService userService;
    private volatile QueryService queryService;
    private volatile MessageService messageService;
    private volatile FileImportService fileImportService;
    private volatile HsmEnergyService hsmEnergyService;
    private volatile HsmEncryptionService hsmEncryptionService;

    @Inject
    public SecurityManagementServiceImpl(OrmService ormService,
                                         UpgradeService upgradeService,
                                         NlsService nlsService,
                                         DataVaultService dataVaultService,
                                         PropertySpecService propertySpecService,
                                         EventService eventService,
                                         UserService userService,
                                         QueryService queryService,
                                         MessageService messageService,
                                         FileImportService fileImportService,
                                         HsmEnergyService hsmEnergyService,
                                         HsmEncryptionService hsmEncryptionService) {
        this.setOrmService(ormService);
        this.setUpgradeService(upgradeService);
        this.setNlsService(nlsService);
        this.setDataVaultService(dataVaultService);
        this.setPropertySpecService(propertySpecService);
        this.setEventService(eventService);
        this.setUserService(userService);
        this.setQueryService(queryService);
        this.setMessageService(messageService);
        this.setFileImportService(fileImportService);
        this.setHsmEnergyService(hsmEnergyService);
        this.setHsmEncryptionService(hsmEncryptionService);
        this.activate();
    }

    // OSGi constructor
    @SuppressWarnings("unused")
    public SecurityManagementServiceImpl() {
        registerSymmetricAlgorithm(new SymmetricAlgorithm() {
            @Override
            public String getCipherName() {
                return "AES/CBC/PKCS5PADDING";
            }

            @Override
            public String getIdentifier() {
                return "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
            }

            @Override
            public int getKeyLength() {
                return 32;
            }
        });
    }

    @Override
    public List<String> getKeyEncryptionMethods(CryptographicType cryptographicType) {
        switch (cryptographicType) {
            case ClientCertificate: // ClientCertificates are linked to an asymmetric key
            case AsymmetricKey:
                return privateKeyFactories.keySet().stream().sorted().collect(Collectors.toList());
            case SymmetricKey:
            case Hsm:
                return symmetricKeyFactories.keySet().stream().sorted().collect(Collectors.toList());
            case Passphrase:
                return passphraseFactories.keySet().stream().sorted().collect(Collectors.toList());
            default:
                return Collections.emptyList(); // No encryption methods for other cryptographic elements
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addPrivateKeyFactory(PrivateKeyFactory privateKeyFactory) {
        if (this.privateKeyFactories.containsKey(privateKeyFactory.getKeyEncryptionMethod())) {
            throw new DuplicateKeyEncryptionRegistration(thesaurus);
        }
        this.privateKeyFactories.put(privateKeyFactory.getKeyEncryptionMethod(), privateKeyFactory);
    }

    public void removePrivateKeyFactory(PrivateKeyFactory privateKeyFactory) {
        this.privateKeyFactories.remove(privateKeyFactory.getKeyEncryptionMethod());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSymmetricKeyFactory(SymmetricKeyFactory symmetricKeyFactory) {
        if (this.symmetricKeyFactories.containsKey(symmetricKeyFactory.getKeyEncryptionMethod())) {
            throw new DuplicateKeyEncryptionRegistration(thesaurus);
        }
        this.symmetricKeyFactories.put(symmetricKeyFactory.getKeyEncryptionMethod(), symmetricKeyFactory);
    }

    public void removeSymmetricKeyFactory(SymmetricKeyFactory symmetricKeyFactory) {
        this.symmetricKeyFactories.remove(symmetricKeyFactory.getKeyEncryptionMethod());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addPassphraseFactory(PassphraseFactory passphraseFactory) {
        if (this.passphraseFactories.containsKey(passphraseFactory.getKeyEncryptionMethod())) {
            throw new DuplicateKeyEncryptionRegistration(thesaurus);
        }
        this.passphraseFactories.put(passphraseFactory.getKeyEncryptionMethod(), passphraseFactory);
    }

    public void removePassphraseFactory(PassphraseFactory passphraseFactory) {
        this.passphraseFactories.remove(passphraseFactory.getKeyEncryptionMethod());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCertificateUsagesFinder(CertificateUsagesFinder finder) {
        certificateUsagesFinders.add(finder);
    }

    public void removeCertificateUsagesFinder(CertificateUsagesFinder finder) {
        certificateUsagesFinders.remove(finder);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = hsmEnergyService;
    }

    @Reference
    public void setHsmEncryptionService(HsmEncryptionService hsmEncryptionService) {
        this.hsmEncryptionService = hsmEncryptionService;
    }


    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Activate
    public void activate() {
        Security.addProvider(new BouncyCastleProvider());
        dataModel = ormService.newDataModel(COMPONENTNAME, "Private Key Infrastructure");
        Stream.of(TableSpecs.values()).forEach(tableSpecs -> tableSpecs.addTo(dataModel, dataVaultService));
        dataModel.register(this.getModule());
        Map<Version, Class<? extends Upgrader>> upgraders = new HashMap<>();
        upgraders.put(version(10, 4), UpgraderV10_4.class);
        upgraders.put(version(10, 4, 1), UpgraderV10_4_1.class);
        upgraders.put(version(10, 4, 2), V10_4_2SimpleUpgrader.class);
        upgraders.put(version(10, 4, 3), V10_4_3SimpleUpgrader.class);
        upgraders.put(version(10, 4, 4), V10_4_6SimpleUpgrader.class);
        upgraders.put(version(10, 4, 8), V10_4_8SimpleUpgrader.class);
        upgraders.put(version(10, 4, 9), V10_4_9SimpleUpgrader.class);
        upgraders.put(version(10, 7), V10_7SimpleUpgrader.class);
        upgraders.put(version(10, 8), V10_8SimpleUpgrader.class);
        upgraders.put(version(10,9),V10_9SimpleUpgrader.class);

        upgradeService.register(
                InstallIdentifier.identifier("Pulse", SecurityManagementService.COMPONENTNAME),
                dataModel,
                Installer.class,
                upgraders);
    }

    private AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(UpgradeService.class).toInstance(upgradeService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(SecurityManagementService.class).toInstance(SecurityManagementServiceImpl.this);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(EventService.class).toInstance(eventService);
                bind(UserService.class).toInstance(userService);
                bind(QueryService.class).toInstance(queryService);
                bind(MessageService.class).toInstance(messageService);
                bind(FileImportService.class).toInstance(fileImportService);
                bind(DataVaultService.class).toInstance(dataVaultService);
                bind(HsmEnergyService.class).toInstance(hsmEnergyService);
                bind(HsmEncryptionService.class).toInstance(hsmEncryptionService);
            }
        };
    }

    @Override
    public TrustStoreBuilder newTrustStore(String name) {
        TrustStoreImpl instance = dataModel.getInstance(TrustStoreImpl.class);
        instance.setName(name);
        return new TrustStoreBuilderImpl(instance);
    }

    @Override
    public Optional<TrustStore> findTrustStore(long id) {
        return getDataModel().mapper(TrustStore.class).getUnique("id", id);
    }

    @Override
    public Optional<TrustStore> findTrustStore(String name) {
        return getDataModel().mapper(TrustStore.class).getUnique("name", name);
    }

    @Override
    public Optional<TrustStore> findAndLockTrustStoreByIdAndVersion(long id, long version) {
        return this.getDataModel().mapper(TrustStore.class).lockObjectIfVersion(version, id);
    }

    @Override
    public List<TrustStore> getAllTrustStores() {
        return getDataModel().mapper(TrustStore.class).select(Condition.TRUE, Order.ascending(TrustStoreImpl.Fields.NAME.fieldName()).toUpperCase());
    }

    @Override
    public List<TrustStore> findTrustStores(TrustStoreFilter trustStoreFilter) {
        Condition searchCondition = Condition.TRUE;
        if (trustStoreFilter.nameContains.isPresent()) {
            searchCondition = searchCondition.and(where(TrustStoreImpl.Fields.NAME.fieldName()).isNotNull()
                    .and(where(TrustStoreImpl.Fields.NAME.fieldName()).likeIgnoreCase("*" + trustStoreFilter.nameContains.get() + "*")));
        }
        return getDataModel().mapper(TrustStore.class).select(searchCondition, Order.ascending(TrustStoreImpl.Fields.NAME.fieldName()).toUpperCase());
    }


    @Override
    public List<KeypairWrapper> getAllKeyPairs() {
        return getDataModel().mapper(KeypairWrapper.class).select(Condition.TRUE, Order.ascending(KeypairWrapperImpl.Fields.ALIAS.fieldName()).toUpperCase());
    }

    @Override
    public KeyTypeBuilder newSymmetricKeyType(String name, String keyAlgorithmName, int keySize) {
        KeyTypeImpl keyType = dataModel.getInstance(KeyTypeImpl.class);
        keyType.setName(name);
        keyType.setKeyAlgorithm(keyAlgorithmName);
        keyType.setCryptographicType(CryptographicType.SymmetricKey);
        keyType.setKeySize(keySize);
        return new KeyTypeBuilderImpl(keyType);
    }

    @Override
    public KeyTypeBuilder newHsmKeyType(String name) {
        KeyTypeImpl keyType = dataModel.getInstance(KeyTypeImpl.class);
        keyType.setName(name);
        keyType.setCryptographicType(CryptographicType.Hsm);
        return new KeyTypeBuilderImpl(keyType);
    }

    @Override
    public ClientCertificateTypeBuilder newClientCertificateType(String name, String signingAlgorithm) {
        KeyTypeImpl keyType = dataModel.getInstance(KeyTypeImpl.class);
        keyType.setCryptographicType(CryptographicType.ClientCertificate);
        keyType.setName(name);
        keyType.setSignatureAlgorithm(signingAlgorithm);
        return new ClientCertificateTypeBuilderImpl(keyType);
    }

    @Override
    public CertificateTypeBuilder newCertificateType(String name) {
        KeyTypeImpl keyType = new KeyTypeImpl(dataModel);
        keyType.setCryptographicType(CryptographicType.Certificate);
        keyType.setName(name);
        return new CertificateTypeBuilderImpl(keyType);
    }

    @Override
    public CertificateTypeBuilder newTrustedCertificateType(String name) {
        KeyTypeImpl keyType = new KeyTypeImpl(dataModel);
        keyType.setCryptographicType(CryptographicType.TrustedCertificate);
        keyType.setName(name);
        return new CertificateTypeBuilderImpl(keyType);
    }

    @Override
    public PasswordTypeBuilder newPassphraseType(String name) {
        KeyTypeImpl keyType = new KeyTypeImpl(dataModel);
        keyType.setCryptographicType(CryptographicType.Passphrase);
        keyType.setName(name);
        return new PasswordTypeBuilderImpl(keyType);
    }

    @Override
    public List<PropertySpec> getPropertySpecs(KeyType keyType, String keyEncryptionMethod) {
        switch (keyType.getCryptographicType()) {
            case Certificate:
                return getDataModel().getInstance(RequestableCertificateWrapperImpl.class).getPropertySpecs();
            case ClientCertificate:
                return getDataModel().getInstance(ClientCertificateWrapperImpl.class).getPropertySpecs();
            case TrustedCertificate:
                return getDataModel().getInstance(TrustedCertificateImpl.class).getPropertySpecs();
            case SymmetricKey:
                return getSymmetricKeyFactoryOrThrowException(keyEncryptionMethod).getPropertySpecs();
            case Passphrase:
                return getPassphraseFactoryOrThrowException(keyEncryptionMethod).getPropertySpecs();
            case AsymmetricKey:
                return Collections.emptyList(); // There is currently no need for visibility on asymmetric keys
            case Hsm:
                return getDataModel().getInstance(HsmKeyImpl.class).getPropertySpecs();
            default:
                throw new RuntimeException("A new case was added: implement it");
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs(SecurityAccessorType securityAccessorType) {
        return getPropertySpecs(securityAccessorType.getKeyType(), securityAccessorType.getKeyEncryptionMethod());
    }

    @Override
    public Optional<KeyType> getKeyType(String name) {
        return this.getDataModel().mapper(KeyType.class).getUnique(KeyTypeImpl.Fields.NAME.fieldName(), name);
    }

    @Override
    public KeyPurpose getKeyPurpose(String key) {
        return KeyPurposeImpl.from(key).asKeyPurpose(thesaurus);
    }

    @Override
    public Optional<KeyType> getKeyType(long id) {
        return this.getDataModel().mapper(KeyType.class).getUnique(KeyTypeImpl.Fields.ID.fieldName(), id);
    }

    @Override
    public List<KeyType> getKeyTypes() {
        return this.getDataModel().mapper(KeyType.class).find();
    }

    @Override
    public Finder<KeyType> findAllKeyTypes() {
        return DefaultFinder.of(KeyType.class, dataModel).defaultSortColumn(KeyTypeImpl.Fields.NAME.fieldName());
    }

    @Override
    public List<KeyPurpose> getAllKeyPurposes() {
        return Stream.of(KeyPurposeImpl.values()).map(keyPurposes -> keyPurposes.asKeyPurpose(thesaurus)).collect(Collectors.toList());
    }

    @Override
    public PrivateKeyWrapper newPrivateKeyWrapper(KeyType keyType, String keyEncryptionMethod) { // TODO remove from interface?
        if (!privateKeyFactories.containsKey(keyEncryptionMethod)) {
            throw new NoSuchKeyEncryptionMethod(thesaurus);
        }
        return privateKeyFactories.get(keyEncryptionMethod).newPrivateKeyWrapper(keyType);
    }

    @Override
    public SymmetricKeyWrapper newSymmetricKeyWrapper(SecurityAccessorType securityAccessorType) {
        return getSymmetricKeyFactoryOrThrowException(securityAccessorType.getKeyEncryptionMethod()).newSymmetricKey(securityAccessorType);
    }

    @Override
    public List<SecurityValueWrapper> getExpired(Expiration expiration, Instant when) {
        List<SecurityValueWrapper> all = new ArrayList<>();
        allFactoriesSupportingExpiration().forEach(es -> all.addAll(es.findExpired(expiration, when)));
        all.addAll(this.findExpiredCertificates(expiration, when));
        return all;
    }

    private List<ExpirationSupport> allFactoriesSupportingExpiration() {
        List<ExpirationSupport> factoriesSupportingExpiration = new ArrayList<>();
        privateKeyFactories.values().stream().filter(pkf -> pkf instanceof ExpirationSupport).map(ExpirationSupport.class::cast).forEach(factoriesSupportingExpiration::add);
        symmetricKeyFactories.values().stream().filter(skf -> skf instanceof ExpirationSupport).map(ExpirationSupport.class::cast).forEach(factoriesSupportingExpiration::add);
        passphraseFactories.values().stream().filter(ppf -> ppf instanceof ExpirationSupport).map(ExpirationSupport.class::cast).forEach(factoriesSupportingExpiration::add);
        return factoriesSupportingExpiration;
    }

    @Override
    public Optional<Comparison> getExpirationCondition(Expiration expiration, Instant when, String securityValueWrapperTableName) {
        return allFactoriesSupportingExpiration().stream().map(es -> es.isExpiredCondition(expiration, when)).filter(c -> c.getFieldName().startsWith(securityValueWrapperTableName)).findAny();
    }

    @Override
    public DeviceSecretImporter getDeviceSecretImporter(SecurityAccessorType securityAccessorType) {
        switch (securityAccessorType.getKeyType().getCryptographicType()) {
            case SymmetricKey:
                return getDeviceSecretImporterOrThrowException(securityAccessorType);
            case Passphrase:
                return getPassphraseFactoryOrThrowException(securityAccessorType.getKeyEncryptionMethod()).getDevicePassphraseImporter(securityAccessorType);
            default:
                throw new UnsupportedImportOperation(thesaurus, securityAccessorType);
        }
    }

    private DeviceSecretImporter getDeviceSecretImporterOrThrowException(SecurityAccessorType securityAccessorType) {
        SymmetricKeyFactory factory = getSymmetricKeyFactoryOrThrowException(securityAccessorType.getKeyEncryptionMethod());
        return factory.getDeviceKeyImporter(securityAccessorType);
    }

    private SymmetricKeyFactory getSymmetricKeyFactoryOrThrowException(String keyEncryptionMethod) {
        if (!symmetricKeyFactories.containsKey(keyEncryptionMethod)) {
            throw new NoSuchKeyEncryptionMethod(thesaurus);
        }
        return symmetricKeyFactories.get(keyEncryptionMethod);
    }

    @Override
    public PassphraseWrapper newPassphraseWrapper(SecurityAccessorType securityAccessorType) {
        return getPassphraseFactoryOrThrowException(securityAccessorType.getKeyEncryptionMethod()).newPassphraseWrapper(securityAccessorType);
    }

    private PassphraseFactory getPassphraseFactoryOrThrowException(String keyEncryptionMethod) {
        if (!passphraseFactories.containsKey(keyEncryptionMethod)) {
            throw new NoSuchKeyEncryptionMethod(thesaurus);
        }
        return passphraseFactories.get(keyEncryptionMethod);
    }


    @Override
    public RequestableCertificateWrapper newCertificateWrapper(String alias) {
        RequestableCertificateWrapperImpl renewableCertificate = getDataModel().getInstance(RequestableCertificateWrapperImpl.class);
        renewableCertificate.setAlias(alias);
        renewableCertificate.save();
        return renewableCertificate;
    }

    @Override
    public ClientCertificateWrapperBuilder newClientCertificateWrapper(KeyType clientCertificateKeyType, String keyEncryptionMethod) {
        AbstractPlaintextPrivateKeyWrapperImpl privateKeyWrapper = (AbstractPlaintextPrivateKeyWrapperImpl) this.newPrivateKeyWrapper(clientCertificateKeyType, keyEncryptionMethod);
        ClientCertificateWrapperImpl clientCertificate = getDataModel().getInstance(ClientCertificateWrapperImpl.class)
                .init(privateKeyWrapper, clientCertificateKeyType);
        return new ClientCertificateWrapperBuilder(clientCertificate);
    }

    @Override
    public KeypairWrapper newKeypairWrapper(String alias, KeyType keyType, String keyEncryptionMethod) {
        AbstractPlaintextPrivateKeyWrapperImpl privateKeyWrapper = (AbstractPlaintextPrivateKeyWrapperImpl) this.newPrivateKeyWrapper(keyType, keyEncryptionMethod);
        KeypairWrapperImpl keypairWrapper = getDataModel().getInstance(KeypairWrapperImpl.class).init(keyType, privateKeyWrapper);
        keypairWrapper.setAlias(alias);
        keypairWrapper.save();
        return keypairWrapper;
    }

    @Override
    public KeypairWrapper newPublicKeyWrapper(String alias, KeyType keyType) {
        KeypairWrapperImpl keypairWrapper = getDataModel().getInstance(KeypairWrapperImpl.class).init(keyType);
        keypairWrapper.setAlias(alias);
        keypairWrapper.save();
        return keypairWrapper;
    }

    @Override
    public String getComponentName() {
        return SecurityManagementService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(TranslationKeys.values()),
                Arrays.stream(Privileges.values()),
                Arrays.stream(SecurityAccessorTypePurposeTranslation.values()),
                Arrays.stream(CSRImporterTranslatedProperty.values()),
                Arrays.stream(KeyPurposeImpl.values())
        )
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }


    class ClientCertificateWrapperBuilder implements SecurityManagementService.ClientCertificateWrapperBuilder {
        private final ClientCertificateWrapper underConstruction;

        public ClientCertificateWrapperBuilder(ClientCertificateWrapper underConstruction) {
            this.underConstruction = underConstruction;
        }

        @Override
        public SecurityManagementService.ClientCertificateWrapperBuilder alias(String alias) {
            underConstruction.setAlias(alias);
            return this;
        }

        @Override
        public ClientCertificateWrapper add() {
            underConstruction.save();
            return underConstruction;
        }
    }

    class KeypairWrapperBuilder implements SecurityManagementService.KeypairWrapperBuilder {
        private final KeypairWrapper underConstruction;

        public KeypairWrapperBuilder(KeypairWrapper underConstruction) {
            this.underConstruction = underConstruction;
        }

        @Override
        public SecurityManagementService.KeypairWrapperBuilder alias(String alias) {
            underConstruction.setAlias(alias);
            return this;
        }

        @Override
        public KeypairWrapper add() {
            underConstruction.save();
            return underConstruction;
        }
    }

    @Override
    public Optional<ClientCertificateWrapper> findClientCertificateWrapper(String alias) {
        return getDataModel().mapper(ClientCertificateWrapper.class).getUnique(ClientCertificateWrapperImpl.Fields.ALIAS.fieldName(), alias);
    }

    @Override
    public Optional<ClientCertificateWrapper> findClientCertificateWrapper(long id) {
        return getDataModel().mapper(ClientCertificateWrapper.class).getUnique(ClientCertificateWrapperImpl.Fields.ID.fieldName(), id);
    }

    @Override
    public Finder<CertificateWrapper> findCertificateWrappers(Condition condition) {
        return DefaultFinder.of(CertificateWrapper.class, condition, getDataModel())
                .defaultSortColumn(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName())
                .maxPageSize(thesaurus, 1000);
    }

    @Override
    public Optional<CertificateWrapper> findCertificateWrapper(String alias) {
        List<CertificateWrapper> certificateWrappers = getDataModel().
                query(CertificateWrapper.class).
                select(where(ClientCertificateWrapperImpl.Fields.ALIAS.fieldName()).isEqualTo(alias).
                        and(where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR))));
        if (certificateWrappers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(certificateWrappers.get(0)); // There should only be one
    }

    @Override
    public Optional<CertificateWrapper> findCertificateWrapper(long id) {
        return getDataModel().mapper(CertificateWrapper.class).getUnique(AbstractCertificateWrapperImpl.Fields.ID.fieldName(), id);
    }

    @Override
    public Optional<KeypairWrapper> findKeypairWrapper(long id) {
        return getDataModel().mapper(KeypairWrapper.class).getUnique(KeypairWrapperImpl.Fields.ID.fieldName(), id);
    }

    @Override
    public Optional<KeypairWrapper> findKeypairWrapper(String alias) {
        return getDataModel().mapper(KeypairWrapper.class).getUnique(KeypairWrapperImpl.Fields.ALIAS.fieldName(), alias);
    }

    @Override
    public Optional<CertificateWrapper> findAndLockCertificateWrapper(long id, long version) {
        return getDataModel().mapper(CertificateWrapper.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Finder<CertificateWrapper> findAllCertificates() {
        return DefaultFinder.of(CertificateWrapper.class,
                where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR)),
                getDataModel()).sorted(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName(), true);
    }

    @Override
    public Optional<SymmetricAlgorithm> getSymmetricAlgorithm(String identifier) {
        return symmetricAlgorithmMap.containsKey(identifier) ?
                Optional.of(symmetricAlgorithmMap.get(identifier)) :
                Optional.empty();
    }

    @Override
    public void registerSymmetricAlgorithm(SymmetricAlgorithm symmetricAlgorithm) {
        symmetricAlgorithmMap.put(symmetricAlgorithm.getIdentifier(), new SymmetricAlgorithmImpl(symmetricAlgorithm));
    }

    @Override
    public Finder<KeypairWrapper> findAllKeypairs() {
        return DefaultFinder.of(KeypairWrapper.class, dataModel).sorted(KeypairWrapperImpl.Fields.ALIAS.fieldName(), true);
    }

    @Override
    public Optional<KeypairWrapper> findAndLockKeypairWrapper(long id, long version) {
        return getDataModel().mapper(KeypairWrapper.class).lockObjectIfVersion(version, id);
    }

    private List<CertificateWrapper> findExpiredCertificates(Expiration expiration, Instant when) {
        return dataModel.query(CertificateWrapper.class).select(
                where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR))
                        .and(expiration.isExpired("expirationTime", when)));
    }

    @Override
    public Finder<CertificateWrapper> getAliasesByFilter(AliasSearchFilter searchFilter) {
        Condition searchCondition;
        if (searchFilter.trustStore == null) {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName())
                    .likeIgnoreCase(searchFilter.alias)
                    .and(where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR)));
        } else {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName())
                    .likeIgnoreCase(searchFilter.alias)
                    .and(where(AbstractCertificateWrapperImpl.Fields.TRUST_STORE.fieldName()).isEqualTo(searchFilter.trustStore));
        }
        return DefaultFinder.of(CertificateWrapper.class,
                searchCondition, getDataModel())
                .sorted(Order.ascending(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName()).toLowerCase())
                .maxPageSize(thesaurus, DEFAULT_MAX_PAGE_SIZE);
    }

    @Override
    public Finder<CertificateWrapper> getAliasesByFilter(AliasParameterFilter searchFilter) {
        Condition searchCondition;
        if (searchFilter.trustStore == null) {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName())
                    .likeIgnoreCase(searchFilter.searchValue)
                    .and(where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR)));
        } else {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName())
                    .likeIgnoreCase(searchFilter.searchValue)
                    .and(where(AbstractCertificateWrapperImpl.Fields.TRUST_STORE.fieldName()).isEqualTo(searchFilter.trustStore));
        }
        return DefaultFinder.of(CertificateWrapper.class,
                searchCondition, getDataModel())
                .sorted(Order.ascending(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName().toLowerCase()))
                .maxPageSize(thesaurus, DEFAULT_MAX_PAGE_SIZE);
    }

    @Override
    public Finder<CertificateWrapper> getSubjectsByFilter(SubjectParameterFilter searchFilter) {
        Condition searchCondition;
        if (searchFilter.trustStore == null) {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.SUBJECT.fieldName())
                    .likeIgnoreCase(searchFilter.searchValue)
                    .and(where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR)))
                    .and(where(AbstractCertificateWrapperImpl.Fields.SUBJECT.fieldName()).isNotNull());
        } else {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.SUBJECT.fieldName())
                    .likeIgnoreCase(searchFilter.searchValue)
                    .and(where(AbstractCertificateWrapperImpl.Fields.SUBJECT.fieldName()).isNotNull())
                    .and(where(AbstractCertificateWrapperImpl.Fields.TRUST_STORE.fieldName()).isEqualTo(searchFilter.trustStore));
        }

        return DefaultFinder.of(CertificateWrapper.class,
                searchCondition, getDataModel())
                .sorted(Order.ascending(AbstractCertificateWrapperImpl.Fields.SUBJECT.fieldName()).toLowerCase())
                .maxPageSize(thesaurus, DEFAULT_MAX_PAGE_SIZE);
    }

    public Finder<CertificateWrapper> getIssuersByFilter(IssuerParameterFilter searchFilter) {
        Condition searchCondition;
        if (searchFilter.trustStore == null) {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.ISSUER.fieldName())
                    .likeIgnoreCase(searchFilter.searchValue)
                    .and(where(AbstractCertificateWrapperImpl.Fields.ISSUER.fieldName()).isNotNull())
                    .and(where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR)));

        } else {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.ISSUER.fieldName())
                    .likeIgnoreCase(searchFilter.searchValue)
                    .and(where(AbstractCertificateWrapperImpl.Fields.ISSUER.fieldName()).isNotNull())
                    .and(where(AbstractCertificateWrapperImpl.Fields.TRUST_STORE.fieldName()).isEqualTo(searchFilter.trustStore));
        }

        return DefaultFinder.of(CertificateWrapper.class,
                searchCondition, getDataModel())
                .sorted(Order.ascending(AbstractCertificateWrapperImpl.Fields.ISSUER.fieldName()).toLowerCase())
                .maxPageSize(thesaurus, DEFAULT_MAX_PAGE_SIZE);
    }

    @Override
    public Finder<CertificateWrapper> getKeyUsagesByFilter(KeyUsagesParameterFilter searchFilter) {
        Condition searchCondition;
        if (searchFilter.trustStore == null) {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.KEY_USAGES.fieldName())
                    .likeIgnoreCase(searchFilter.searchValue)
                    .and(where(AbstractCertificateWrapperImpl.Fields.KEY_USAGES.fieldName()).isNotNull())
                    .and(where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR)));
        } else {
            searchCondition = Where.where(AbstractCertificateWrapperImpl.Fields.KEY_USAGES.fieldName())
                    .likeIgnoreCase(searchFilter.searchValue)
                    .and(where(AbstractCertificateWrapperImpl.Fields.KEY_USAGES.fieldName()).isNotNull())
                    .and(where(AbstractCertificateWrapperImpl.Fields.TRUST_STORE.fieldName()).isEqualTo(searchFilter.trustStore));
        }

        return DefaultFinder.of(CertificateWrapper.class,
                searchCondition, getDataModel())
                .sorted(Order.ascending(AbstractCertificateWrapperImpl.Fields.KEY_USAGES.fieldName()).toLowerCase())
                .maxPageSize(thesaurus, DEFAULT_MAX_PAGE_SIZE);
    }

    @Override
    public Finder<CertificateWrapper> findCertificatesByFilter(DataSearchFilter dataSearchFilter) {
        return DefaultFinder.of(CertificateWrapper.class,
                getSearchCondition(dataSearchFilter), getDataModel())
                .maxPageSize(thesaurus, DEFAULT_MAX_PAGE_SIZE);
    }

    @Override
    public List<CertificateWrapper> findTrustedCertificatesByFilter(DataSearchFilter dataSearchFilter) {
        List<CertificateWrapper> trustedCertificates = new ArrayList<>();
        Query<CertificateWrapper> query = getCertificateWrapperQuery();
        List<CertificateWrapper> pagedList = query.select(getSearchCondition(dataSearchFilter));

        trustedCertificates.addAll(pagedList);
        return trustedCertificates;
    }

    @Override
    public Condition getSearchCondition(DataSearchFilter dataSearchFilter) {
        Condition searchCondition = Condition.TRUE;

        if (dataSearchFilter.alias.isPresent()) {
            searchCondition = searchCondition.and(where(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName()).isNotNull()
                    .and(where(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName()).in(dataSearchFilter.alias.get())));
        }
        if (dataSearchFilter.subject.isPresent()) {
            searchCondition = searchCondition.and(where(AbstractCertificateWrapperImpl.Fields.SUBJECT.fieldName()).isNotNull()
                    .and(where(AbstractCertificateWrapperImpl.Fields.SUBJECT.fieldName()).in(dataSearchFilter.subject.get())));
        }
        if (dataSearchFilter.issuer.isPresent()) {
            searchCondition = searchCondition.and(where(AbstractCertificateWrapperImpl.Fields.ISSUER.fieldName()).isNotNull()
                    .and(where(AbstractCertificateWrapperImpl.Fields.ISSUER.fieldName()).in(dataSearchFilter.issuer.get())));
        }
        if (dataSearchFilter.keyUsages.isPresent()) {
            Condition UsageIn = Condition.FALSE;

            for (String keyUsage : dataSearchFilter.keyUsages.get()) {
                UsageIn = UsageIn.or(where(AbstractCertificateWrapperImpl.Fields.KEY_USAGES.fieldName()).likeIgnoreCase("*" + keyUsage + "*"));
            }

            searchCondition = searchCondition
                    .and(where(AbstractCertificateWrapperImpl.Fields.KEY_USAGES.fieldName()).isNotNull()
                            .and(UsageIn));
        }
        if (dataSearchFilter.intervalFrom.isPresent() && dataSearchFilter.intervalTo.isPresent()) {
            searchCondition = searchCondition
                    .and(where(AbstractCertificateWrapperImpl.Fields.EXPIRATION.fieldName())
                            .between(dataSearchFilter.intervalFrom.get().toEpochMilli()).and(dataSearchFilter.intervalTo.get().toEpochMilli()));
        } else if (dataSearchFilter.intervalFrom.isPresent()) {
            searchCondition = searchCondition
                    .and(where(AbstractCertificateWrapperImpl.Fields.EXPIRATION.fieldName())
                            .isGreaterThanOrEqual(dataSearchFilter.intervalFrom.get().toEpochMilli()));

        } else if (dataSearchFilter.intervalTo.isPresent()) {
            searchCondition = searchCondition
                    .and(where(AbstractCertificateWrapperImpl.Fields.EXPIRATION.fieldName())
                            .isLessThanOrEqual(dataSearchFilter.intervalTo.get().toEpochMilli()));
        }

        if (dataSearchFilter.trustStore.isPresent()) {
            searchCondition = searchCondition.and(where(AbstractCertificateWrapperImpl.Fields.TRUST_STORE.fieldName()).isEqualTo(dataSearchFilter.trustStore.get()));
        } else {
            searchCondition = searchCondition.and(where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR)));
        }

        if (dataSearchFilter.aliasContains.isPresent()) {
            searchCondition = searchCondition.and(where(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName()).isNotNull()
                    .and(where(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName()).likeIgnoreCase("*" + dataSearchFilter.aliasContains.get() + "*")));
        }

        return searchCondition;
    }

    @Override
    public QueryService getQueryService() {
        return queryService;
    }

    public Query<CertificateWrapper> getCertificateWrapperQuery() {
        return getQueryService().wrap(dataModel.query(CertificateWrapper.class));
    }

    @Override
    public DirectoryCertificateUsage newDirectoryCertificateUsage(UserDirectory userDirectory) {
        return dataModel.getInstance(DirectoryCertificateUsageImpl.class).init(userDirectory);
    }

    @Override
    public Optional<DirectoryCertificateUsage> getUserDirectoryCertificateUsage(UserDirectory userDirectory) {
        return getDataModel().mapper(DirectoryCertificateUsage.class).getUnique(DirectoryCertificateUsageImpl.Fields.DIRECTORY.fieldName(), userDirectory);
    }

    @Override
    public QueryStream<DirectoryCertificateUsage> streamDirectoryCertificateUsages() {
        return dataModel.stream(DirectoryCertificateUsage.class)
                .join(TrustStore.class)
                .join(CertificateWrapper.class);
    }

    @Override
    public Optional<KeyStore> getTrustedKeyStore(LdapUserDirectory ldapUserDirectory) {
        return this.getUserDirectoryCertificateUsage(ldapUserDirectory)
                .map(directoryCertificateUsage -> {
                    KeyStore keyStore = null;
                    try {
                        keyStore = KeyStore.getInstance("JKS");
                        keyStore.load(null, null);
                        Optional<TrustStore> trustStore = directoryCertificateUsage.getTrustStore();
                        if (trustStore.isPresent()) {
                            for (TrustedCertificate cert : trustStore.get().getCertificates()) {
                                if (cert.getCertificate().isPresent()) {
                                    keyStore.setCertificateEntry(cert.getAlias(), cert.getCertificate().get());
                                }
                            }
                        }
                        return keyStore;
                    } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                        return null;
                    }
                });
    }

    @Override
    public Optional<KeyStore> getKeyStore(LdapUserDirectory ldapUserDirectory, char[] password) {
        return this.getUserDirectoryCertificateUsage(ldapUserDirectory)
                .map(directoryCertificateUsage -> {
                    KeyStore keyStore = null;
                    try {
                        keyStore = KeyStore.getInstance("JKS");
                        keyStore.load(null, password);
                        Optional<CertificateWrapper> certificate = directoryCertificateUsage.getCertificate();
                        if (certificate.isPresent() && certificate.get().getCertificate().isPresent()) {
                            X509Certificate cert = certificate.get().getCertificate().get();
                            keyStore.setCertificateEntry(certificate.get().getAlias(), cert);
                        } else {
                            return null;
                        }
                        return keyStore;
                    } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                        return null;
                    }
                });
    }

    @Override
    public SecurityAccessorType.Builder addSecurityAccessorType(String name, KeyType keyType) {
        return new SecurityAccessorTypeBuilder(dataModel, name, keyType);
    }

    @Override
    public List<SecurityAccessorType> getSecurityAccessorTypes() {
        return dataModel.mapper(SecurityAccessorType.class).find();
    }

    @Override
    public List<SecurityAccessorType> getSecurityAccessorTypes(SecurityAccessorType.Purpose purpose) {
        return dataModel.stream(SecurityAccessorType.class)
                .filter(Where.where(SecurityAccessorTypeImpl.Fields.PURPOSE.fieldName()).isEqualTo(purpose))
                .select();
    }

    @Override
    public Optional<SecurityAccessorType> findSecurityAccessorTypeById(long id) {
        return dataModel.mapper(SecurityAccessorType.class).getOptional(id);
    }

    @Override
    public Optional<SecurityAccessor> findSecurityAccessorById(long id) {
        return dataModel.mapper(SecurityAccessor.class).getOptional(id);
    }

    @Override
    public Optional<SecurityAccessorType> findSecurityAccessorTypeByName(String name) {
        return dataModel.mapper(SecurityAccessorType.class).getUnique(SecurityAccessorTypeImpl.Fields.NAME.fieldName(), name);
    }

    @Override
    public Optional<SecurityAccessorType> findAndLockSecurityAccessorType(long id, long version) {
        return dataModel.mapper(SecurityAccessorType.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<SecurityAccessor<? extends SecurityValueWrapper>> getDefaultValues(SecurityAccessorType securityAccessorType) {
        if (securityAccessorType.isManagedCentrally()) {
            return dataModel.mapper(SecurityAccessor.class).getOptional(securityAccessorType.getId())
                    .map(securityAccessor -> (SecurityAccessor<? extends SecurityValueWrapper>) securityAccessor);
        }
        return Optional.empty();
    }

    @Override
    public List<SecurityAccessor<? extends SecurityValueWrapper>> getDefaultValues(SecurityAccessorType... securityAccessorTypes) {
        List<SecurityAccessorType> typesManagedCentrally = Arrays.stream(securityAccessorTypes)
                .filter(SecurityAccessorType::isManagedCentrally)
                .collect(Collectors.toList());
        return typesManagedCentrally.isEmpty() ? Collections.emptyList() : dataModel.stream(SecurityAccessor.class)
                .filter(Where.where(AbstractSecurityAccessorImpl.Fields.KEY_ACCESSOR_TYPE.fieldName()).in(typesManagedCentrally))
                .map(sa -> (SecurityAccessor<? extends SecurityValueWrapper>) sa)
                .collect(Collectors.toList());
    }

    @Override
    public <T extends SecurityValueWrapper> SecurityAccessor<T> setDefaultValues(SecurityAccessorType securityAccessorType, T actualValue, T tempValue) {
        if (!securityAccessorType.isManagedCentrally()) {
            throw new UnsupportedOperationException("Can't set default values for security accessor type that isn't managed centrally.");
        }
        switch (securityAccessorType.getKeyType().getCryptographicType()) {
            case Certificate:
            case ClientCertificate:
            case TrustedCertificate:
                if (actualValue instanceof CertificateWrapper && (tempValue == null || tempValue instanceof CertificateWrapper)) {
                    AbstractSecurityAccessorImpl<T> certificateAccessor = (AbstractSecurityAccessorImpl<T>) dataModel.getInstance(CertificateAccessorImpl.class);
                    certificateAccessor.init(securityAccessorType);
                    certificateAccessor.setActualPassphraseWrapperReference(actualValue);
                    certificateAccessor.setTempValue(tempValue);
                    Save.CREATE.save(dataModel, certificateAccessor);
                    return certificateAccessor;
                } else {
                    throw new IllegalArgumentException("Wrong type of actual or temp value; must be " + CertificateWrapper.class.getSimpleName() + '.');
                }
            default:
                throw new UnsupportedOperationException("Default values are only supported for certificate accessor type.");
                // when adding more cases pls modify com.energyict.mdc.device.data.impl.pki.AbstractCentrallyManagedDeviceSecurityAccessor
        }
    }

    @Override
    public Optional<SecurityAccessor<? extends SecurityValueWrapper>> lockDefaultValues(SecurityAccessorType securityAccessorType, long version) {
        return dataModel.mapper(SecurityAccessor.class)
                .lockObjectIfVersion(version, securityAccessorType.getId())
                .map(securityAccessor -> (SecurityAccessor<? extends SecurityValueWrapper>) securityAccessor);
    }

    @Override
    public boolean isUsedByCertificateAccessors(CertificateWrapper certificate) {
        return streamAssociatedCertificateAccessors(certificate).findAny().isPresent();
    }

    @Override
    public List<SecurityAccessor> getAssociatedCertificateAccessors(CertificateWrapper certificate) {
        return streamAssociatedCertificateAccessors(certificate).collect(Collectors.toList());
    }

    private Stream<SecurityAccessor> streamAssociatedCertificateAccessors(CertificateWrapper certificate) {
        return dataModel.stream(SecurityAccessor.class)
                .filter(Where.where(AbstractSecurityAccessorImpl.Fields.CERTIFICATE_WRAPPER_ACTUAL.fieldName()).isEqualTo(certificate)
                        .or(Where.where(AbstractSecurityAccessorImpl.Fields.CERTIFICATE_WRAPPER_TEMP.fieldName()).isEqualTo(certificate)));
    }

    @Override
    public List<SecurityAccessor> getSecurityAccessors(SecurityAccessorType.Purpose purpose) {
        return dataModel.stream(SecurityAccessor.class)
                .join(SecurityAccessorType.class)
                .filter(Where.where(AbstractSecurityAccessorImpl.Fields.KEY_ACCESSOR_TYPE.fieldName() + '.' + SecurityAccessorTypeImpl.Fields.PURPOSE.fieldName())
                        .isEqualTo(purpose))
                .select();
    }

    @Override
    public List<String> getCertificateAssociatedDevicesNames(CertificateWrapper certificateWrapper) {
        List<String> names = new ArrayList<>();
        certificateUsagesFinders.forEach(finder -> names.addAll(finder.findAssociatedDevicesNames(certificateWrapper)));
        return names;
    }

    private class ClientCertificateTypeBuilderImpl implements ClientCertificateTypeBuilder {
        private final KeyTypeImpl underConstruction;

        private ClientCertificateTypeBuilderImpl(KeyTypeImpl underConstruction) {
            this.underConstruction = underConstruction;
        }

        @Override
        public ClientCertificateTypeBuilder setKeyUsages(EnumSet<KeyUsage> keyUsages) {
            this.underConstruction.setKeyUsages(keyUsages);
            return this;
        }

        @Override
        public ClientCertificateTypeBuilder setExtendedKeyUsages(EnumSet<ExtendedKeyUsage> keyUsages) {
            this.underConstruction.setExtendedKeyUsages(keyUsages);
            return this;
        }

        @Override
        public ClientCertificateTypeBuilder description(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public AsyncKeySizeBuilder RSA() {
            this.underConstruction.setKeyAlgorithm(AsymmetricKeyAlgorithms.RSA.name());
            return new AsyncKeySizeBuilderImpl();
        }

        @Override
        public AsyncKeySizeBuilder DSA() {
            this.underConstruction.setKeyAlgorithm(AsymmetricKeyAlgorithms.DSA.name());
            return new AsyncKeySizeBuilderImpl();
        }

        @Override
        public AsyncCurveBuilder ECDSA() {
            this.underConstruction.setKeyAlgorithm(AsymmetricKeyAlgorithms.ECDSA.name());
            return new AsyncCurveBuilderImpl();
        }

        private class AsyncKeySizeBuilderImpl implements AsyncKeySizeBuilder {

            @Override
            public AsyncKeySizeBuilder keySize(int keySize) {
                underConstruction.setKeySize(keySize);
                return this;
            }

            @Override
            public KeyType add() {
                underConstruction.save();
                return underConstruction;
            }
        }

        private class AsyncCurveBuilderImpl implements AsyncCurveBuilder {

            @Override
            public AsyncCurveBuilder curve(String curveName) {
                underConstruction.setCurve(curveName);
                return this;
            }

            @Override
            public KeyType add() {
                underConstruction.save();
                return underConstruction;
            }
        }
    }

    private class CertificateTypeBuilderImpl implements CertificateTypeBuilder {
        private final KeyTypeImpl underConstruction;

        private CertificateTypeBuilderImpl(KeyTypeImpl underConstruction) {
            this.underConstruction = underConstruction;
        }

        @Override
        public CertificateTypeBuilder description(String description) {
            this.underConstruction.setDescription(description);
            return this;
        }

        @Override
        public KeyType add() {
            underConstruction.save();
            return underConstruction;
        }

    }

    class KeyTypeBuilderImpl implements KeyTypeBuilder {
        private final KeyTypeImpl underConstruction;

        KeyTypeBuilderImpl(KeyTypeImpl underConstruction) {
            this.underConstruction = underConstruction;
        }

        @Override
        public KeyTypeBuilder description(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public KeyType add() {
            this.underConstruction.save();
            return underConstruction;
        }
    }

    class TrustStoreBuilderImpl implements TrustStoreBuilder {

        private final TrustStoreImpl underConstruction;

        TrustStoreBuilderImpl(TrustStoreImpl underConstruction) {
            this.underConstruction = underConstruction;
        }

        @Override
        public TrustStoreBuilder description(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public TrustStore add() {
            this.underConstruction.save();
            return underConstruction;
        }
    }

    private class PasswordTypeBuilderImpl implements PasswordTypeBuilder {
        private final KeyTypeImpl underConstruction;

        public PasswordTypeBuilderImpl(KeyTypeImpl keyType) {
            this.underConstruction = keyType;
            this.underConstruction.setUseLowerCaseCharacters(false);
            this.underConstruction.setUseUpperCaseCharacters(false);
            this.underConstruction.setUseNumbers(false);
            this.underConstruction.setUseSpecialCharacters(false);
        }

        @Override
        public PasswordTypeBuilder description(String description) {
            this.underConstruction.setDescription(description);
            return this;
        }

        @Override
        public PasswordTypeBuilder length(int length) {
            this.underConstruction.setPasswordLength(length);
            return this;
        }

        @Override
        public PasswordTypeBuilder withLowerCaseCharacters() {
            this.underConstruction.setUseLowerCaseCharacters(true);
            return this;
        }

        @Override
        public PasswordTypeBuilder withUpperCaseCharacters() {
            this.underConstruction.setUseUpperCaseCharacters(true);
            return this;
        }

        @Override
        public PasswordTypeBuilder withNumbers() {
            this.underConstruction.setUseNumbers(true);
            return this;
        }

        @Override
        public PasswordTypeBuilder withSpecialCharacters() {
            this.underConstruction.setUseSpecialCharacters(true);
            return this;
        }

        @Override
        public KeyType add() {
            this.underConstruction.save();
            return underConstruction;
        }
    }
}
