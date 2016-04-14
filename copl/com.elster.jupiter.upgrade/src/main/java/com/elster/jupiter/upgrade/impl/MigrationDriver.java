package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.upgrade.Migration;
import com.elster.jupiter.upgrade.Upgrader;

import org.flywaydb.core.api.MigrationVersion;

import java.sql.Connection;

public final class MigrationDriver implements Migration {

    private final Upgrader upgrader;
    private boolean iveBeenInstalled;

    public MigrationDriver(Upgrader upgrader) {
        this.upgrader = upgrader;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public MigrationVersion getVersion() {
        return upgrader.getVersion();
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
    public void migrate(Connection connection) throws Exception {
        if (!iveBeenInstalled) {
            upgrader.migrate();
        }
    }

    @Override
    public String getName() {
        return upgrader.getName();
    }
}
