package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;

import java.sql.Connection;

import static com.elster.jupiter.orm.Version.version;

final class InstallerDriver implements Migration {
    private final DataModel dataModel;
    private final TransactionService transactionService;
    private final InstallAwareMigrationResolver installAwareMigrationResolver;
    private final FullInstaller installer;


    public InstallerDriver(DataModel dataModel, TransactionService transactionService,InstallAwareMigrationResolver resolver, FullInstaller installer) {
        this.dataModel = dataModel;
        this.transactionService = transactionService;
        this.installer = installer;
        this.installAwareMigrationResolver = resolver;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public Version getVersion() {
        return version("1.0");
    }

    @Override
    public boolean executeInTransaction() {
        return false;
    }

    @Override
    public void migrate(Connection connection) throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            installer.install();
            installAwareMigrationResolver.installed(installer.installs());
            context.commit();
        }
    }

    @Override
    public final void youveBeenInstalled() {
    }

    @Override
    public String getName() {
        return installer.getName();
    }
}
