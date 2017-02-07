/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;

import java.sql.Connection;
import java.util.logging.Logger;

final class InstallerDriver implements Migration {
    private final TransactionService transactionService;
    private final InstallAwareMigrationResolver installAwareMigrationResolver;
    private final DataModelUpgrader dataModelUpgrader;
    private final Holder<FullInstaller> installer;
    private final Logger logger;


    public InstallerDriver(DataModel dataModel, DataModelUpgrader dataModelUpgrader, TransactionService transactionService, InstallAwareMigrationResolver resolver, Class<? extends FullInstaller> installer, Logger logger) {
        this.dataModelUpgrader = dataModelUpgrader;
        this.transactionService = transactionService;
        this.logger = logger;
        this.installer = HolderBuilder.lazyInitialize(() -> dataModel.getInstance(installer));
        this.installAwareMigrationResolver = resolver;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean executeInTransaction() {
        return false;
    }

    @Override
    public void migrate(Connection connection) throws Exception {
        if (transactionService.isInTransaction()) {
            doInstall().run();
            return;
        }
        transactionService.builder()
                .principal(() -> "Installer")
                .run(doInstall());
    }

    private Runnable doInstall() {
        return () -> {
                    installer.get().install(dataModelUpgrader, logger);
                    installAwareMigrationResolver.installed();
                };
    }

    @Override
    public final void youveBeenInstalled() {
    }

    @Override
    public String getName() {
        return installer.get().getClass().getName();
    }
}
