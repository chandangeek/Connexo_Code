package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;

import java.sql.Connection;

final class InstallerDriver implements Migration {
    private final TransactionService transactionService;
    private final InstallAwareMigrationResolver installAwareMigrationResolver;
    private final DataModelUpgrader dataModelUpgrader;
    private final Holder<FullInstaller> installer;


    public InstallerDriver(DataModel dataModel, DataModelUpgrader dataModelUpgrader, TransactionService transactionService, InstallAwareMigrationResolver resolver, Class<? extends FullInstaller> installer) {
        this.dataModelUpgrader = dataModelUpgrader;
        this.transactionService = transactionService;
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
        try (TransactionContext context = transactionService.getContext()) {
            installer.get().install(dataModelUpgrader);
            installAwareMigrationResolver.installed();
            context.commit();
        }
    }

    @Override
    public final void youveBeenInstalled() {
    }

    @Override
    public String getName() {
        return installer.get().getClass().getName();
    }
}
