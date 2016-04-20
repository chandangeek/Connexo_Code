package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import java.sql.Connection;

final class MigrationDriver implements Migration {

    private final Upgrader upgrader;
    private final DataModelUpgrader dataModelUpgrader;
    private boolean iveBeenInstalled;

    public MigrationDriver(Upgrader upgrader, DataModelUpgrader dataModelUpgrader) {
        this.upgrader = upgrader;
        this.dataModelUpgrader = dataModelUpgrader;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public Version getVersion() {
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
            upgrader.migrate(dataModelUpgrader);
        }
    }

    @Override
    public String getName() {
        return upgrader.getName();
    }
}
