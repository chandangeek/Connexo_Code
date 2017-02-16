/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PrivateKeyFactory;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.impl.Installer;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Collections;
import java.util.stream.Stream;

@Component(name="PlaintextPrivateKeyFactory", service = PrivateKeyFactory.class, immediate = true)
public class PlaintextPrivateKeyFactory implements PrivateKeyFactory {

    public static final String KEY_ENCRYPTION_METHOD = "SSM";
    private static final String COMPONENTNAME = "SSM";

    private volatile DataVaultService dataVaultService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile UpgradeService upgradeService;

    @Inject
    public PlaintextPrivateKeyFactory(DataVaultService dataVaultService, PropertySpecService propertySpecService, OrmService ormService, NlsService nlsService, UpgradeService upgradeService) {
        this.setDataVaultService(dataVaultService);
        this.setPropertySpecService(propertySpecService);
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setUpGradeService(upgradeService);
        activate();
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Plaintext keys");
        Stream.of(TableSpecs.values()).forEach(tableSpec -> tableSpec.addTo(dataModel));
        this.dataModel = dataModel;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setUpGradeService(UpgradeService upGradeService) {
        this.upgradeService = upGradeService;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return KEY_ENCRYPTION_METHOD;
    }

    @Override
    public PrivateKeyWrapper newPrivateKey(KeyAccessorType keyAccessorType) {
        switch (PkiService.AsymmetricKeyAlgorithms.valueOf(keyAccessorType.getKeyType().getAlgorithm())) {
            case ECDSA: return newEcdsaPrivateKey(keyAccessorType);
            case RSA: return newRsaPrivateKey(keyAccessorType);
            case DSA: return newDsaPrivateKey(keyAccessorType);
            default: return null;
        }
    }

    private PrivateKeyWrapper newRsaPrivateKey(KeyAccessorType keyAccessorType) {
        PlaintextRsaPrivateKey plaintextPrivateKey = new PlaintextRsaPrivateKey(dataVaultService, propertySpecService, dataModel);
        plaintextPrivateKey.init(keyAccessorType);
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    private PrivateKeyWrapper newDsaPrivateKey(KeyAccessorType keyAccessorType) {
        PlaintextDsaPrivateKey plaintextPrivateKey = new PlaintextDsaPrivateKey(dataVaultService, propertySpecService, dataModel);
        plaintextPrivateKey.init(keyAccessorType);
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    private PrivateKeyWrapper newEcdsaPrivateKey(KeyAccessorType keyAccessorType) {
        PlaintextEcdsaPrivateKey plaintextPrivateKey = new PlaintextEcdsaPrivateKey(dataVaultService, propertySpecService, dataModel);
        plaintextPrivateKey.init(keyAccessorType);
        plaintextPrivateKey.save();
        return plaintextPrivateKey;
    }

    @Activate
    public void activate() {
        registerInjector();
        upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENTNAME), dataModel, Installer.class, Collections.emptyMap());
    }

    private void registerInjector() {
        this.dataModel.register(this.getModule());
    }

    private AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Thesaurus.class).toInstance(thesaurus);
            }
        };
    }

}
