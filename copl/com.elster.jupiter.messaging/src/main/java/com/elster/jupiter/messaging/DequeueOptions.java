/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import java.time.Duration;

/**
 * Models the dequeueing options for a {@link SubscriberSpec}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-11 (09:49)
 */
public final class DequeueOptions {
    private final Duration wait;
    private final Duration retryDelay;

    public static DequeueOptionsBuilder wait(Duration time) {
        return new DequeueOptionsBuilder(time);
    }

    public Duration waitTime() {
        return this.wait;
    }

    public Duration retryDelay() {
        return this.retryDelay;
    }

    public static final class DequeueOptionsBuilder {
        private final Duration wait;

        DequeueOptionsBuilder(Duration wait) {
            this.wait = wait;
        }

        public DequeueOptions retryAfter(Duration time) {
            return new DequeueOptions(this.wait, time);
        }
    }

    private DequeueOptions(Duration wait, Duration retryDelay) {
        this.wait = wait;
        this.retryDelay = retryDelay;
    }

}