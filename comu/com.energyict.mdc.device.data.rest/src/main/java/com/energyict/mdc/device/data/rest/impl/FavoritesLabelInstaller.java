/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.energyict.mdc.favorites.FavoritesService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.energyict.ddr.favorites.label", service = FavoritesLabelInstaller.class, immediate = true, property = {"name=FLI"})
public class FavoritesLabelInstaller implements FullInstaller {

    private static final Logger LOGGER = Logger.getLogger(FavoritesLabelInstaller.class.getName());
    private volatile FavoritesService favoritesService;
    private volatile UpgradeService upgradeService;

    @Reference
    public void setFavoritesService(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(FavoritesLabelInstaller.class).toInstance(FavoritesLabelInstaller.this);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("MultiSense", "FLI"), dataModel, FavoritesLabelInstaller.class, Collections
                .emptyMap());
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create label categories for FLI",
                this::createLabelCategories,
                logger
        );
    }

    private void createLabelCategories() {
        try {
            favoritesService.createLabelCategory(DefaultTranslationKey.MDC_LABEL_CATEGORY_FAVORITES.getKey());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}
