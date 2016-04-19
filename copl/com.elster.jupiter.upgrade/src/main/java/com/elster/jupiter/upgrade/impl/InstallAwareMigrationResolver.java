package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.Version;

import org.flywaydb.core.api.resolver.MigrationResolver;

public interface InstallAwareMigrationResolver extends MigrationResolver {
    void installed(Version installed);
}
