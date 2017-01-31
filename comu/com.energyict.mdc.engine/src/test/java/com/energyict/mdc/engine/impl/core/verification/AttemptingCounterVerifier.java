/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.verification;


import com.energyict.mdc.engine.impl.tools.Counter;

import static junit.framework.Assert.fail;

/**
 * Serves as the root for component that intend to provide an implementation
 * for the {@link CounterVerifier} interface that will attempt verification
 * a number of times before failing.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-19 (14:12)
 */
public abstract class AttemptingCounterVerifier implements CounterVerifier {

    private int maximumAttempts;
    private int actualAttempts;
    private long idleMillis;

    protected AttemptingCounterVerifier (int attempts, long idleMillis) {
        super();
        this.maximumAttempts = attempts;
        this.actualAttempts = 0;
        this.idleMillis = idleMillis;
    }

    @Override
    public void verify (Counter counter) {
        VerificationResult verificationResult = null;
        boolean continueVerification = true;
        while (continueVerification) {
            this.actualAttempts++;
            verificationResult = this.attemptVerification(counter.getValue());
            if (verificationResult.continueVerification()) {
                this.waitBetweenAttempts();
                continueVerification = this.actualAttempts < this.maximumAttempts;
            }
            else {
                continueVerification = false;
            }
        }
        if (!verificationResult.indicatesSucces()) {
            fail(this.errorMessage(verificationResult.getValue()));
        }
    }

    private void waitBetweenAttempts () {
        try {
            Thread.sleep(this.idleMillis);
        }
        catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Attempts the verification and answers <code>true</code>
     * iff the verification succeeds.
     *
     * @param counterValue The value of the Counter that needs verification
     * @return A flag that indicates if the verification should continue.
     */
    protected abstract VerificationResult attemptVerification (int counterValue);

    /**
     * Returns an error message that will go to the unit test framework
     * as a failure message.
     *
     * @param counterValue The last verified value of the Counter that failed the verification
     * @return The error message for the Counter
     */
    protected abstract String errorMessage (int counterValue);

    protected class VerificationResult {
        private boolean succes;
        private boolean continueVerification;
        private int value;

        public VerificationResult (boolean succes, boolean continueVerification, int value) {
            super();
            this.succes = succes;
            this.continueVerification = continueVerification;
            this.value = value;
        }

        public boolean indicatesSucces () {
            return succes;
        }

        public boolean continueVerification () {
            return continueVerification;
        }

        public int getValue () {
            return value;
        }

    }
}