/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.persistence.test.rules;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.annotation.Annotation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalRuleTest {

    private static final Transactional TRANSACTIONAL = new Transactional() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Transactional.class;
        }
    };

    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionContext context;

    @Before
    public void setUp() {
        when(transactionService.getContext()).thenReturn(context);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testTransactionalRollsBackIfAllIsWell() throws Throwable {
        TransactionalRule transactionalRule = new TransactionalRule(transactionService);

        Statement statement = transactionalRule.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // happily behaving nicely
            }
        }, Description.createTestDescription("className", "name", TRANSACTIONAL));

        statement.evaluate();

        verify(context).close();
    }

    @Test
    public void testTransactionalRollsBackIfUnexpectedExceptionOccurs() {
        TransactionalRule transactionalRule = new TransactionalRule(transactionService);

        Statement statement = transactionalRule.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                throw new RuntimeException("I came unexpectedly");
            }
        }, Description.createTestDescription("className", "name", TRANSACTIONAL));

        try {
            statement.evaluate();
        } catch (Throwable throwable) {
            // expected
        }

        verify(context).close();
    }

    @Test
    public void testTransactionalRollsBackIfAssertionErrorOccurs() {
        TransactionalRule transactionalRule = new TransactionalRule(transactionService);

        Statement statement = transactionalRule.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                throw new AssertionError("Unit test fails");
            }
        }, Description.createTestDescription("className", "name", TRANSACTIONAL));

        try {
            statement.evaluate();
        } catch (Throwable throwable) {
            // expected
        }

        verify(context).close();
    }


}
