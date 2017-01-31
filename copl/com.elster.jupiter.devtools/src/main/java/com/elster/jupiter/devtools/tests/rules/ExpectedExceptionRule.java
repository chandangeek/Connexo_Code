/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests.rules;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpectedExceptionRule implements TestRule {

    public ExpectedExceptionRule() {
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
            try {
                statement.evaluate();
            } catch (AssumptionViolatedException e) {
                throw e;
            } catch (Throwable e) {
                if (!annotation.value().isAssignableFrom(e.getClass())) {
                    String message = "Unexpected exception, expected <" + annotation.value().getName() + "> but was <" + e.getClass().getName() + ">";
                    AssertionError assertionError = new AssertionError(message, e);
                    assertionError.addSuppressed(e);
                    throw assertionError;
                } else {
                    if (!annotation.message().isEmpty()) {
                        assertThat(e.getMessage()).isEqualTo(annotation.message());
                    }
                    return;
                }
            }
            throw new AssertionError("Expected exception: " + annotation.value().getName());

        }

    }
}
