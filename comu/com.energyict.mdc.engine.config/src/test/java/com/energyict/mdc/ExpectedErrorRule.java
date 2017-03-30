/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

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
                        if (ConstraintViolationException.class.isAssignableFrom(e.getClass())) {
                            boolean expectedViolationIsPresent=false;
                            for (ConstraintViolation<?> constraintViolation : ((ConstraintViolationException) e).getConstraintViolations()) {
                                if (constraintViolation.getMessageTemplate().equals(annotation.messageId())) {
                                    expectedViolationIsPresent=true;
                                }
                            }
                            if (!expectedViolationIsPresent) {
                                throw new AssertionError("Validation violation encountered, but the expected message was not listed: "
                                        + annotation.messageId(), e);
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
