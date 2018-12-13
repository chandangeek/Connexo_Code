/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.verification;

/**
 * Provides factory services for {@link CounterVerifier}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-19 (13:52)
 */
public class CounterVerifierFactory {

    private int attempts;
    private long idleMillis;

    /**
     * Creates a new CounterVerifierFactory that will always produce
     * {@link CounterVerifier}s that use the number of attempts
     * and idle milli seconds.
     *
     * @param attempts The number of times the verification will be attempted
     *                 before an AssertionFailedError will be thrown
     * @param idleMillis The number of milli seconds that the CounterVerifier
     *                   should idle between each verification attempt
     */
    public CounterVerifierFactory (int attempts, long idleMillis) {
        super();
        this.attempts = attempts;
        this.idleMillis = idleMillis;
    }

    public int getAttempts () {
        return attempts;
    }

    public long getIdleMillis () {
        return idleMillis;
    }

    /**
     * Returns a {@link CounterVerifier} that verifies
     * that the counter was never incremented,
     * i.e. its value is zero.
     *
     * @return The CounterVerifier
     */
    public CounterVerifier never () {
        return new AtMost(0, this.attempts, this.idleMillis);
    }

    /**
     * Returns a {@link CounterVerifier} that verifies
     * that the counter's value is the expected value.
     *
     * @param expectedValue The expected Counter value
     * @return The CounterVerifier
     */
    public CounterVerifier times (long expectedValue) {
        return new Times(expectedValue, this.attempts, this.idleMillis);
    }

    /**
     * Returns a {@link CounterVerifier} that verifies
     * that the counter's value &lt;= maximum value.
     *
     * @param maximumValue The maximum Counter value
     * @return The CounterVerifier
     */
    public CounterVerifier atMost (long maximumValue) {
        return new AtMost(maximumValue, this.attempts, this.idleMillis);
    }

    /**
     * Returns a {@link CounterVerifier} that verifies
     * that the counter was incremented at most once,
     * i.e. its value is one.
     *
     * @return The CounterVerifier
     */
    public CounterVerifier atMostOnce () {
        return atMost(1);
    }

    /**
     * Returns a {@link CounterVerifier} that verifies
     * that the counter was incremented at least once,
     * i.e. its value &gt; one.
     *
     * @param minimumValue The minimum Counter value
     * @return The CounterVerifier
     */
    public CounterVerifier atLeast (long minimumValue) {
        return new AtLeast(minimumValue, this.attempts, this.idleMillis);
    }

    /**
     * Returns a {@link CounterVerifier} that verifies
     * that the counter was incremented at least once,
     * i.e. its value is at least one.
     *
     * @return The CounterVerifier
     */
    public CounterVerifier atLeastOnce () {
        return atLeast(1);
    }

}