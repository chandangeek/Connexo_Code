package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.mdc.engine.impl.core.ServerProcess;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Models the scheduling aspects of a {@link com.energyict.mdc.engine.model.ComServer}.
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

}