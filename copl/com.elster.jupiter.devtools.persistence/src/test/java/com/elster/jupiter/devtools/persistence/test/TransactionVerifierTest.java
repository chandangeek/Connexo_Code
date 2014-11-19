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
import static org.mockito.Mockito.verify;

/**
 * Copyrights EnergyICT
 * Date: 19/11/2014
 * Time: 15:09
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionVerifierTest {

    private TransactionVerifier transactionVerifier;

    @Mock
    private Comparator<String> comparator;

    @Before
    public void setUp() {
        transactionVerifier = new TransactionVerifier(comparator);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testInTransactionContext() {
        try (TransactionContext ctx = transactionVerifier.getContext()) {
            comparator.compare("A", "B");
            ctx.commit();
        }

        verify(comparator, transactionVerifier.inTransaction()).compare("A", "B");
    }

    @Test(expected = AssertionError.class)
    public void testInTransactionContextFails() {
        try (TransactionContext ctx = transactionVerifier.getContext()) {
            ctx.commit();
        }
        comparator.compare("A", "B");
        verify(comparator, transactionVerifier.inTransaction()).compare("A", "B");
    }

    @Test
    public void testInTransactionClass() {
        Transaction<String> transaction = () -> {
            comparator.compare("A", "B");
            return "A";
        };

        String result = transactionVerifier.execute(transaction);

        assertThat(result).isEqualTo("A");

        verify(comparator, transactionVerifier.inTransaction()).compare("A", "B");
    }

    @Test(expected = AssertionError.class)
    public void testInTransactionClassFails() {
        Transaction<String> transaction = () -> {
            return "A";
        };
        comparator.compare("A", "B");

        String result = transactionVerifier.execute(transaction);

        verify(comparator, transactionVerifier.inTransaction()).compare("A", "B");
    }


}
