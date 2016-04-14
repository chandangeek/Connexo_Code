package com.elster.jupiter.upgrade;

import com.elster.jupiter.util.HasName;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

public interface Migration extends JdbcMigration, HasName {

    boolean isRepeatable();

    MigrationVersion getVersion();

    boolean executeInTransaction();

    void youveBeenInstalled();
}
