/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.persistence.test;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Pair;
import com.google.common.collect.ImmutableList;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.finder.AllInvocationsFinder;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.Invocation;
import org.mockito.verification.VerificationMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.spy;

public final class TransactionVerifier implements TransactionService {

    private final List<Object> mocks;
    private TransactionMode transactionMode = spy(new TransactionMode());

    public TransactionVerifier(Object... mocks) {
        this.mocks = ImmutableList.builder().add(transactionMode).add(mocks).build();
    }

    @Override
    public <T> T execute(Transaction<T> transaction) {
        transactionMode.startTransaction();
        try {
            T value = transaction.perform();
            transactionMode.commitTransaction();
            return value;
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
                transactionMode.commitTransaction();
            }

            @Override
            public TransactionEvent getStats() {
                return null;
            }
        };
    }

    @Override
    public boolean isInTransaction() {
        boolean intransaction = false;
        for (Operation call : transactionMode.calls) {
            intransaction = Operation.START.equals(call);
        }
        return intransaction;
    }

    public VerificationMode inTransaction() {
        return new VerificationMode() {
            private Reporter reporter = new Reporter();

            @Override
            public void verify(VerificationData data) {
                boolean neverInvoked = true;
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
                        neverInvoked = false;
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
                if (neverInvoked) {
                    throw new MockitoAssertionError("Method " + wanted.toString() + " was never invoked.");
                }
            }
        };
    }

    public VerificationMode inTransaction(final int nthTransaction) {
        return new VerificationMode() {

            @Override
            public void verify(VerificationData data) {
                boolean neverInvoked = true;
                int transactionCount = 0;
                boolean inTransaction = false;
                InvocationMatcher wanted = data.getWanted();
                List<Invocation> invocations = new AllInvocationsFinder().find(mocks);

                List<Pair<Invocation, Optional<Integer>>> otherInvocations = new ArrayList<>();

                for (Invocation invocation : invocations) {

                    if (invocation.getMock() == transactionMode && invocation.getMethod().getName().equals("startTransaction")) {
                        inTransaction = true;
                        transactionCount++;
                    }
                    if (invocation.getMock() == transactionMode && invocation.getMethod().getName().equals("endTransaction")) {
                        inTransaction = false;
                    }
                    if (wanted.matches(invocation)) {
                        neverInvoked = false;
                        if (!inTransaction) {
                            otherInvocations.add(Pair.of(invocation, Optional.<Integer>empty()));
                        }
                        if (transactionCount != nthTransaction) {
                            otherInvocations.add(Pair.of(invocation, Optional.of(transactionCount)));
                        } else {
                            return;
                        }
                    }
                }
                if (neverInvoked) {
                    throw new MockitoAssertionError("Method " + wanted.toString() + " was never invoked.");
                } else if (!otherInvocations.isEmpty()) {
                    String message = otherInvocations.stream()
                            .flatMap(invocation -> Arrays.asList(
                                    invocation.getLast().map(number -> "Method invoked in the " + number + suffix(number) + " transaction instead of in the " + nthTransaction + suffix(nthTransaction)).orElse("Method invoked outside of a transaction"),
                                    invocation.getFirst().toString(),
                                    invocation.getFirst().getLocation().toString(),
                                    ""
                                ).stream())
                            .collect(Collectors.joining("\n"));
                    throw new MockitoAssertionError(message);
                }
            }
        };
    }

    private String suffix(int n) {
        int r100 = n % 100;
        if (r100 == 11 || r100 == 12 || r100 == 13) {
            return "th";
        }
        int r10 = r100 % 10;
        switch (r10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public VerificationMode notInTransaction() {
        return new VerificationMode() {
            private Reporter reporter = new Reporter();

            @Override
            public void verify(VerificationData data) {
                boolean neverInvoked = true;
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
                        neverInvoked = false;
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
                if (neverInvoked) {
                    throw new MockitoAssertionError("Method " + wanted.toString() + " was never invoked.");
                }
            }
        };
    }

    class TransactionMode {

        private List<Operation> calls = new ArrayList<>();

        public void startTransaction() {
            calls.add(Operation.START);
        }

        public void endTransaction() {
            calls.add(Operation.END);
        }

        public void commitTransaction() {
            calls.add(Operation.COMMIT);
        }
    }

    private enum Operation {
        START, END, COMMIT
    }

    public TransactionAssertion assertThatTransaction(int n) {
        return new TransactionAssertion(n);
    }

    public class TransactionAssertion {

        private final int targetedTransaction;
        private final List<Operation> relevantScope;

        public TransactionAssertion(int targetedTransaction) {
            this.targetedTransaction = targetedTransaction;
            int count = 0;
            for (int i = 0; i < transactionMode.calls.size(); i++) {
                if (transactionMode.calls.get(i) == Operation.START) {
                    count++;
                }
                if (count == targetedTransaction) {
                    relevantScope = transactionMode.calls.subList(i, transactionMode.calls.size());
                    return;
                }
            }
            relevantScope = Collections.emptyList();
        }

        public void wasCommitted() {
            if (relevantScope.isEmpty()) {
                throw new MockitoAssertionError("There was no " + targetedTransaction + suffix(targetedTransaction) + " transaction.");
            }
            if (relevantScope.size() < 3 ||
                    !asList(Operation.START, Operation.COMMIT, Operation.END).equals(relevantScope.subList(0, 3))) {
                throw new MockitoAssertionError(targetedTransaction + suffix(targetedTransaction) + " transaction was not committed.");
            }
        }

        public void wasNotCommitted() {
            if (relevantScope.isEmpty()) {
                throw new MockitoAssertionError("There was no " + targetedTransaction + suffix(targetedTransaction) + " transaction.");
            }
            if (relevantScope.size() < 2 ||
                    !asList(Operation.START, Operation.END).equals(relevantScope.subList(0, 2))) {
                throw new MockitoAssertionError(targetedTransaction + suffix(targetedTransaction) + " transaction was committed.");
            }
        }
    }

	@Override
	public TransactionBuilder builder() {
		// TODO Auto-generated method stub
		return null;
	}
}
