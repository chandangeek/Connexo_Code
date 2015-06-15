package com.energyict.mdc.issue.datavalidation.impl;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;

@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceIntegrationTest {

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    protected static InMemoryIntegrationPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() throws SQLException {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        inMemoryPersistence.initializeDatabase("PersistenceIntegrationTest.mdc.issue.datavalidation", false);

        try (TransactionContext ctx = inMemoryPersistence.getTransactionService().getContext()) {
            inMemoryPersistence.getService(IssueDataValidationService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }
}