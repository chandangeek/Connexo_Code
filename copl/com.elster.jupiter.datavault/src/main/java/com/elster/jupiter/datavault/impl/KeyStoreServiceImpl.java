package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.datavault.KeyStoreService;
import com.elster.jupiter.datavault.LegacyDataVaultProvider;
import com.elster.jupiter.datavault.PersistentKeyStore;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerKeyStoreService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (11:32)
 */
@Component(
        name = "com.elster.jupiter.keystore.service",
        service = ServerKeyStoreService.class,
        property = "name=" + DataVaultService.COMPONENT_NAME,
        immediate = true)
@SuppressWarnings("unused")
public class KeyStoreServiceImpl implements ServerKeyStoreService {

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile UpgradeService upgradeService;

    // For OSGi purposes
    public KeyStoreServiceImpl() {
        super();
    }

    @Inject
    public KeyStoreServiceImpl(NlsService nlsService, OrmService ormService, UpgradeService upgradeService) {
        this();
        this.setNlsService(nlsService);
        this.setOrmService(ormService);
        this.setUpgradeService(upgradeService);
        this.activate();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(DataVaultService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(DataVaultService.COMPONENT_NAME, "Key stores");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        LegacyDataVaultProvider.instance.set(() -> dataModel.getInstance(DataVault.class));
        upgradeService.register(InstallIdentifier.identifier("Pulse", DataVaultService.COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(NlsService.class).toInstance(nlsService);
                bind(ExceptionFactory.class);
                bind(ServerKeyStoreService.class).toInstance(KeyStoreServiceImpl.this);
                bind(KeyStoreService.class).to(ServerKeyStoreService.class);
                bind(DataVault.class).toProvider(DataVaultProvider.class).in(Singleton.class);
            }
        };
    }

    @Override
    public DataVault getDataVaultInstance() {
        return this.dataModel.getInstance(DataVault.class);
    }

    @Override
    public Builder newDataVaultKeyStore(String type) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        return this.newKeyStore(this.dataModel.getInstance(DataVaultKeyStore.class).initialize(type));
    }

    @Override
    public Builder newUserDefinedKeyStore(String name, String type) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        return this.newKeyStore(this.dataModel.getInstance(UserDefinedKeyStore.class).initialize(name, type));
    }

    @Override
    public List<PersistentKeyStore> findUserDefinedKeyStores() {
        return new ArrayList<>(this.dataModel.mapper(UserDefinedKeyStore.class).find());
    }

    @Override
    public List<PersistentKeyStore> findUserDefinedKeyStores(String type) {
        return new ArrayList<>(
                this.dataModel
                        .mapper(UserDefinedKeyStore.class)
                        .find(PersistentKeyStoreImpl.Fields.TYPE.fieldName(), type));
    }

    @Override
    public Builder newSystemDefinedKeyStore(String name, String type) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        return this.newKeyStore(this.dataModel.getInstance(SystemDefinedKeyStore.class).initialize(name, type));
    }

    private Builder newKeyStore(PersistentKeyStoreImpl underConstruction) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        return new BuilderImpl(underConstruction);
    }

    @Override
    public Optional<PersistentKeyStore> findSystemDefined(String name) {
        return this.dataModel
                .mapper(SystemDefinedKeyStore.class)
                .getUnique(PersistentKeyStoreImpl.Fields.NAME.fieldName(), name)
                .map(PersistentKeyStore.class::cast);
    }

    private class BuilderImpl implements Builder {
        private Builder actual;

        BuilderImpl(PersistentKeyStoreImpl persistentKeyStore) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
            this.actual = new Underconstruction(persistentKeyStore);
        }

        @Override
        public Builder setEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
            this.actual.setEntry(alias, entry, protParam);
            return this;
        }

        @Override
        public Builder setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
            this.actual.setCertificateEntry(alias, cert);
            return this;
        }

        @Override
        public Builder setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
            this.actual.setKeyEntry(alias, key, chain);
            return this;
        }

        @Override
        public Builder setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
            this.actual.setKeyEntry(alias, key, password, chain);
            return this;
        }

        @Override
        public PersistentKeyStore build(char[] password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
            PersistentKeyStore keyStore = this.actual.build(password);
            this.actual = new Completed();
            return keyStore;
        }
    }

    private static class Underconstruction implements Builder {
        private final KeyStore keyStore;
        private final PersistentKeyStoreImpl persistentKeyStore;

        private Underconstruction(PersistentKeyStoreImpl persistentKeyStore) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
            this.keyStore = KeyStore.getInstance(persistentKeyStore.getType());
            this.keyStore.load(null); // This initializes the empty keystore
            this.persistentKeyStore = persistentKeyStore;
        }

        @Override
        public Builder setEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
            this.keyStore.setEntry(alias, entry, protParam);
            return this;
        }

        @Override
        public Builder setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
            this.keyStore.setCertificateEntry(alias, cert);
            return this;
        }

        @Override
        public Builder setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
            this.keyStore.setKeyEntry(alias, key, chain);
            return this;
        }

        @Override
        public Builder setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
            this.keyStore.setKeyEntry(alias, key, password, chain);
            return this;
        }

        @Override
        public PersistentKeyStore build(char[] password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                this.keyStore.store(outputStream, password);
                this.persistentKeyStore.setKeyStoreBytesFrom(outputStream);
                this.persistentKeyStore.save();
                return this.persistentKeyStore;
            }
        }
    }

    private static class Completed implements Builder {
        @Override
        public Builder setEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
            throw new IllegalStateException("Building process for new KeyStore is complete");
        }

        @Override
        public Builder setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
            throw new IllegalStateException("Building process for new KeyStore is complete");
        }

        @Override
        public Builder setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
            throw new IllegalStateException("Building process for new KeyStore is complete");
        }

        @Override
        public Builder setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
            throw new IllegalStateException("Building process for new KeyStore is complete");
        }

        @Override
        public PersistentKeyStore build(char[] password) {
            throw new IllegalStateException("Building process for new KeyStore is complete");
        }
    }
}