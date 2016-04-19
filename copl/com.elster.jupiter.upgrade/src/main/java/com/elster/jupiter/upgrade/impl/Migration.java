package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.Version;
import com.elster.jupiter.util.HasName;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

public interface Migration extends JdbcMigration, HasName {

    boolean isRepeatable();

    Version getVersion();

    boolean executeInTransaction();

    void youveBeenInstalled();
}
