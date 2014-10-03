package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.dashboard.rest.status.impl.ComServerTypeAdapter;
import java.util.Date;

/**
 * Represents the status of a ComServer in the REST layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-22 (09:23)
 */
public class ComServerStatusInfo {

    public String comServerName;
    public long comServerId;
    public String comServerType;
    public boolean running;
    public boolean blocked;
    public TimeDurationInfo blockTime;
    public Date blockedSince;
    public String defaultUri;

    public ComServerStatusInfo() {
        super();
    }


}