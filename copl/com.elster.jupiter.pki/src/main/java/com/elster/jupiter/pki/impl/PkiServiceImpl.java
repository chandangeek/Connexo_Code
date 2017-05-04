package com.elster.jupiter.pki.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.PassphraseFactory;
import com.elster.jupiter.pki.PassphraseWrapper;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PrivateKeyFactory;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.SymmetricKeyFactory;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.wrappers.asymmetric.AbstractPlaintextPrivateKeyWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.certificate.AbstractCertificateWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.certificate.ClientCertificateWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.certificate.RequestableCertificateWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.certificate.TrustedCertificateImpl;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
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
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Created by bvn on 1/26/17.
 */
@Component(name="PkiService",
        service = { PkiService.class, TranslationKeyProvider.class, MessageSeedProvider.class },
        property = "name=" + PkiService.COMPONENTNAME,
        immediate = true)
public class PkiServiceImpl implements PkiService, TranslationKeyProvider, MessageSeedProvider {

    private final Map<String, PrivateKeyFactory> privateKeyFactories = new ConcurrentHashMap<>();
    private final Map<String, SymmetricKeyFactory> symmetricKeyFactories = new ConcurrentHashMap<>();
    private final Map<String, PassphraseFactory> passphraseFactories = new ConcurrentHashMap<>();

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Thesaurus thesaurus;
    private volatile DataVaultService dataVaultService;
    private volatile OrmService ormService;
    private volatile PropertySpecService propertySpecService;
    private volatile EventService eventService;
    private volatile UserService userService;

    @Inject
    public PkiServiceImpl(OrmService ormService, UpgradeService upgradeService, NlsService nlsService,
                          DataVaultService dataVaultService, PropertySpecService propertySpecService,
                          EventService eventService, UserService userService) {
        this.setOrmService(ormService);
        this.setUpgradeService(upgradeService);
        this.setNlsService(nlsService);
        this.setDataVaultService(dataVaultService);
        this.setPropertySpecService(propertySpecService);
        this.setEventService(eventService);
        this.setUserService(userService);
        this.activate();
    }

    // OSGi constructor
    public PkiServiceImpl() {

    }

