/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.favorites.FavoritesService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.energyict.mdc.dashboard.init", service = {DashBoardInitService.class}, immediate = true, property = {"name=" + DashboardApplication.COMPONENT_NAME})
public class DashBoardInitService {

    private volatile UserService userService;
    private volatile UpgradeService upgradeService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile TransactionService transactionService;
    private volatile FavoritesService favoritesService;

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("MultiSense", "DSI"), dataModel, DashboardApplicationInstaller.class, ImmutableMap.of(version(10, 2), UpgraderV10_2.class));
    }


    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setFavoritesService(FavoritesService favoritesService) {
        // for the order of installation
        this.favoritesService = favoritesService;
    }
}
