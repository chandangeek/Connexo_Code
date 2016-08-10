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

    public ComServer getComServer ();

    public boolean isRemoteQueryApiStarted ();

    public int getCollectedDataStorageCapacity ();

    public int getCurrentCollectedDataStorageSize ();

    public int getCurrentCollectedDataStorageLoadPercentage ();

    public int getNumberOfCollectedDataStorageThreads ();

    public int getCollectedDataStorageThreadPriority ();

    public void eventClientRegistered();

    public void eventClientUnregistered();

    public void eventWasPublished();

    public void refresh(ComPort comPort);

}