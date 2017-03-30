/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.verification;

/**
 * Provides an implementation for the {@link CounterVerifier} interface
 * that verifies that the Counter's value &gt;= an expected value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-19 (13:49)
 */
public class AtLeast extends AttemptingCounterVerifier {

    private long minimum;

    public AtLeast (long minimum, int attempts, long idleMillis) {
        super(attempts, idleMillis);
        this.minimum = minimum;
    }

    @Override
    protected VerificationResult attemptVerification (int counterValue) {
        return new VerificationResult(this.minimum <= counterValue, this.minimum > counterValue, counterValue);
    }

    @Override
    protected String errorMessage (int counterValue) {
        StringBuilder builder = new StringBuilder();
        builder.
            append("Wanted at least ").
            append(this.minimum);
        if (this.minimum == 1) {
            builder.append(" invocation");
        }
        else {
            builder.append(" invocations");
        }
        builder.append(" but found ");
        if (counterValue == 0) {
            builder.append("none");
        }
        else {
            builder.append("only ").append(counterValue);
        }
        return builder.toString();
    }

}