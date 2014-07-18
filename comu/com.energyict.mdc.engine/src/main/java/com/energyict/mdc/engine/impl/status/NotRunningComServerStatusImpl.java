package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.status.ComServerStatus;

import org.joda.time.Duration;

/**
 * Provides an implementation for the {@link ComServerStatus} interface
 * for a {@link ComServer} that either does not exist or is not running.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (13:10)
 */
public class NotRunningComServerStatusImpl implements ComServerStatus {

    private final String comServerName;

    public NotRunningComServerStatusImpl(ComServer comServer) {
        super();
        this.comServerName = comServer.getName();
    }

    @Override
    public String getComServerName() {
        return this.comServerName;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    @Override
    public Duration getBlockTime() {
        return null;
    }

}