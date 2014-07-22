package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

/**
 * Represents the status of a ComServer in the REST layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-22 (09:23)
 */
public class ComServerStatusInfo {

    public final String comServerName;
    public final ComServerType comServerType;
    public final boolean running;
    public final boolean blocked;
    public final TimeDurationInfo blockTime;

    public ComServerStatusInfo(ComServerStatus status) {
        super();
        this.comServerName = status.getComServerName();
        this.comServerType = status.getComServerType();
        this.running = status.isRunning();
        this.blocked = status.isBlocked();
        if (this.blocked) {
            this.blockTime = new TimeDurationInfo((int) status.getBlockTime().getStandardSeconds());
        }
        else {
            this.blockTime = null;
        }
    }

}