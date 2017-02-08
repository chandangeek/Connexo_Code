/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.ComServer;

import java.util.concurrent.ThreadFactory;

/**
 * Provides an implementation for the ThreadFactory interface
 * that creates Threads on behalf of a {@link ComServer}
 * making sure that all threads are in the same ThreadGroup.
 * The name of the ThreadGroup is the name of the ComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-22 (10:55)
 */
public class ComServerThreadFactory implements ThreadFactory {

    private final ThreadGroup threadGroup;

    public ComServerThreadFactory(ComServer comServer) {
        super();
        this.threadGroup = new ThreadGroup(comServer.getName());
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(this.threadGroup, r);
    }

}