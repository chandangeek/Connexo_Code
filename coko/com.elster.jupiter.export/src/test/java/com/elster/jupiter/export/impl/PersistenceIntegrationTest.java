/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceIntegrationTest {

    protected static ExportInMemoryBootstrapModule inMemoryPersistence = new ExportInMemoryBootstrapModule();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence.initializeDatabase();
    }

    @AfterClass
    public static void cleanUpDatabase() throws SQLException {
        inMemoryPersistence.cleanUpDatabase();
    }
}
