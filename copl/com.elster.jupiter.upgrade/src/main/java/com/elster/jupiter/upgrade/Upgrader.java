package com.elster.jupiter.upgrade;

import com.elster.jupiter.util.HasName;

import org.flywaydb.core.api.MigrationVersion;

public interface Upgrader extends HasName {

    MigrationVersion getVersion();

    void migrate() throws Exception;
}
