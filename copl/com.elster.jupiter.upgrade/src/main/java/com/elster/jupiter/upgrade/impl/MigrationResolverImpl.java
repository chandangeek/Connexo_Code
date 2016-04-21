package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableMap;
import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;

final class MigrationResolverImpl implements InstallAwareMigrationResolver {

    private final DataModel dataModel;
    private final Map<Version, Migration> migrations;

    public MigrationResolverImpl(DataModel dataModel, DataModelUpgrader dataModelUpgrader, TransactionService transactionService, Class<? extends FullInstaller> installer, Map<Version, Class<? extends Upgrader>> upgraders) {
        this.dataModel = dataModel;
        this.migrations = ImmutableMap.<Version, Migration>builder()
                .put(version(1, 0), new InstallerDriver(dataModel, dataModelUpgrader, transactionService, this, installer))
                .putAll(
                        upgraders
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> new MigrationDriver(dataModelUpgrader, dataModel, entry.getValue())
                                ))
                )
                .build();
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        return migrations
                .entrySet()
                .stream()
                .map(entry -> new ResolvedMigrationImpl(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void installed() {
        migrations
                .values()
                .stream()
                .forEach(Migration::youveBeenInstalled);
    }
}
