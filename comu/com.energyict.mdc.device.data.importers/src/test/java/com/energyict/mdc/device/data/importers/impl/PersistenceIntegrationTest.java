package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.transaction.TransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceIntegrationTest {

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    protected static InMemoryIntegrationPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() throws SQLException {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        inMemoryPersistence.initializeDatabase("PersistenceIntegrationTest.mdc.device.data.importers", false);
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }
}