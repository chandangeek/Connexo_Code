package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallAwareMigrationResolver;
import com.elster.jupiter.upgrade.Installer;
import com.elster.jupiter.upgrade.Migration;

import org.flywaydb.core.api.MigrationVersion;

import java.sql.Connection;

public final class InstallerDriver implements Migration {
    private final DataModel dataModel;
    private final TransactionService transactionService;
    private final InstallAwareMigrationResolver installAwareMigrationResolver;
    private final Installer installer;


    public InstallerDriver(DataModel dataModel, TransactionService transactionService,InstallAwareMigrationResolver resolver, Installer installer) {
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
    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion("1.0");
    }

    @Override
    public boolean executeInTransaction() {
        return false;
    }

    @Override
    public void migrate(Connection connection) throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            if (!dataModel.isInstalled()) {
                installer.install();
                installAwareMigrationResolver.installed(installer.installs());
            }
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
