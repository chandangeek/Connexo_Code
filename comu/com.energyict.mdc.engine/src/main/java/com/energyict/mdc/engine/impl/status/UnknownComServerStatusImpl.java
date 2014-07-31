package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

import org.joda.time.Duration;

/**
 * Provides an implementation for the {@link ComServerStatus} interface
 * for a {@link ComServer} that does not exist.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (13:10)
 */
public class UnknownComServerStatusImpl implements ComServerStatus {

    @Override
    public String getComServerName() {
        return "Not existing";
    }

    @Override
    public ComServerType getComServerType() {
        return ComServerType.NOT_APPLICABLE;
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