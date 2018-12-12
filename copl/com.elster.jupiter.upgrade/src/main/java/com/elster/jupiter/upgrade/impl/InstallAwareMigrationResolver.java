/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade.impl;

import org.flywaydb.core.api.resolver.MigrationResolver;

public interface InstallAwareMigrationResolver extends MigrationResolver {
    void installed();
}
