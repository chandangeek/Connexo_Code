/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.persistence.test;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TransactionVerifierTest {

    private TransactionVerifier transactionService;

    @Mock
    private Comparator<String> comparator;

    @Before
    public void setUp() {
        transactionService = new TransactionVerifier(comparator);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testInTransactionContext() {
        try (TransactionContext ctx = transactionService.getContext()) {
            comparator.compare("A", "B");
            ctx.commit();
        }

        verify(comparator, transactionService.inTransaction()).compare("A", "B");
    }

    @Test(expected = AssertionError.class)
    public void testInTransactionContextFails() {
        try (TransactionContext ctx = transactionService.getContext()) {
            ctx.commit();
        }
        comparator.compare("A", "B");
        verify(comparator, transactionService.inTransaction()).compare("A", "B");
    }


    @Test
    public void testInTransactionClass() {
        Transaction<String> transaction = () -> {
            comparator.compare("A", "B");
            return "A";
        };

        String result = transactionService.execute(transaction);

        assertThat(result).isEqualTo("A");

        verify(comparator, transactionService.inTransaction()).compare("A", "B");
    }

    @Test(expected = AssertionError.class)
    public void testInTransactionClassFails() {
        Transaction<String> transaction = () -> {
            return "A";
        };
        comparator.compare("A", "B");

        String result = transactionService.execute(transaction);

        verify(comparator, transactionService.inTransaction()).compare("A", "B");
    }

    @Test
    public void testNotInTransactionContext() {
        try (TransactionContext ctx = transactionService.getContext()) {
            ctx.commit();
        }
        comparator.compare("A", "B");

        verify(comparator, transactionService.notInTransaction()).compare("A", "B");
    }

    @Test(expected = AssertionError.class)
    public void testNotInTransactionContextFails() {
        try (TransactionContext ctx = transactionService.getContext()) {
            comparator.compare("A", "B");
            ctx.commit();
        }
        verify(comparator, transactionService.notInTransaction()).compare("A", "B");
    }

    @Test
    public void testNotInTransactionClass() {
        Transaction<String> transaction = () -> {
            return "A";
        };
        comparator.compare("A", "B");

        String result = transactionService.execute(transaction);

        assertThat(result).isEqualTo("A");

        verify(comparator, transactionService.notInTransaction()).compare("A", "B");
    }

    @Test(expected = AssertionError.class)
    public void testNotInTransactionClassFails() {
        Transaction<String> transaction = () -> {
            comparator.compare("A", "B");
            return "A";
        };

        String result = transactionService.execute(transaction);

        verify(comparator, transactionService.notInTransaction()).compare("A", "B");
    }

    @Test
    public void testInNthTransactionClassFails() {
        Transaction<String> transaction = () -> {
            comparator.compare("A", "B");
            return "A";
        };

        String result = transactionService.execute(transaction);

        try {
            verify(comparator, transactionService.inTransaction(2)).compare("A", "B");
            fail("expected verification to fail.");
        } catch (AssertionError e) {
            assertThat(e.getMessage()).contains("Method invoked in the 1st transaction instead of in the 2nd");
        }
    }


}
