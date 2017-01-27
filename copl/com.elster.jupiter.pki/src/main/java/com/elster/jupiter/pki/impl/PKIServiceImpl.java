package com.elster.jupiter.pki.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PKIService;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Created by bvn on 1/26/17.
 */
@Component(name="PkiService",
        service = PKIService.class,
        property = "name=" + PKIService.COMPONENTNAME,
        immediate = true)
public class PKIServiceImpl implements PKIService {


    private DataModel dataModel;
    private UpgradeService upgradeService;

    @Inject
    public PKIServiceImpl(OrmService ormService, UpgradeService upgradeService) {
        this.setOrmService(ormService);
        this.setUpgradeService(upgradeService);
        this.activate();
    }

    public PKIServiceImpl() {

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

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
//        upgradeService.register(InstallIdentifier.identifier("MultiSense", DeviceConfigurationService.COMPONENTNAME), dataModel, Installer.class, ImmutableMap
//                .of(
//                        Version.version(10, 2), UpgraderV10_2.class
//                ));
//        initPrivileges();
    }

    private AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(UpgradeService.class).toInstance(upgradeService);
            }
        };
    }

    @Override
    public KeyType addSymmetricKeyType(String name, String keyAlgorithmName, int keySize) {
        KeyTypeImpl keyType = dataModel.getInstance(KeyTypeImpl.class);
        keyType.setName(name);
        keyType.setAlgorithm(keyAlgorithmName);
        keyType.setKeySize(keySize);
        return keyType;
    }

    @Override
    public AsyncBuilder addAsymmetricKeyType(String name) {
        return null;
    }

    @Override
    public AsyncBuilder addCertificateWithPrivateKeyType(String name) {
        return null;
    }

    @Override
    public KeyType addCertificateType(String name) {
        return null;
    }

    @Override
    public Finder<KeyType> findAllKeyTypes() {
        return DefaultFinder.of(KeyType.class, dataModel).defaultSortColumn(KeyTypeImpl.Fields.NAME.fieldName());
    }
}
