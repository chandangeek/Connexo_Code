/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.util.HasName;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

public interface Migration extends JdbcMigration, HasName {

    boolean isRepeatable();

    boolean executeInTransaction();

    void youveBeenInstalled();
}
