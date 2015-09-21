package com.energyict.mdc.scheduling.model.impl;

import com.energyict.mdc.scheduling.SchedulingService;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class PersistenceTest {

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    public PersistenceTest() {
    }

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabase("PersistenceTest.mdc.scheduling", false);
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public SchedulingService getSchedulingService() {
        return inMemoryPersistence.getSchedulingService();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Before
    public void initializeMocks() {
    }

}