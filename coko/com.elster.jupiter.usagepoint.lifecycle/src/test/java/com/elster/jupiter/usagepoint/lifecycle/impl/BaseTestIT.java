/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.transaction.TransactionService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

public abstract class BaseTestIT {

    private static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.get(TransactionService.class));

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void beforeClass() {
        inMemoryPersistence.initializeDatabase();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        inMemoryPersistence.cleanUpDataBase();
    }

    protected <T> T get(Class<T> clazz) {
        return inMemoryPersistence.get(clazz);
    }
}
