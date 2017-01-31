package com.elster.jupiter.pki.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by bvn on 1/26/17.
 */
@Component(name="PkiService",
        service = PkiService.class,
        property = "name=" + PkiService.COMPONENTNAME,
        immediate = true)
public class PkiServiceImpl implements PkiService {


    private DataModel dataModel;
    private UpgradeService upgradeService;
    private Thesaurus thesaurus;

    @Inject
    public PkiServiceImpl(OrmService ormService, UpgradeService upgradeService, NlsService nlsService) {
        this.setOrmService(ormService);
        this.setUpgradeService(upgradeService);
        this.setNlsService(nlsService);
        this.activate();
    }

    public PkiServiceImpl() {

    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Private Key Infrastructure");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
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
            }
        };
    }

    @Override
    public KeyType addSymmetricKeyType(String name, String keyAlgorithmName, int keySize) {
        KeyTypeImpl keyType = dataModel.getInstance(KeyTypeImpl.class);
        keyType.setName(name);
        keyType.setAlgorithm(keyAlgorithmName);
        keyType.setCryptographicType(CryptographicType.SymmetricKey);
        keyType.setKeySize(keySize);
        keyType.save();
        return keyType;
    }

    @Override
    public AsyncBuilder addAsymmetricKeyType(String name) {
        KeyTypeImpl instance = dataModel.getInstance(KeyTypeImpl.class);
        return new AsyncBuilderImpl(name, instance);
    }

    @Override
    public AsyncBuilder addCertificateWithPrivateKeyType(String name) {
        return null; // TODO complete
    }

    @Override
    public KeyType addCertificateType(String name) {
        return null; // TODO complete
    }

    @Override
    public Optional<KeyType> getKeyType(String name) {
        return this.getDataModel().mapper(KeyType.class).getUnique("name", name);
    }

    @Override
    public Finder<KeyType> findAllKeyTypes() {
        return DefaultFinder.of(KeyType.class, dataModel).defaultSortColumn(KeyTypeImpl.Fields.NAME.fieldName());
    }

    private class AsyncBuilderImpl implements AsyncBuilder {
        private final KeyTypeImpl underConstruction;

        AsyncBuilderImpl(String name, KeyTypeImpl instance) {
            this.underConstruction = instance;
            this.underConstruction.setName(name);
            this.underConstruction.setCryptographicType(CryptographicType.AsymmetricKey);
        }

        @Override
        public AsyncKeySizeBuilder RSA() {
            this.underConstruction.setAlgorithm("RSA");
            return new AsyncKeySizeBuilderImpl();
        }

        @Override
        public AsyncKeySizeBuilder DSA() {
            this.underConstruction.setAlgorithm("DSA");
            return new AsyncKeySizeBuilderImpl();
        }

        @Override
        public AsyncCurveBuilder EC() {
            this.underConstruction.setAlgorithm("EC");
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
}
