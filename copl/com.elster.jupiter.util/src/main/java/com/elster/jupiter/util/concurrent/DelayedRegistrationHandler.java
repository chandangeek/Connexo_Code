/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.concurrent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class DelayedRegistrationHandler implements RegistrationHandler {
    private volatile RegistrationHandler state = new QueuedHandler();

    @Override
    public void handle(Runnable registration) {
        state.handle(registration);
    }

    @Override
    public void ready() {
        RegistrationHandler queued = state;
        state = Runnable::run;
        queued.ready();
    }

    private static class QueuedHandler implements RegistrationHandler {

        private final Queue<Runnable> registrations = new ConcurrentLinkedQueue<>();

        @Override
        public void handle(Runnable registration) {
            registrations.add(registration);
        }

        public void ready() {
            while (!registrations.isEmpty()) {
                registrations.remove().run();
            }
        }
    }

}