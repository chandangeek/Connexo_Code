package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

import java.time.Duration;
import java.util.Date;

/**
 * Provides an implementation for the {@link ComServerStatus} interface
 * for a {@link ComServer} that either does not exist or is not running.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (13:10)
 */
public class NotRunningComServerStatusImpl implements ComServerStatus {

    private final String comServerName;
    private final long id;
    private final ComServerType type;

    public NotRunningComServerStatusImpl(ComServer comServer) {
        super();
        this.comServerName = comServer.getName();
        this.id=comServer.getId();
        this.type = ComServerType.typeFor(comServer);
    }

    @Override
    public String getComServerName() {
        return this.comServerName;
    }

    @Override
    public ComServerType getComServerType() {
        return this.type;
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

    @Override
    public Date getBlockTimestamp() {
        return null;
    }

    @Override
    public long getComServerId() {
        return this.id;
    }

}