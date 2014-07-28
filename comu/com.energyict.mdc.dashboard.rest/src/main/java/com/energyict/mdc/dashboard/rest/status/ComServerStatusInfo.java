package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the status of a ComServer in the REST layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-22 (09:23)
 */
public class ComServerStatusInfo {

    public String comServerName;
    public ComServerType comServerType;
    public boolean running;
    public boolean blocked;
    public TimeDurationInfo blockTime;

    public ComServerStatusInfo() {
        super();
    }

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