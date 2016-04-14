package com.elster.jupiter.upgrade;

import com.elster.jupiter.util.HasName;

import org.flywaydb.core.api.MigrationVersion;

public interface Installer extends HasName {

    /**
     * @return up to which version is installed
     */
    MigrationVersion installs();

    void install();

}
