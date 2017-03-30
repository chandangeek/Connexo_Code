/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.verification;

/**
 * Provides an implementation for the {@link CounterVerifier} interface
 * that verifies that the counter's value &lt;= an expected value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-19 (13:49)
 */
public class AtMost extends AttemptingCounterVerifier {

    private long maximum;

    public AtMost (long maximum, int attempts, long idleMillis) {
        super(attempts, idleMillis);
        this.maximum = maximum;
    }

    @Override
    protected VerificationResult attemptVerification (int counterValue) {
        return new VerificationResult(this.maximum >= counterValue, true, counterValue);
    }

    @Override
    protected String errorMessage (int counterValue) {
        StringBuilder builder = new StringBuilder();
        builder.
                append("Wanted at most ").
                append(this.maximum);
        if (this.maximum == 1) {
            builder.append(" invocation");
        }
        else {
            builder.append(" invocations");
        }
        builder.append(" but found ").append(counterValue);
        return builder.toString();
    }

}