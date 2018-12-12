/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.persistence.test.rules;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ExpectedConstraintViolationRule implements TestRule {

    public ExpectedConstraintViolationRule() {
    }

    @Override
    public Statement apply(Statement base, Description description) {
        ExpectedConstraintViolation annotation = description.getAnnotation(ExpectedConstraintViolation.class);
        if (annotation == null) {
            return base;
        }
        return new ErrorThrowingStatement(base, annotation);
    }

    private class ErrorThrowingStatement extends Statement {

        private final Statement statement;
        private final ExpectedConstraintViolation annotation;

        public ErrorThrowingStatement(Statement statement, ExpectedConstraintViolation annotation) {
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
            } catch (ConstraintViolationException e) {
                boolean expectedViolationIsPresent=false;
                boolean expectedViolationIsPresentForProperty=false;
                List<String> encounteredViolations = new ArrayList<>();
                for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
                    encounteredViolations.add(constraintViolation.getMessageTemplate()+(constraintViolation.getPropertyPath().toString().isEmpty()?"(global)":"(->"+constraintViolation.getPropertyPath()+")"));
                    if (constraintViolation.getMessageTemplate().equals(annotation.messageId())) {
                        expectedViolationIsPresent=true;
                        if (!annotation.property().isEmpty() && annotation.property().equals(constraintViolation.getPropertyPath().toString())) {
                            expectedViolationIsPresentForProperty=true;
                        }
                    }
                }
                if (!expectedViolationIsPresent) {
                    throw new AssertionError("Validation violation encountered, but the expected message '"+annotation.messageId()+"' was not listed, did see [" +
                            Joiner.on(',').join(encounteredViolations)+"]"
                            , e);
                }
                if (!annotation.property().isEmpty() && !expectedViolationIsPresentForProperty) {
                    throw new AssertionError("Validation violation encountered with correct message, but not for the desired property '"+annotation.property()+"', did see [" +
                            Joiner.on(',').join(encounteredViolations)+"]"
                            , e);

                }
                if (annotation.strict() && encounteredViolations.size()>1) {
                    throw new AssertionError("Multiple validation violations encountered, saw [" +
                            Joiner.on(',').join(encounteredViolations)+"]. Switch 'strict' to false if this is allowed in this test."
                            , e);
                }
            } catch (Throwable e) {
                String message = "Unexpected exception, expected<ConstraintViolationException> but was<"
                        + e.getClass().getName() + ">";
                throw new Exception(message, e);
            }
            if (complete) {
                throw new AssertionError("Expected exception: ConstraintViolationException, but no exception was thrown");
            }

        }

    }
}
