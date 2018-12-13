/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.ComPort;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provides an implementation for the {@link ThreadFactory} interface
 * that sets the name of the created Threads so that they can visually
 * be linked to the {@link ComPort} for which they are working.
 * It delegates to an actual ThreadFactory.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-08-29 (14:22)
 */
class ComPortThreadFactory implements ThreadFactory {

    private ComPort comPort;
    private ThreadFactory actual;
    private AtomicLong count = new AtomicLong(0);

    ComPortThreadFactory(ComPort comPort, ThreadFactory actual) {
        super();
        this.comPort = comPort;
        this.actual = actual;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = this.actual.newThread(r);
        thread.setName("ComPort schedule worker for " + this.comPort.getName() + " - " + count.getAndIncrement());
        return thread;
    }
}