package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallAwareMigrationResolver;
import com.elster.jupiter.upgrade.Installer;
import com.elster.jupiter.upgrade.Migration;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MigrationResolverImpl implements InstallAwareMigrationResolver {

    private final DataModel dataModel;
    private final List<Migration> migrations;

    public MigrationResolverImpl(DataModel dataModel, Class<? extends Installer> installer, List<Class<? extends Upgrader>> upgraders) {
        this.dataModel = dataModel;
        this.migrations = ImmutableList.<Migration>builder()
                .add(new InstallerDriver(dataModel, dataModel.getInstance(TransactionService.class), this, dataModel.getInstance(installer)))
                .addAll(
                        upgraders
                                .stream()
                                .map(dataModel::getInstance)
                                .map(MigrationDriver::new)
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
    public void installed(MigrationVersion installed) {
        migrations
                .stream()
                .filter(migration -> migration.getVersion().compareTo(installed) <= 0)
                .forEach(Migration::youveBeenInstalled);
    }
}
