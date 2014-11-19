package com.elster.jupiter.devtools.persistence.test;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableList;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.finder.AllInvocationsFinder;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.Invocation;
import org.mockito.verification.VerificationMode;

import java.util.List;
import java.util.StringJoiner;

import static org.mockito.Mockito.mock;

public final class TransactionVerifier implements TransactionService {

    private final List<Object> mocks;
    private TransactionMode transactionMode = mock(TransactionMode.class);

    public TransactionVerifier(Object... mocks) {
        this.mocks = ImmutableList.builder().add(transactionMode).add(mocks).build();
    }

    @Override
    public <T> T execute(Transaction<T> transaction) {
        transactionMode.startTransaction();
        try {
            return transaction.perform();
        } finally {
            transactionMode.endTransaction();
        }
    }

    @Override
    public TransactionContext getContext() {
        transactionMode.startTransaction();
        return new TransactionContext() {
            @Override
            public void close() {
                transactionMode.endTransaction();
            }

            @Override
            public void commit() {
            }

            @Override
            public TransactionEvent getStats() {
                return null;
            }
        };
    }

    public VerificationMode inTransaction() {
        return new VerificationMode() {
            private Reporter reporter = new Reporter();

            @Override
            public void verify(VerificationData data) {
                boolean inTransaction = false;
                InvocationMatcher wanted = data.getWanted();
                List<Invocation> invocations = new AllInvocationsFinder().find(mocks);

                for (Invocation invocation : invocations) {

                    if (invocation.getMock() == transactionMode && invocation.getMethod().getName().equals("startTransaction")) {
                        inTransaction = true;
                    }
                    if (invocation.getMock() == transactionMode && invocation.getMethod().getName().equals("endTransaction")) {
                        inTransaction = false;
                    }
                    if (wanted.matches(invocation)) {
                        if (!inTransaction) {
                            throw new MockitoAssertionError(new StringJoiner("\n")
                                    .add("Method invoked outside of a transaction")
                                    .add(invocation.toString())
                                    .add(invocation.getLocation().toString())
                                    .add("")
                                    .toString()
                            );
                        }
                    }
                }

            }
        };
    }

    public VerificationMode notInTransaction() {
        return new VerificationMode() {
            private Reporter reporter = new Reporter();

            @Override
            public void verify(VerificationData data) {
                boolean inTransaction = false;
                InvocationMatcher wanted = data.getWanted();
                List<Invocation> invocations = new AllInvocationsFinder().find(mocks);

                for (Invocation invocation : invocations) {

                    if (invocation.getMock() == transactionMode && invocation.getMethod().getName().equals("startTransaction")) {
                        inTransaction = true;
                    }
                    if (invocation.getMock() == transactionMode && invocation.getMethod().getName().equals("endTransaction")) {
                        inTransaction = false;
                    }
                    if (wanted.matches(invocation)) {
                        if (inTransaction) {
                            throw new MockitoAssertionError(new StringJoiner("\n")
                                    .add("Method invoked inside of a transaction")
                                    .add(invocation.toString())
                                    .add(invocation.getLocation().toString())
                                    .add("")
                                    .toString()
                            );
                        }
                    }
                }

            }
        };
    }

    interface TransactionMode {
        void startTransaction();
        void endTransaction();
    }

}
