/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.Version;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.sql.Connection;
import java.sql.SQLException;

final class ResolvedMigrationImpl implements ResolvedMigration {

    private final Migration migration;
    private final Version version;

    public ResolvedMigrationImpl(Version version, Migration migration) {
        this.migration = migration;
        this.version = version;
    }

    @Override
    public MigrationVersion getVersion() {
        return migration.isRepeatable() ? null : MigrationVersion.fromVersion(version.toString());
    }

    @Override
    public String getDescription() {
        return migration.getName();
    }

    @Override
    public String getScript() {
        return migration.getClass().getName();
    }

    @Override
    public Integer getChecksum() {
        return null;
    }

    @Override
    public MigrationType getType() {
        return MigrationType.JDBC;
    }

    @Override
    public String getPhysicalLocation() {
        return null;
    }

    @Override
    public MigrationExecutor getExecutor() {
        return new MigrationExecutor() {
            @Override
            public void execute(Connection connection) throws SQLException {
                try {
                    migration.migrate(connection);
                } catch (Exception e) {
                    throw new FlywayException(e); // TODO
                }
            }

            @Override
            public boolean executeInTransaction() {
                return migration.executeInTransaction();
            }
        };
    }
}
