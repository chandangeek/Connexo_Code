/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.verification;

/**
 * Provides an implementation for the {@link CounterVerifier} interface
 * that verifies that the Counter's value exactly matches an expected value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-19 (13:46)
 */
public class Times extends AttemptingCounterVerifier {

    private long expectedValue;

    public Times (long expectedValue, int attempts, long idleMillis) {
        super(attempts, idleMillis);
        this.expectedValue = expectedValue;
    }

    @Override
    protected VerificationResult attemptVerification (int counterValue) {
        return new VerificationResult(this.expectedValue == counterValue, true, counterValue);
    }

    @Override
    protected String errorMessage (int counterValue) {
        StringBuilder builder = new StringBuilder();
        builder.
            append("Wanted exactly ").
            append(this.expectedValue);
        if (this.expectedValue == 1) {
            builder.append(" invocation");
        }
        else {
            builder.append(" invocations");
        }
        builder.append(" but found ").append(counterValue);
        return builder.toString();
    }

}