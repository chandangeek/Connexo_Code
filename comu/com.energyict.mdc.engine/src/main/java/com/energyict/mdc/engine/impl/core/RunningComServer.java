/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;

/**
 * Models the aspects of a {@link com.energyict.mdc.engine.config.ComServer} that is actually running.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (09:55)
 */
public interface RunningComServer extends ServerProcess {

    ComServer getComServer();

    boolean isRemoteQueryApiStarted();

    int getCollectedDataStorageCapacity();

    int getCurrentCollectedDataStorageSize();

    int getCurrentCollectedDataStorageLoadPercentage();

    int getNumberOfCollectedDataStorageThreads();

    int getCollectedDataStorageThreadPriority();

    String getAcquiredTokenThreadNames();

    void eventClientRegistered();

    void eventClientUnregistered();

    void eventWasPublished();

    public void refresh(ComPort comPort);

}