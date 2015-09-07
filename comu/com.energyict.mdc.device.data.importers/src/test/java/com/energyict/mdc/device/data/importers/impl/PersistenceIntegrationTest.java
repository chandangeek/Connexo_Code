package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceIntegrationTest {

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    protected static InMemoryIntegrationPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() throws SQLException {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        inMemoryPersistence.initializeDatabase(false);
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }
}