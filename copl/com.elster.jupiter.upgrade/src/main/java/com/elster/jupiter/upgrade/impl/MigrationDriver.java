/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;

import java.sql.Connection;

final class MigrationDriver implements Migration {

    private final Holder<Upgrader> upgrader;
    private final DataModelUpgrader dataModelUpgrader;
    private final TransactionService transactionService;
    private boolean iveBeenInstalled;

    public MigrationDriver(DataModelUpgrader dataModelUpgrader, DataModel dataModel, TransactionService transactionService, Class<? extends Upgrader> upgrader) {
        this.dataModelUpgrader = dataModelUpgrader;
        this.transactionService = transactionService;
        this.upgrader = HolderBuilder.lazyInitialize(() -> dataModel.getInstance(upgrader));
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
    public void youveBeenInstalled() {
        iveBeenInstalled = true;
    }

    @Override
    public void migrate(Connection connection) {
        if (!iveBeenInstalled) {
            if (transactionService.isInTransaction()) {
                doMigrate().run();
                return;
            }
            transactionService.builder()
                    .principal(() -> "Installer")
                    .run(doMigrate());
        }
    }

    private Runnable doMigrate() {
        return () -> upgrader.get().migrate(dataModelUpgrader);
    }

    @Override
    public String getName() {
        return upgrader.get().getClass().getName();
    }
}