    @Override
    public List<String> getKeyEncryptionMethods(CryptographicType cryptographicType) {
        switch (cryptographicType) {
            case ClientCertificate: // ClientCertificates are linked to an asymmetric key
            case AsymmetricKey: return privateKeyFactories.keySet().stream().sorted().collect(Collectors.toList());
            case SymmetricKey: return symmetricKeyFactories.keySet().stream().sorted().collect(Collectors.toList());
            case Passphrase: return passphraseFactories.keySet().stream().sorted().collect(Collectors.toList());
            default: return Collections.emptyList(); // No encryption methods for other cryptographic elements
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

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
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

    DataModel getDataModel() {
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

    @Activate
    public void activate() {
        Security.addProvider(new BouncyCastleProvider());
        this.dataModel = ormService.newDataModel(COMPONENTNAME, "Private Key Infrastructure");
        Stream.of(TableSpecs.values()).forEach(tableSpecs -> tableSpecs.addTo(dataModel, dataVaultService));
        this.dataModel.register(this.getModule());
        upgradeService.register(InstallIdentifier.identifier("Pulse", PkiService.COMPONENTNAME), dataModel, Installer.class, Collections.emptyMap());
//        initPrivileges();
    }

    private AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(UpgradeService.class).toInstance(upgradeService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(PkiService.class).toInstance(PkiServiceImpl.this);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(EventService.class).toInstance(eventService);
                bind(UserService.class).toInstance(userService);
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
    public KeyTypeBuilder newSymmetricKeyType(String name, String keyAlgorithmName, int keySize) {
        KeyTypeImpl keyType = dataModel.getInstance(KeyTypeImpl.class);
        keyType.setName(name);
        keyType.setKeyAlgorithm(keyAlgorithmName);
        keyType.setCryptographicType(CryptographicType.SymmetricKey);
        keyType.setKeySize(keySize);
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
    public List<PropertySpec> getPropertySpecs(KeyAccessorType keyAccessorType) {
        switch (keyAccessorType.getKeyType().getCryptographicType()) {
            case Certificate:
                return getDataModel().getInstance(RequestableCertificateWrapperImpl.class).getPropertySpecs();
            case ClientCertificate:
                return getDataModel().getInstance(ClientCertificateWrapperImpl.class).getPropertySpecs();
            case TrustedCertificate:
                return getDataModel().getInstance(TrustedCertificateImpl.class).getPropertySpecs();
            case SymmetricKey:
                return getSymmetricKeyFactoryOrThrowException(keyAccessorType.getKeyEncryptionMethod()).getPropertySpecs();
            case Passphrase:
                return getPassphraseFactoryOrThrowException(keyAccessorType.getKeyEncryptionMethod()).getPropertySpecs();
            case AsymmetricKey:
                return Collections.emptyList(); // There is currently no need for visibility on asymmetric keys
            default:
                throw new RuntimeException("A new case was added: implement it");
        }
    }

    @Override
    public Optional<KeyType> getKeyType(String name) {
        return this.getDataModel().mapper(KeyType.class).getUnique(KeyTypeImpl.Fields.NAME.fieldName(), name);
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
    public PrivateKeyWrapper newPrivateKeyWrapper(KeyType keyType, String keyEncryptionMethod) { // TODO remove from interface?
        if (!privateKeyFactories.containsKey(keyEncryptionMethod)) {
            throw new NoSuchKeyEncryptionMethod(thesaurus);
        }
        return privateKeyFactories.get(keyEncryptionMethod).newPrivateKeyWrapper(keyType);
    }

    @Override
    public SymmetricKeyWrapper newSymmetricKeyWrapper(KeyAccessorType keyAccessorType) {
        return getSymmetricKeyFactoryOrThrowException(keyAccessorType.getKeyEncryptionMethod()).newSymmetricKey(keyAccessorType);
    }

    private SymmetricKeyFactory getSymmetricKeyFactoryOrThrowException(String keyEncryptionMethod) {
        if (!symmetricKeyFactories.containsKey(keyEncryptionMethod)) {
            throw new NoSuchKeyEncryptionMethod(thesaurus);
        }
        return symmetricKeyFactories.get(keyEncryptionMethod);
    }

    @Override
    public PassphraseWrapper newPassphraseWrapper(KeyAccessorType keyAccessorType) {
        return getPassphraseFactoryOrThrowException(keyAccessorType.getKeyEncryptionMethod()).newPassphraseWrapper(keyAccessorType);
    }

    private PassphraseFactory getPassphraseFactoryOrThrowException(String keyEncryptionMethod) {
        if (!passphraseFactories.containsKey(keyEncryptionMethod)) {
            throw new NoSuchKeyEncryptionMethod(thesaurus);
        }
        return passphraseFactories.get(keyEncryptionMethod);
    }


    @Override
    public CertificateWrapper newCertificateWrapper(String alias) {
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
    public String getComponentName() {
        return PkiServiceImpl.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(TranslationKeys.values()),
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    class ClientCertificateWrapperBuilder implements PkiService.ClientCertificateWrapperBuilder {
        private final ClientCertificateWrapper underConstruction;

        public ClientCertificateWrapperBuilder(ClientCertificateWrapper underConstruction) {
            this.underConstruction = underConstruction;
        }

        @Override
        public PkiService.ClientCertificateWrapperBuilder alias(String alias) {
            underConstruction.setAlias(alias);
            return this;
        }

        @Override
        public ClientCertificateWrapper add() {
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
    public Finder<CertificateWrapper> getAliasesByFilter(AliasSearchFilter searchFilter) {
        Condition searchCondition;
        if (searchFilter.trustStore ==null) {
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
                .sorted(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName(), true)
                .maxPageSize(thesaurus, 100);
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
