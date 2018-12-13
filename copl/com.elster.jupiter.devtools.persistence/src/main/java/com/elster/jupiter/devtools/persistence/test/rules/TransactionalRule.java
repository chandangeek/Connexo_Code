/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.persistence.test.rules;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class TransactionalRule implements TestRule {

    private final TransactionService transactionService;

    public TransactionalRule(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (description.getAnnotation(Transactional.class) == null) {
            return base;
        }
        return new TransactionWrappedStatement(base);
    }

    private class TransactionWrappedStatement extends Statement {

        private final Statement statement;

        public TransactionWrappedStatement(Statement statement) {
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            try (TransactionContext context = transactionService.getContext()) {
                statement.evaluate();
            }

        }

    }

}
