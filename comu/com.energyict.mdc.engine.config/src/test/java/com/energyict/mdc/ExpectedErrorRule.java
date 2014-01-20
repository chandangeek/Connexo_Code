package com.energyict.mdc;

import com.energyict.mdc.common.TranslatableApplicationException;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Copyrights EnergyICT
 * Date: 12/02/13
 * Time: 15:49
 */
public class ExpectedErrorRule implements TestRule {

    public ExpectedErrorRule() {
    }

    @Override
    public Statement apply(Statement base, Description description) {
        Expected annotation = description.getAnnotation(Expected.class);
        if (annotation == null) {
            return base;
        }
        return new ErrorThrowingStatement(base, annotation);
    }

    private class ErrorThrowingStatement extends Statement {

        private final Statement statement;
        private final Expected annotation;

        public ErrorThrowingStatement(Statement statement, Expected annotation) {
            this.statement = statement;
            this.annotation = annotation;
        }

        @Override
        public void evaluate() throws Throwable {
            boolean complete = false;
            try {
                statement.evaluate();
                complete = true;
            } catch (AssumptionViolatedException e) {
                throw e;
            } catch (Throwable e) {
                if (!annotation.expected().isAssignableFrom(e.getClass())) {
                    String message = "Unexpected exception, expected<"
                            + annotation.expected().getName() + "> but was<"
                            + e.getClass().getName() + ">";
                    throw new Exception(message, e);
                } else {
                    if (!annotation.messageId().isEmpty()) {
                        if (TranslatableApplicationException.class.isAssignableFrom(e.getClass())) {
                            String messageId = ((TranslatableApplicationException) e).getMessageId();
                            if (!messageId.equals(annotation.messageId())) {
                                throw new AssertionError("Expected messageId: "
                                        + annotation.messageId()+" but encountered "+messageId);

                            }
                        }
                    }
                }
            }
            if (complete) {
                throw new AssertionError("Expected exception: "
                        + annotation.expected().getName());
            }

        }

    }
}
