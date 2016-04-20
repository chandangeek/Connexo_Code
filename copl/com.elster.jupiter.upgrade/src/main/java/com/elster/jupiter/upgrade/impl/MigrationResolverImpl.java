package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;
import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class MigrationResolverImpl implements InstallAwareMigrationResolver {

    private final DataModel dataModel;
    private final List<Migration> migrations;

    public MigrationResolverImpl(DataModel dataModel, DataModelUpgrader dataModelUpgrader, TransactionService transactionService, Class<? extends FullInstaller> installer, List<Class<? extends Upgrader>> upgraders) {
        this.dataModel = dataModel;
        this.migrations = ImmutableList.<Migration>builder()
                .add(new InstallerDriver(dataModel, dataModelUpgrader, transactionService, this, dataModel.getInstance(installer)))
                .addAll(
                        upgraders
                                .stream()
                                .map(dataModel::getInstance)
                                .map(upgrader -> new MigrationDriver(upgrader, dataModelUpgrader))
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        return migrations
                .stream()
                .map(ResolvedMigrationImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public void installed() {
        migrations
                .stream()
                .forEach(Migration::youveBeenInstalled);
    }
}
