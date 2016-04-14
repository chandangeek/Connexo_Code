package com.elster.jupiter.upgrade;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;

public interface InstallAwareMigrationResolver extends MigrationResolver {
    void installed(MigrationVersion installed);
}
