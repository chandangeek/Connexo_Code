package com.elster.jupiter.pki.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeyUsage;
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
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.conditions.Where;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn on 1/26/17.
 */
@Component(name="PkiService",
        service = PkiService.class,
        property = "name=" + PkiService.COMPONENTNAME,
        immediate = true)
public class PkiServiceImpl implements PkiService {

    private final Map<String, PrivateKeyFactory> privateKeyFactories = new ConcurrentHashMap<>();
    private final Map<String, SymmetricKeyFactory> symmetricKeyFactories = new ConcurrentHashMap<>();

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Thesaurus thesaurus;
    private volatile DataVaultService dataVaultService;
    private volatile OrmService ormService;
    private volatile PropertySpecService propertySpecService;
    private volatile EventService eventService;

    @Inject
    public PkiServiceImpl(OrmService ormService, UpgradeService upgradeService, NlsService nlsService,
                          DataVaultService dataVaultService, PropertySpecService propertySpecService,
                          EventService eventService) {
        this.setOrmService(ormService);
        this.setUpgradeService(upgradeService);
        this.setNlsService(nlsService);
        this.setDataVaultService(dataVaultService);
        this.setPropertySpecService(propertySpecService);
        this.setEventService(eventService);
        this.activate();
    }

    // OSGi constructor
    public PkiServiceImpl() {

    }

    @Override
    public List<String> getKeyEncryptionMethods(CryptographicType cryptographicType) {
        switch (cryptographicType) {
            case AsymmetricKey: return privateKeyFactories.keySet().stream().sorted().collect(Collectors.toList());
            case SymmetricKey: return symmetricKeyFactories.keySet().stream().sorted().collect(Collectors.toList());
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

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
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
        return getDataModel().mapper(TrustStore.class).find();
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
    public Optional<KeyType> getKeyType(String name) {
        return this.getDataModel().mapper(KeyType.class).getUnique("name", name);
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
    public PrivateKeyWrapper newPrivateKeyWrapper(KeyAccessorType keyAccessorType) { // TODO remove from interface?
        if (!privateKeyFactories.containsKey(keyAccessorType.getKeyEncryptionMethod())) {
            throw new NoSuchKeyEncryptionMethod(thesaurus);
        }
        return privateKeyFactories.get(keyAccessorType.getKeyEncryptionMethod()).newPrivateKeyWrapper(keyAccessorType);
    }

    @Override
    public SymmetricKeyWrapper newSymmetricKeyWrapper(KeyAccessorType keyAccessorType) {
        if (!symmetricKeyFactories.containsKey(keyAccessorType.getKeyEncryptionMethod())) {
            throw new NoSuchKeyEncryptionMethod(thesaurus);
        }
        return symmetricKeyFactories.get(keyAccessorType.getKeyEncryptionMethod()).newSymmetricKey(keyAccessorType);
    }

    @Override
    public CertificateWrapper newCertificateWrapper(String alias) {
        RequestableCertificateWrapperImpl renewableCertificate = getDataModel().getInstance(RequestableCertificateWrapperImpl.class);
        renewableCertificate.setAlias(alias);
        renewableCertificate.save();
        return renewableCertificate;
    }

    @Override
    public ClientCertificateWrapper newClientCertificateWrapper(KeyAccessorType clientCertificateAccessorType) {
        AbstractPlaintextPrivateKeyWrapperImpl privateKeyWrapper = (AbstractPlaintextPrivateKeyWrapperImpl) this.newPrivateKeyWrapper(clientCertificateAccessorType);
        ClientCertificateWrapperImpl clientCertificate = getDataModel().getInstance(ClientCertificateWrapperImpl.class)
                .init(privateKeyWrapper, clientCertificateAccessorType.getKeyType());
        clientCertificate.save();
        return clientCertificate;
    }

    @Override
    public Optional<ClientCertificateWrapper> findClientCertificateWrapper(String alias) {
        return getDataModel().mapper(ClientCertificateWrapper.class).getUnique(ClientCertificateWrapperImpl.Fields.ALIAS.fieldName(), alias);
    }

    @Override
    public Finder<CertificateWrapper> findAllCertificates() {
        return DefaultFinder.of(CertificateWrapper.class,
                Where.where("class").in(Arrays.asList(AbstractCertificateWrapperImpl.CERTIFICATE_DISCRIMINATOR, AbstractCertificateWrapperImpl.CLIENT_CERTIFICATE_DISCRIMINATOR)),
                getDataModel());
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
}
