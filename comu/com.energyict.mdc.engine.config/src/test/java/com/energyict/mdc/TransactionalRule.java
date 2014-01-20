package com.energyict.mdc;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Copyrights EnergyICT
 * Date: 12/02/13
 * Time: 15:49
 */
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
            try {
                try (TransactionContext context = transactionService.getContext()) {
                    statement.evaluate();
                }
            } catch (ForceRollbackException e) {
                if (e.getCause() != null) {
                    throw e.getCause();
                }
            }

        }

    }

    private static class AutoFailTransaction implements Transaction<Void> {

        private final Statement base;

        private AutoFailTransaction(Statement base) {
            this.base = base;
        }

        @Override
        public Void perform() {
            try {
                base.evaluate();
            } catch (Throwable throwable) {
                if (throwable instanceof RuntimeException) {
                    throw ((RuntimeException) throwable);
                }
                if (throwable instanceof Error) {
                    throw ((Error) throwable);
                }
                throw new ForceRollbackException(throwable);
            }
            throw ForceRollbackException.INSTANCE;
        }
    }

    private static class ForceRollbackException extends RuntimeException {

        public static ForceRollbackException INSTANCE = new ForceRollbackException(null);

        public ForceRollbackException(Throwable e) {
            super(e);
        }
    }
}
