/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.datavault.LegacyDataVaultProvider;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by bvn on 11/6/14.
 */
@Component(
        name = "com.elster.kore.datavault",
        service = {DataVaultService.class, MessageSeedProvider.class},
        property = "name=" + DataVaultService.COMPONENT_NAME,
        immediate = true)
public final class DataVaultServiceImpl implements DataVaultService, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile UpgradeService upgradeService;

    public DataVaultServiceImpl() {
    }

    @Inject
    public DataVaultServiceImpl(NlsService nlsService, OrmService ormService, UpgradeService upgradeService) {
        this();
        setNlsService(nlsService);
        setOrmService(ormService);
        setUpgradeService(upgradeService);
        activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENT_NAME, "Data vault");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
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
                bind(DataVault.class).toProvider(DataVaultProvider.class).in(Singleton.class);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        LegacyDataVaultProvider.instance.set(() -> dataModel.getInstance(DataVault.class));
        upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Override
    public String encrypt(byte[] plainText) {
        DataVault dataVault = dataModel.getInstance(DataVault.class);
        return dataVault.encrypt(plainText);
    }

    @Override
    public byte[] decrypt(String encrypted) {
        DataVault dataVault = dataModel.getInstance(DataVault.class);
        return dataVault.decrypt(encrypted);
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}